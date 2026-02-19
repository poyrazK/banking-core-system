package com.cbs.card.service;

import com.cbs.card.dto.CardResponse;
import com.cbs.card.dto.CardStatusReasonRequest;
import com.cbs.card.dto.CreateCardRequest;
import com.cbs.card.dto.UpdateCardLimitRequest;
import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.repository.CardRepository;
import com.cbs.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        String cardNumber = normalizeCardNumber(request.cardNumber());
        String token = normalizeToken(request.token());

        if (cardRepository.existsByCardNumber(cardNumber)) {
            throw new ApiException("CARD_NUMBER_EXISTS", "Card number already exists");
        }

        if (cardRepository.existsByToken(token)) {
            throw new ApiException("CARD_TOKEN_EXISTS", "Card token already exists");
        }

        if (!request.expiryDate().isAfter(LocalDate.now())) {
            throw new ApiException("CARD_INVALID_EXPIRY", "Expiry date must be in the future");
        }

        Card card = new Card(
                request.customerId(),
                request.accountId(),
                cardNumber,
                token,
                request.cardType(),
                request.dailyLimit(),
                request.monthlyLimit(),
                request.expiryDate()
        );

        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public CardResponse getCard(Long cardId) {
        return CardResponse.from(findCard(cardId));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> listCards(Long customerId, Long accountId, CardStatus status) {
        List<Card> cards;

        if (customerId != null && status != null && accountId == null) {
            cards = cardRepository.findByCustomerIdAndStatusOrderByIdDesc(customerId, status);
        } else if (customerId != null && accountId == null && status == null) {
            cards = cardRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (accountId != null && customerId == null && status == null) {
            cards = cardRepository.findByAccountIdOrderByIdDesc(accountId);
        } else if (status != null && customerId == null && accountId == null) {
            cards = cardRepository.findByStatusOrderByIdDesc(status);
        } else {
            cards = cardRepository.findAll().stream()
                    .filter(card -> customerId == null || card.getCustomerId().equals(customerId))
                    .filter(card -> accountId == null || card.getAccountId().equals(accountId))
                    .filter(card -> status == null || card.getStatus() == status)
                    .sorted(Comparator.comparing(Card::getId).reversed())
                    .toList();
        }

        return cards.stream().map(CardResponse::from).toList();
    }

    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = findCard(cardId);
        if (card.getStatus() != CardStatus.NEW && card.getStatus() != CardStatus.FROZEN) {
            throw new ApiException("CARD_NOT_ACTIVATABLE", "Only new or frozen cards can be activated");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setStatusReason(null);
        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional
    public CardResponse freezeCard(Long cardId, CardStatusReasonRequest request) {
        Card card = findCard(cardId);
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ApiException("CARD_NOT_ACTIVE", "Only active cards can be frozen");
        }

        card.setStatus(CardStatus.FROZEN);
        card.setStatusReason(request.reason().trim());
        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional
    public CardResponse blockCard(Long cardId, CardStatusReasonRequest request) {
        Card card = findCard(cardId);
        if (card.getStatus() == CardStatus.BLOCKED || card.getStatus() == CardStatus.CLOSED) {
            throw new ApiException("CARD_ALREADY_BLOCKED", "Card is already blocked or closed");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setStatusReason(request.reason().trim());
        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional
    public CardResponse updateLimits(Long cardId, UpdateCardLimitRequest request) {
        Card card = findCard(cardId);
        if (card.getStatus() == CardStatus.CLOSED || card.getStatus() == CardStatus.BLOCKED) {
            throw new ApiException("CARD_LIMIT_UPDATE_NOT_ALLOWED", "Cannot update limits for blocked or closed cards");
        }

        card.setDailyLimit(request.dailyLimit());
        card.setMonthlyLimit(request.monthlyLimit());
        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional
    public CardResponse closeCard(Long cardId, CardStatusReasonRequest request) {
        Card card = findCard(cardId);
        if (card.getStatus() == CardStatus.CLOSED) {
            throw new ApiException("CARD_ALREADY_CLOSED", "Card is already closed");
        }

        card.setStatus(CardStatus.CLOSED);
        card.setStatusReason(request.reason().trim());
        return CardResponse.from(cardRepository.save(card));
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException("CARD_NOT_FOUND", "Card not found"));
    }

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber.trim().replace(" ", "");
    }

    private String normalizeToken(String token) {
        return token.trim().toUpperCase();
    }
}
