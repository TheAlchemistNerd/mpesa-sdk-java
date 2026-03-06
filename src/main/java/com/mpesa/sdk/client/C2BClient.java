package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.c2b.C2BRegisterRequest;
import com.mpesa.sdk.model.c2b.C2BSimulateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

/**
 * Client for C2B (Customer to Business) operations.
 */
@Slf4j
public class C2BClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public C2BClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
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
            return webClient.post()
                    .uri("/mpesa/c2b/v2/registerurl")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
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
            return webClient.post()
                    .uri("/mpesa/c2b/v2/simulate")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("C2B simulation failed", e);
            throw new RuntimeException("C2B simulation failed", e);
        }
    }
}
