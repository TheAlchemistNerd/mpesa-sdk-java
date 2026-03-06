package com.mpesa.sdk.model.stk;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Lipa Na M-Pesa Online (STK Push).
 */
public record StkPushResponse(
        @JsonProperty("MerchantRequestID") String merchantRequestId,
        @JsonProperty("CheckoutRequestID") String checkoutRequestId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription,
        @JsonProperty("CustomerMessage") String customerMessage) {
}
