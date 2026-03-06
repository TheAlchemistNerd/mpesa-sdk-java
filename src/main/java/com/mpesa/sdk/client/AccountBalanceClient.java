package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.balance.AccountBalanceRequest;
import com.mpesa.sdk.model.balance.AccountBalanceResponse;
import com.mpesa.sdk.security.MpesaSecurityUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client for querying M-Pesa Account Balance.
 */
@Slf4j
public class AccountBalanceClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final MpesaAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccountBalanceClient(HttpClient httpClient, MpesaSdkConfig config, MpesaAuthClient authClient) {
        this.httpClient = httpClient;
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
            String jsonPayload = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/mpesa/accountbalance/v1/query"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Account Balance query failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Account Balance query failed. Status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), AccountBalanceResponse.class);
        } catch (Exception e) {
            log.error("Account Balance query failed", e);
            throw new RuntimeException("Account Balance query failed", e);
        }
    }
}
