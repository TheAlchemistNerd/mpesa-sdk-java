package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.status.TransactionStatusResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStatusClientTest {

    private MockWebServer mockWebServer;
    private TransactionStatusClient statusClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        MpesaSdkConfig config = MpesaSdkConfig.builder()
                .environment("sandbox")
                .baseUrl(mockWebServer.url("/").toString())
                .consumerKey("test_key")
                .consumerSecret("test_secret")
                .shortcode("174379")
                .initiatorName("test_initiator")
                .initiatorPassword("test_password")
                .callbackBaseUrl("http://localhost:8080")
                .build();

        WebClient.Builder webClientBuilder = WebClient.builder();
        MpesaAuthClient authClient = new MpesaAuthClient(webClientBuilder, config) {
            @Override
            public String getAccessToken() {
                return "test_token";
            }
        };

        statusClient = new TransactionStatusClient(webClientBuilder, config, authClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testQueryStatusSuccess() throws Exception {
        TransactionStatusResponse expectedResponse = new TransactionStatusResponse(
                "12345", "67890", "0", "Accept the service request successfully.");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        TransactionStatusResponse actualResponse = statusClient.queryStatus("NEF61H8J60", "orig_conv_id", "Test query");

        assertNotNull(actualResponse);
        assertEquals("0", actualResponse.responseCode());
        assertEquals("12345", actualResponse.conversationId());
    }
}
