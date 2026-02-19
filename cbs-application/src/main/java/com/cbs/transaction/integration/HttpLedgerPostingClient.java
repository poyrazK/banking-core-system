package com.cbs.transaction.integration;

import com.cbs.common.exception.ApiException;
import com.cbs.transaction.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpLedgerPostingClient implements LedgerPostingClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean postingEnabled;
    private final String ledgerBaseUrl;

    public HttpLedgerPostingClient(RestClient.Builder restClientBuilder,
                                   ObjectMapper objectMapper,
                                   @Value("${ledger.posting.enabled:true}") boolean postingEnabled,
                                   @Value("${ledger.posting.base-url:http://localhost:8088}") String ledgerBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.postingEnabled = postingEnabled;
        this.ledgerBaseUrl = ledgerBaseUrl;
    }

    @Override
    public void postTransaction(Transaction transaction) {
        if (!postingEnabled) {
            return;
        }

        LedgerPolicyPostRequest request = new LedgerPolicyPostRequest(
                transaction.getReference(),
                transaction.getDescription(),
                transaction.getValueDate(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getAccountId().toString(),
                transaction.getCounterpartyAccountId() == null ? null : transaction.getCounterpartyAccountId().toString()
        );

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
