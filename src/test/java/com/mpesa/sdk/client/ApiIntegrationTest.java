package com.mpesa.sdk.client;

import com.mpesa.sdk.config.MpesaSdkConfig;
import com.mpesa.sdk.model.stk.StkPushResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for M-Pesa API.
 * Uses .env file for credentials.
 */
class ApiIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiIntegrationTest.class);
    private MpesaClient mpesaClient;

    @BeforeEach
    void setUp() {
        Dotenv dotenv = Dotenv.load();
        
        MpesaSdkConfig config = MpesaSdkConfig.builder()
                .consumerKey(dotenv.get("MPESA_CONSUMER_KEY"))
                .consumerSecret(dotenv.get("MPESA_CONSUMER_SECRET"))
                .shortcode(dotenv.get("MPESA_SHORTCODE"))
                .passkey(dotenv.get("MPESA_PASSKEY"))
                .environment(dotenv.get("MPESA_ENVIRONMENT", "sandbox"))
                .callbackBaseUrl("https://example.com/callback")
                .build();

        mpesaClient = new MpesaClient(config);
    }

    @Test
    void testStkPush() {
        try {
            // Test with a sample STK push
            // Using a test phone number (e.g., 254708374149 or similar sandbox numbers)
            String testPhone = "254708374149"; 
            StkPushResponse response = mpesaClient.stkPush().initiate(1.0, testPhone, "TestTransaction", "Payment for Goods");
            
            logger.info("STK Push Response: {}", response);
            assertNotNull(response);
        } catch (Exception e) {
            logger.error("STK Push failed", e);
            // We don't fail the test strictly here if it's a connectivity issue or sandbox downtime, 
            // but for a true integration test, we want to see it pass.
            throw e;
        }
    }
}
