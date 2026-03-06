package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.stk.StkPushRequest;
import com.mpesa.sdk.model.stk.StkPushResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Client for initiating Lipa Na M-Pesa Online (STK Push) transactions.
 */
@Slf4j
public class StkPushClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StkPushClient(HttpClient httpClient, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Initiates an STK push transaction.
     *
     * @param amount      the payment amount
     * @param phoneNumber customer phone number (254...)
     * @param reference   account reference
     * @param description transaction description
     * @return the API response
     */
    public StkPushResponse initiate(Double amount, String phoneNumber, String reference, String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = Base64.getEncoder().encodeToString(
                (config.shortcode() + config.passkey() + timestamp).getBytes());

        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode(config.shortcode())
                .password(password)
                .timestamp(timestamp)
                .transactionType("CustomerPayBillOnline")
                .amount(amount)
                .partyA(phoneNumber)
                .partyB(config.shortcode())
                .phoneNumber(phoneNumber)
                .callBackUrl(config.callbackBaseUrl() + "/stk/callback")
                .accountReference(reference)
                .transactionDesc(description)
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/stkpush/v1/processrequest"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("STK Push failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("STK Push failed. Status: " + response.statusCode() + ", Body: " + response.body());
            }

            return objectMapper.readValue(response.body(), StkPushResponse.class);
        } catch (Exception e) {
            log.error("STK Push initiation failed", e);
            throw new RuntimeException("STK Push failed", e);
        }
    }
}
