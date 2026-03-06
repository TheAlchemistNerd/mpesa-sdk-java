package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.b2c.B2CRequest;
import com.mpesa.sdk.model.b2c.B2CResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Client for Business to Customer (B2C) payments.
 */
@Slf4j
public class B2CClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public B2CClient(HttpClient httpClient, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Initiates a B2C payment.
     *
     * @param commandId   Transaction type (SalaryPayment, BusinessPayment,
     *                    PromotionPayment)
     * @param amount      Transaction amount
     * @param phoneNumber Customer phone number (254...)
     * @param remarks     Transaction remarks
     * @param occassion   Optional occasion
     * @return the API response
     */
    public B2CResponse initiate(String commandId, Double amount, String phoneNumber, String remarks, String occassion) {
        String securityCredential = MpesaSecurityUtil.generateSecurityCredential(
                config.initiatorPassword(),
                config.certificatePath());

        B2CRequest request = B2CRequest.builder()
                .originatorConversationId(UUID.randomUUID().toString())
                .initiatorName(config.initiatorName())
                .securityCredential(securityCredential)
                .commandId(commandId)
                .amount(amount)
                .partyA(config.shortcode())
                .partyB(phoneNumber)
                .remarks(remarks)
                .queueTimeOutUrl(config.callbackBaseUrl() + "/b2c/timeout")
                .resultUrl(config.callbackBaseUrl() + "/b2c/result")
                .occassion(occassion)
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/b2c/v3/paymentrequest"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("B2C initiation failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("B2C payment failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), B2CResponse.class);
        } catch (Exception e) {
            log.error("B2C initiation failed", e);
            throw new RuntimeException("B2C payment failed", e);
        }
    }
}
