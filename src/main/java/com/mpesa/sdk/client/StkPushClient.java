package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.stk.StkPushRequest;
import com.mpesa.sdk.model.stk.StkPushResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Client for initiating Lipa Na M-Pesa Online (STK Push) transactions.
 */
@Slf4j
public class StkPushClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public StkPushClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
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
            return webClient.post()
                    .uri("/mpesa/stkpush/v1/processrequest")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StkPushResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("STK Push initiation failed", e);
            throw new RuntimeException("STK Push failed", e);
        }
    }
}
