package com.mpesa.sdk.model.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for Transaction Status query.
 */
@Builder
public record TransactionStatusRequest(
        @JsonProperty("Initiator") String initiator,
        @JsonProperty("SecurityCredential") String securityCredential,
        @JsonProperty("CommandID") String commandId,
        @JsonProperty("TransactionID") String transactionId,
        @JsonProperty("PartyA") String partyA,
        @JsonProperty("IdentifierType") String identifierType,
        @JsonProperty("ResultURL") String resultUrl,
        @JsonProperty("QueueTimeOutURL") String queueTimeOutUrl,
        @JsonProperty("Remarks") String remarks,
        @JsonProperty("Occasion") String occasion,
        @JsonProperty("OriginalConversationID") String originalConversationId) {
}
