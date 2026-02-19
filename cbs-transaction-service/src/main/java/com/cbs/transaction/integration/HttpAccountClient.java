package com.cbs.transaction.integration;

import com.cbs.common.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HttpAccountClient implements AccountClient {

    private static final String ACCOUNT_LOOKUP_FAILED = "Account lookup failed";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String accountBaseUrl;

    public HttpAccountClient(RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${account.service.base-url:http://localhost:8083}") String accountBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.accountBaseUrl = accountBaseUrl;
    }

    @Override
    public String getAccountCurrency(Long accountId) {
        try {
            String response = restClient.get()
                    .uri(accountBaseUrl + "/api/v1/accounts/" + accountId + "/currency")
                    .retrieve()
                    .body(String.class);

            return extractCurrency(response);
        } catch (RestClientResponseException exception) {
            throw new ApiException(ACCOUNT_LOOKUP_FAILED, extractMessage(exception.getResponseBodyAsString()));
        } catch (Exception exception) {
            throw new ApiException(ACCOUNT_LOOKUP_FAILED, "Account lookup failed: " + exception.getMessage());
        }
    }

    private String extractCurrency(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.has("currency")) {
                return dataNode.get("currency").asText();
            }
        } catch (Exception exception) {
            throw new ApiException(ACCOUNT_LOOKUP_FAILED, "Failed to parse account currency response");
        }
        throw new ApiException(ACCOUNT_LOOKUP_FAILED, "Currency not found in account response");
    }

    private String extractMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return ACCOUNT_LOOKUP_FAILED;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.get("message");
            if (messageNode != null && !messageNode.isNull()) {
                return "Account lookup failed: " + messageNode.asText();
            }
        } catch (Exception exception) {
            return ACCOUNT_LOOKUP_FAILED;
        }

        return ACCOUNT_LOOKUP_FAILED;
    }
}
