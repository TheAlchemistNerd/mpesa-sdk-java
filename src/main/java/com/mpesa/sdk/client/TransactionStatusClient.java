package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.status.TransactionStatusRequest;
import com.mpesa.sdk.model.status.TransactionStatusResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for querying M-Pesa Transaction Status.
 */
@Slf4j
public class TransactionStatusClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public TransactionStatusClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config,
            MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
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
            return webClient.post()
                    .uri("/mpesa/transactionstatus/v1/query")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TransactionStatusResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Transaction Status query failed for {}", transactionId, e);
            throw new RuntimeException("Transaction Status query failed", e);
        }
    }
}
