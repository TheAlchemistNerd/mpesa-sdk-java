package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.status.TransactionStatusRequest;
import com.mpesa.sdk.model.status.TransactionStatusResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client for querying M-Pesa Transaction Status.
 */
@Slf4j
public class TransactionStatusClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionStatusClient(HttpClient httpClient, MpesaSdkConfig config,
            MpesaAuthClient authClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Queries the status of a transaction.
     *
     * @param transactionId          the M-Pesa receipt number
     * @param originalConversationId optional original conversation ID
     * @param remarks                transaction remarks
     * @return the API response
     */
    public TransactionStatusResponse queryStatus(String transactionId, String originalConversationId, String remarks) {
        String securityCredential = MpesaSecurityUtil.generateSecurityCredential(
                config.initiatorPassword(),
                config.certificatePath());

        TransactionStatusRequest request = TransactionStatusRequest.builder()
                .initiator(config.initiatorName())
                .securityCredential(securityCredential)
                .commandId("TransactionStatusQuery")
                .transactionId(transactionId)
                .partyA(config.shortcode())
                .identifierType("4") // 4 = Organization shortcode
                .resultUrl(config.callbackBaseUrl() + "/status/result")
                .queueTimeOutUrl(config.callbackBaseUrl() + "/status/timeout")
                .remarks(remarks)
                .occasion("StatusCheck")
                .originalConversationId(originalConversationId)
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/transactionstatus/v1/query"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Transaction Status query failed for {} with status {}: {}", transactionId, response.statusCode(), response.body());
                throw new RuntimeException("Transaction Status query failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), TransactionStatusResponse.class);
        } catch (Exception e) {
            log.error("Transaction Status query failed for {}", transactionId, e);
            throw new RuntimeException("Transaction Status query failed", e);
        }
    }
}
