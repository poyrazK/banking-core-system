package com.cbs.card.controller;

import com.cbs.card.dto.CardResponse;
import com.cbs.card.dto.CardStatusReasonRequest;
import com.cbs.card.dto.CreateCardRequest;
import com.cbs.card.dto.UpdateCardLimitRequest;
import com.cbs.card.model.CardStatus;
import com.cbs.card.service.CardService;
import com.cbs.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cards", description = "Endpoints for debit/credit card management")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardResponse response = cardService.createCard(request);
        return ResponseEntity.ok(ApiResponse.success("Card created", response));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CardResponse>> getCard(@PathVariable("cardId") Long cardId) {
        CardResponse response = cardService.getCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardResponse>>> listCards(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "status", required = false) CardStatus status) {
        List<CardResponse> responses = cardService.listCards(customerId, accountId, status);
        return ResponseEntity.ok(ApiResponse.success("Cards retrieved", responses));
    }

    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<ApiResponse<CardResponse>> activateCard(@PathVariable("cardId") Long cardId) {
        CardResponse response = cardService.activateCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card activated", response));
    }

    @PatchMapping("/{cardId}/freeze")
    public ResponseEntity<ApiResponse<CardResponse>> freezeCard(
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody CardStatusReasonRequest request) {
        CardResponse response = cardService.freezeCard(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("Card frozen", response));
    }

    @PatchMapping("/{cardId}/block")
    public ResponseEntity<ApiResponse<CardResponse>> blockCard(
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody CardStatusReasonRequest request) {
        CardResponse response = cardService.blockCard(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("Card blocked", response));
    }

    @PatchMapping("/{cardId}/limits")
    public ResponseEntity<ApiResponse<CardResponse>> updateLimits(
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody UpdateCardLimitRequest request) {
        CardResponse response = cardService.updateLimits(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("Card limits updated", response));
    }

    @PatchMapping("/{cardId}/close")
    public ResponseEntity<ApiResponse<CardResponse>> closeCard(
            @PathVariable("cardId") Long cardId,
            @Valid @RequestBody CardStatusReasonRequest request) {
        CardResponse response = cardService.closeCard(cardId, request);
        return ResponseEntity.ok(ApiResponse.success("Card closed", response));
    }
}
