package com.mpesa.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.stk.StkPushResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StkPushClientTest {

    private MockWebServer mockWebServer;
    private StkPushClient stkPushClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        MpesaSdkConfig testConfig = MpesaSdkConfig.builder()
                .environment("sandbox")
                .baseUrl(mockWebServer.url("/").toString())
                .consumerKey("test_key")
                .consumerSecret("test_secret")
                .shortcode("174379")
                .passkey("test_passkey")
                .callbackBaseUrl("http://localhost:8080")
                .build();

        WebClient.Builder webClientBuilder = WebClient.builder();
        MpesaAuthClient authClient = new MpesaAuthClient(webClientBuilder, testConfig) {
            @Override
            public String getAccessToken() {
                return "test_token";
            }
        };

        stkPushClient = new StkPushClient(webClientBuilder, testConfig, authClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testInitiateSuccess() throws Exception {
        StkPushResponse expectedResponse = new StkPushResponse(
                "12345", "67890", "0", "Success", "Success");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        StkPushResponse actualResponse = stkPushClient.initiate(1.0, "254700000000", "Ref", "Desc");

        assertNotNull(actualResponse);
        assertEquals("0", actualResponse.responseCode());
        assertEquals("Success", actualResponse.responseDescription());
    }
}
