package com.mpesa.sdk.auth;

import com.mpesa.sdk.config.MpesaSdkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.Map;

/**
 * Client for M-Pesa OAuth2 authentication.
 */
@Slf4j
public class MpesaAuthClient {

    private final WebClient webClient;
    private final MpesaSdkConfig config;

    public MpesaAuthClient(WebClient.Builder webClientBuilder, MpesaSdkConfig config) {
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
        this.config = config;
    }

    /**
     * Obtains a new access token from the Daraja API.
     *
     * @return the access token string
     */
    @SuppressWarnings("unchecked")
    public String getAccessToken() {
        String auth = Base64.getEncoder()
                .encodeToString((config.consumerKey() + ":" + config.consumerSecret()).getBytes());

        try {
            Map<String, Object> response = webClient.get()
                    .uri("/oauth/v1/generate?grant_type=client_credentials")
                    .header("Authorization", "Basic " + auth)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                throw new RuntimeException("Empty or invalid response from M-Pesa Auth API");
            }

            return (String) response.get("access_token");
        } catch (Exception e) {
            log.error("Failed to obtain M-Pesa access token", e);
            throw new RuntimeException("Failed to obtain M-Pesa access token", e);
        }
    }
}
