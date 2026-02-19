package com.cbs.fx.service;

import com.cbs.common.exception.ApiException;
import com.cbs.fx.dto.BookFxDealRequest;
import com.cbs.fx.dto.CancelFxDealRequest;
import com.cbs.fx.dto.CreateFxQuoteRequest;
import com.cbs.fx.dto.CreateFxRateRequest;
import com.cbs.fx.dto.FxDealResponse;
import com.cbs.fx.dto.FxQuoteResponse;
import com.cbs.fx.dto.FxRateResponse;
import com.cbs.fx.dto.UpdateFxRateRequest;
import com.cbs.fx.model.FxDeal;
import com.cbs.fx.model.FxDealStatus;
import com.cbs.fx.model.FxRate;
import com.cbs.fx.model.FxRateStatus;
import com.cbs.fx.model.FxSide;
import com.cbs.fx.repository.FxDealRepository;
import com.cbs.fx.repository.FxRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class FxService {

    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

    private final FxRateRepository fxRateRepository;
    private final FxDealRepository fxDealRepository;

    public FxService(FxRateRepository fxRateRepository,
                     FxDealRepository fxDealRepository) {
        this.fxRateRepository = fxRateRepository;
        this.fxDealRepository = fxDealRepository;
    }

    @Transactional
    public FxRateResponse createRate(CreateFxRateRequest request) {
        String baseCurrency = normalizeCurrency(request.baseCurrency());
        String quoteCurrency = normalizeCurrency(request.quoteCurrency());

        if (baseCurrency.equals(quoteCurrency)) {
            throw new ApiException("FX_INVALID_PAIR", "Base and quote currencies must be different");
        }

        String currencyPair = toPair(baseCurrency, quoteCurrency);
        if (fxRateRepository.existsByCurrencyPair(currencyPair)) {
            throw new ApiException("FX_RATE_EXISTS", "FX rate already exists for currency pair");
        }

        FxRate rate = new FxRate(
                baseCurrency,
                quoteCurrency,
                request.midRate(),
                request.buySpreadBps(),
                request.sellSpreadBps()
        );

        return FxRateResponse.from(fxRateRepository.save(rate));
    }

    @Transactional
    public FxRateResponse updateRate(String currencyPair, UpdateFxRateRequest request) {
        FxRate rate = findRate(currencyPair);
        rate.setMidRate(request.midRate());
        rate.setBuySpreadBps(request.buySpreadBps());
        rate.setSellSpreadBps(request.sellSpreadBps());
        rate.setStatus(request.status());
        return FxRateResponse.from(fxRateRepository.save(rate));
    }

    @Transactional(readOnly = true)
    public FxRateResponse getRate(String currencyPair) {
        return FxRateResponse.from(findRate(currencyPair));
    }

    @Transactional(readOnly = true)
    public List<FxRateResponse> listRates(FxRateStatus status) {
        List<FxRate> rates = status == null
                ? fxRateRepository.findAll().stream().sorted(Comparator.comparing(FxRate::getId).reversed()).toList()
                : fxRateRepository.findByStatusOrderByIdDesc(status);
        return rates.stream().map(FxRateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FxQuoteResponse createQuote(CreateFxQuoteRequest request) {
        String baseCurrency = normalizeCurrency(request.baseCurrency());
        String quoteCurrency = normalizeCurrency(request.quoteCurrency());
        FxRate rate = findRate(toPair(baseCurrency, quoteCurrency));

        if (rate.getStatus() != FxRateStatus.ACTIVE) {
            throw new ApiException("FX_RATE_INACTIVE", "FX rate is not active");
        }

        BigDecimal pricedRate = calculateSideRate(rate, request.side());
        BigDecimal quoteAmount = request.baseAmount().multiply(pricedRate).setScale(2, RoundingMode.HALF_UP);

        return new FxQuoteResponse(rate.getCurrencyPair(), request.side(), request.baseAmount(), pricedRate, quoteAmount);
    }

    @Transactional
    public FxDealResponse bookDeal(BookFxDealRequest request) {
        String baseCurrency = normalizeCurrency(request.baseCurrency());
        String quoteCurrency = normalizeCurrency(request.quoteCurrency());
        String reference = normalizeReference(request.reference());

        if (fxDealRepository.existsByReference(reference)) {
            throw new ApiException("FX_DEAL_REFERENCE_EXISTS", "Reference already exists");
        }

        FxRate rate = findRate(toPair(baseCurrency, quoteCurrency));
        if (rate.getStatus() != FxRateStatus.ACTIVE) {
            throw new ApiException("FX_RATE_INACTIVE", "FX rate is not active");
        }

        BigDecimal appliedRate = calculateSideRate(rate, request.side());
        BigDecimal quoteAmount = request.baseAmount().multiply(appliedRate).setScale(2, RoundingMode.HALF_UP);

        FxDeal deal = new FxDeal(
                request.customerId(),
                request.debitAccountId(),
                request.creditAccountId(),
                baseCurrency,
                quoteCurrency,
                request.side(),
                request.baseAmount(),
                quoteAmount,
                appliedRate,
                reference
        );

        return FxDealResponse.from(fxDealRepository.save(deal));
    }

    @Transactional(readOnly = true)
    public FxDealResponse getDeal(Long dealId) {
        return FxDealResponse.from(findDeal(dealId));
    }

    @Transactional(readOnly = true)
    public List<FxDealResponse> listDeals(Long customerId, FxDealStatus status) {
        List<FxDeal> deals;

        if (customerId != null && status != null) {
            deals = fxDealRepository.findByCustomerIdAndStatusOrderByIdDesc(customerId, status);
        } else if (customerId != null) {
            deals = fxDealRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (status != null) {
            deals = fxDealRepository.findByStatusOrderByIdDesc(status);
        } else {
            deals = fxDealRepository.findAll().stream().sorted(Comparator.comparing(FxDeal::getId).reversed()).toList();
        }

        return deals.stream().map(FxDealResponse::from).toList();
    }

    @Transactional
    public FxDealResponse cancelDeal(Long dealId, CancelFxDealRequest request) {
        FxDeal deal = findDeal(dealId);
        if (deal.getStatus() == FxDealStatus.CANCELLED) {
            throw new ApiException("FX_DEAL_ALREADY_CANCELLED", "Deal is already cancelled");
        }

        deal.setStatus(FxDealStatus.CANCELLED);
        deal.setCancelReason(request.reason().trim());
        return FxDealResponse.from(fxDealRepository.save(deal));
    }

    private FxRate findRate(String currencyPair) {
        String normalizedPair = normalizePair(currencyPair);
        return fxRateRepository.findByCurrencyPair(normalizedPair)
                .orElseThrow(() -> new ApiException("FX_RATE_NOT_FOUND", "FX rate not found"));
    }

    private FxDeal findDeal(Long dealId) {
        return fxDealRepository.findById(dealId)
                .orElseThrow(() -> new ApiException("FX_DEAL_NOT_FOUND", "FX deal not found"));
    }

    private BigDecimal calculateSideRate(FxRate rate, FxSide side) {
        BigDecimal spreadFactor = side == FxSide.BUY
                ? BigDecimal.ONE.add(rate.getSellSpreadBps().divide(TEN_THOUSAND, 10, RoundingMode.HALF_UP))
                : BigDecimal.ONE.subtract(rate.getBuySpreadBps().divide(TEN_THOUSAND, 10, RoundingMode.HALF_UP));

        return rate.getMidRate().multiply(spreadFactor).setScale(8, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase();
    }

    private String normalizePair(String currencyPair) {
        return currencyPair.trim().toUpperCase();
    }

    private String toPair(String baseCurrency, String quoteCurrency) {
        return baseCurrency + "/" + quoteCurrency;
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }
}
