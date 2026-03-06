package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.reversal.ReversalRequest;
import com.mpesa.sdk.model.reversal.ReversalResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client for reversing M-Pesa C2B transactions.
 */
@Slf4j
public class ReversalClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReversalClient(HttpClient httpClient, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Reverses a C2B transaction.
     *
     * @param transactionId the M-Pesa receipt number
     * @param amount        the amount to reverse
     * @param remarks       transaction remarks
     * @return the API response
     */
    public ReversalResponse reverse(String transactionId, Double amount, String remarks) {
        String securityCredential = MpesaSecurityUtil.generateSecurityCredential(
                config.initiatorPassword(),
                config.certificatePath());

        ReversalRequest request = ReversalRequest.builder()
                .initiator(config.initiatorName())
                .securityCredential(securityCredential)
                .commandId("TransactionReversal")
                .transactionId(transactionId)
                .amount(String.valueOf(amount.longValue()))
                .receiverParty(config.shortcode())
                .receiverIdentifierType("11") // 11 = Organization
                .resultUrl(config.callbackBaseUrl() + "/reversal/result")
                .queueTimeOutUrl(config.callbackBaseUrl() + "/reversal/timeout")
                .remarks(remarks)
                .occasion("Reversal")
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/reversal/v1/request"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Reversal failed for {} with status {}: {}", transactionId, response.statusCode(), response.body());
                throw new RuntimeException("Reversal failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), ReversalResponse.class);
        } catch (Exception e) {
            log.error("Reversal failed for {}", transactionId, e);
            throw new RuntimeException("Reversal failed", e);
        }
    }
}
