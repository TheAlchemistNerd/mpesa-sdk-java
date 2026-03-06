package com.mpesa.sdk.model.b2c;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Business to Customer (B2C) payments.
 */
public record B2CResponse(
        @JsonProperty("ConversationID") String conversationId,
        @JsonProperty("OriginatorConversationID") String originatorConversationId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription) {
}
