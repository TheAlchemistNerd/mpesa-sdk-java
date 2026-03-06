package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.reversal.ReversalRequest;
import com.mpesa.sdk.model.reversal.ReversalResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for reversing M-Pesa C2B transactions.
 */
@Slf4j
public class ReversalClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public ReversalClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
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
            return webClient.post()
                    .uri("/mpesa/reversal/v1/request")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ReversalResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Reversal failed for {}", transactionId, e);
            throw new RuntimeException("Reversal failed", e);
        }
    }
}
