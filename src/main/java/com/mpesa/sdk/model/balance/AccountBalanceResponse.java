package com.mpesa.sdk.model.balance;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Account Balance query.
 */
public record AccountBalanceResponse(
        @JsonProperty("ConversationID") String conversationId,
        @JsonProperty("OriginatorConversationID") String originatorConversationId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription) {
}
