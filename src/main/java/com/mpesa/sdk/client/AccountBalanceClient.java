package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.balance.AccountBalanceRequest;
import com.mpesa.sdk.model.balance.AccountBalanceResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for querying M-Pesa Account Balance.
 */
@Slf4j
public class AccountBalanceClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;

    public AccountBalanceClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
        this.config = config;
        this.authClient = authClient;
    }

    /**
     * Queries the account balance.
     *
     * @param remarks transaction remarks
     * @return the API response
     */
    public AccountBalanceResponse queryBalance(String remarks) {
        String securityCredential = MpesaSecurityUtil.generateSecurityCredential(
                config.initiatorPassword(),
                config.certificatePath());

        AccountBalanceRequest request = AccountBalanceRequest.builder()
                .initiator(config.initiatorName())
                .securityCredential(securityCredential)
                .commandId("AccountBalance")
                .partyA(config.shortcode())
                .identifierType("4") // 4 = Organization shortcode
                .remarks(remarks)
                .queueTimeOutUrl(config.callbackBaseUrl() + "/balance/timeout")
                .resultUrl(config.callbackBaseUrl() + "/balance/result")
                .build();

        String token = authClient.getAccessToken();

        try {
            return webClient.post()
                    .uri("/mpesa/accountbalance/v1/query")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AccountBalanceResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Account Balance query failed", e);
            throw new RuntimeException("Account Balance query failed", e);
        }
    }
}
