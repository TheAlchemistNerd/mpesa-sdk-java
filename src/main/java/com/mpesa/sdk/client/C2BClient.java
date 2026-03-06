package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.c2b.C2BRegisterRequest;
import com.mpesa.sdk.model.c2b.C2BSimulateRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Client for C2B (Customer to Business) operations.
 */
@Slf4j
public class C2BClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public C2BClient(HttpClient httpClient, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Registers validation and confirmation URLs.
     *
     * @return the API response map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> registerUrls() {
        C2BRegisterRequest request = C2BRegisterRequest.builder()
                .shortCode(config.shortcode())
                .responseType("Completed")
                .confirmationUrl(config.callbackBaseUrl() + "/c2b/confirmation")
                .validationUrl(config.callbackBaseUrl() + "/c2b/validation")
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/c2b/v2/registerurl"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("C2B URL registration failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("C2B registration failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            log.error("C2B URL registration failed", e);
            throw new RuntimeException("C2B registration failed", e);
        }
    }

    /**
     * Simulates a C2B transaction (Sandbox only).
     *
     * @param amount        Transaction amount
     * @param msisdn        Customer phone (254...)
     * @param billRefNumber Bill reference / Account number
     * @param commandId     "CustomerPayBillOnline" or "CustomerBuyGoodsOnline"
     * @return the API response map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> simulate(Double amount, String msisdn, String billRefNumber, String commandId) {
        if (!config.isSandbox()) {
            throw new UnsupportedOperationException("C2B simulation is only available in sandbox");
        }

        C2BSimulateRequest request = C2BSimulateRequest.builder()
                .shortCode(config.shortcode())
                .commandId(commandId)
                .amount(amount)
                .msisdn(msisdn)
                .billRefNumber(billRefNumber)
                .build();

        String token = authClient.getAccessToken();

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/c2b/v2/simulate"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("C2B simulation failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("C2B simulation failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            log.error("C2B simulation failed", e);
            throw new RuntimeException("C2B simulation failed", e);
        }
    }
}
