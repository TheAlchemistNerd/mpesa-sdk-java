package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.b2c.B2CRequest;
import com.mpesa.sdk.model.b2c.B2CResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;

/**
 * Client for Business to Customer (B2C) payments.
 */
@Slf4j
public class B2CClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public B2CClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
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
            return webClient.post()
                    .uri("/mpesa/b2c/v3/paymentrequest")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(B2CResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("B2C initiation failed", e);
            throw new RuntimeException("B2C payment failed", e);
        }
    }
}
