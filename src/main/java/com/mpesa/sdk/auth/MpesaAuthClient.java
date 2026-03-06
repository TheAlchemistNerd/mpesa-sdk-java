package com.mpesa.sdk.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.config.MpesaSdkConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

/**
 * Client for M-Pesa OAuth2 authentication.
 */
@Slf4j
public class MpesaAuthClient {

    private final HttpClient httpClient;
    private final MpesaSdkConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MpesaAuthClient(HttpClient httpClient, MpesaSdkConfig config) {
        this.httpClient = httpClient;
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials"))
                .header("Authorization", "Basic " + auth)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Auth failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to obtain M-Pesa access token. Status: " + response.statusCode());
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
            if (body == null || !body.containsKey("access_token")) {
                throw new RuntimeException("Empty or invalid response from M-Pesa Auth API");
            }

            return (String) body.get("access_token");
        } catch (Exception e) {
            log.error("Failed to obtain M-Pesa access token", e);
            throw new RuntimeException("Failed to obtain M-Pesa access token", e);
        }
    }
}
