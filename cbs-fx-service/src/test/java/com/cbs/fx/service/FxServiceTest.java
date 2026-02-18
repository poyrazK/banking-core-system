package com.cbs.fx.service;

import com.cbs.common.exception.ApiException;
import com.cbs.fx.dto.BookFxDealRequest;
import com.cbs.fx.dto.CancelFxDealRequest;
import com.cbs.fx.dto.CreateFxQuoteRequest;
import com.cbs.fx.dto.CreateFxRateRequest;
import com.cbs.fx.dto.FxDealResponse;
import com.cbs.fx.dto.FxQuoteResponse;
import com.cbs.fx.dto.FxRateResponse;
import com.cbs.fx.model.FxDeal;
import com.cbs.fx.model.FxDealStatus;
import com.cbs.fx.model.FxRate;
import com.cbs.fx.model.FxRateStatus;
import com.cbs.fx.model.FxSide;
import com.cbs.fx.repository.FxDealRepository;
import com.cbs.fx.repository.FxRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FxServiceTest {

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private FxDealRepository fxDealRepository;

    private FxService fxService;

    @BeforeEach
    void setUp() {
        fxService = new FxService(fxRateRepository, fxDealRepository);
    }

    @Test
    void createRate_normalizesCurrencyPair() {
        CreateFxRateRequest request = new CreateFxRateRequest(
                "usd",
                "try",
                new BigDecimal("35.00000000"),
                new BigDecimal("10.0000"),
                new BigDecimal("12.0000")
        );
        when(fxRateRepository.existsByCurrencyPair("USD/TRY")).thenReturn(false);
        when(fxRateRepository.save(any(FxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FxRateResponse response = fxService.createRate(request);

        assertEquals("USD/TRY", response.currencyPair());
    }

    @Test
    void createQuote_throwsWhenRateInactive() {
        FxRate rate = new FxRate("USD", "TRY", new BigDecimal("35.00000000"), new BigDecimal("10.0000"), new BigDecimal("12.0000"));
        rate.setStatus(FxRateStatus.INACTIVE);
        when(fxRateRepository.findByCurrencyPair("USD/TRY")).thenReturn(Optional.of(rate));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> fxService.createQuote(new CreateFxQuoteRequest("usd", "try", FxSide.BUY, new BigDecimal("100.00")))
        );

        assertEquals("FX_RATE_INACTIVE", exception.getErrorCode());
    }

    @Test
    void createQuote_computesQuoteAmount() {
        FxRate rate = new FxRate("USD", "TRY", new BigDecimal("35.00000000"), new BigDecimal("10.0000"), new BigDecimal("12.0000"));
        when(fxRateRepository.findByCurrencyPair("USD/TRY")).thenReturn(Optional.of(rate));

        FxQuoteResponse response = fxService.createQuote(new CreateFxQuoteRequest("usd", "try", FxSide.BUY, new BigDecimal("100.00")));

        assertEquals(new BigDecimal("35.04200000"), response.rate());
        assertEquals(new BigDecimal("3504.20"), response.quoteAmount());
    }

    @Test
    void bookDeal_throwsWhenReferenceExists() {
        when(fxDealRepository.existsByReference("REF-1")).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> fxService.bookDeal(new BookFxDealRequest(1L, 10L, 20L, "USD", "TRY", FxSide.BUY, new BigDecimal("100.00"), "ref-1"))
        );

        assertEquals("FX_DEAL_REFERENCE_EXISTS", exception.getErrorCode());
    }

    @Test
    void cancelDeal_setsCancelledStatusAndReason() {
        FxDeal deal = new FxDeal(1L, 10L, 20L, "USD", "TRY", FxSide.BUY, new BigDecimal("100.00"), new BigDecimal("3500.00"), new BigDecimal("35.00000000"), "REF-1");
        when(fxDealRepository.findById(9L)).thenReturn(Optional.of(deal));
        when(fxDealRepository.save(any(FxDeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FxDealResponse response = fxService.cancelDeal(9L, new CancelFxDealRequest("  duplicate  "));

        assertEquals(FxDealStatus.CANCELLED, response.status());
        assertEquals("duplicate", response.cancelReason());
    }
}
