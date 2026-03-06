package com.mpesa.sdk.model.status;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Transaction Status query.
 */
public record TransactionStatusResponse(
        @JsonProperty("ConversationID") String conversationId,
        @JsonProperty("OriginatorConversationID") String originatorConversationId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription) {
}
