package com.mpesa.sdk.model.reversal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Transaction Reversal.
 */
public record ReversalResponse(
        @JsonProperty("ConversationID") String conversationId,
        @JsonProperty("OriginatorConversationID") String originatorConversationId,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription) {
}
