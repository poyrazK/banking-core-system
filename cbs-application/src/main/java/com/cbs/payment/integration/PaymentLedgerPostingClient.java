package com.cbs.payment.integration;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.model.Payment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PaymentLedgerPostingClient implements LedgerPostingClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean postingEnabled;
    private final String ledgerBaseUrl;

    public PaymentLedgerPostingClient(RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${ledger.posting.enabled:true}") boolean postingEnabled,
            @Value("${ledger.posting.base-url:http://localhost:8088}") String ledgerBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.postingEnabled = postingEnabled;
        this.ledgerBaseUrl = ledgerBaseUrl;
    }

    @Override
    public void postPayment(Payment payment) {
        if (!postingEnabled) {
            return;
        }

        LedgerPolicyPostRequest request = new LedgerPolicyPostRequest(
                payment.getReference(),
                payment.getDescription(),
                payment.getValueDate(),
                "PAYMENT",
                payment.getAmount(),
                payment.getSourceAccountId().toString(),
                payment.getDestinationAccountId() == null ? null : payment.getDestinationAccountId().toString());

        try {
            restClient.post()
                    .uri(ledgerBaseUrl + "/api/v1/ledger/entries/policy")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ApiException("LEDGER_POSTING_FAILED", extractMessage(exception.getResponseBodyAsString()));
        } catch (Exception exception) {
            throw new ApiException("LEDGER_POSTING_FAILED", "Ledger posting failed: " + exception.getMessage());
        }
    }

    private String extractMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Ledger posting failed";
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.get("message");
            if (messageNode != null && !messageNode.isNull()) {
                return "Ledger posting failed: " + messageNode.asText();
            }
        } catch (Exception exception) {
            return "Ledger posting failed";
        }

        return "Ledger posting failed";
    }
}
