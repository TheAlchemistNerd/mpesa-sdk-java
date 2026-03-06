package com.mpesa.sdk.config;

import lombok.Builder;

/**
 * Configuration for the M-Pesa SDK.
 */
@Builder
public record MpesaSdkConfig(
        String environment, // "sandbox" or "production"
        String baseUrl, // Optional override for testing
        String consumerKey,
        String consumerSecret,
        String shortcode,
        String passkey,
        String initiatorName,
        String initiatorPassword,
        String certificatePath,
        String callbackBaseUrl) {
    public String getBaseUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        return "sandbox".equalsIgnoreCase(environment)
                ? "https://sandbox.safaricom.co.ke"
                : "https://api.safaricom.co.ke";
    }

    public boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(environment);
    }
}
