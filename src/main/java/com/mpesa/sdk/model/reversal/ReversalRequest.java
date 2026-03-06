package com.mpesa.sdk.model.reversal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for Transaction Reversal.
 */
@Builder
public record ReversalRequest(
        @JsonProperty("Initiator") String initiator,
        @JsonProperty("SecurityCredential") String securityCredential,
        @JsonProperty("CommandID") String commandId,
        @JsonProperty("TransactionID") String transactionId,
        @JsonProperty("Amount") String amount,
        @JsonProperty("ReceiverParty") String receiverParty,
        @JsonProperty("RecieverIdentifierType") String receiverIdentifierType,
        @JsonProperty("ResultURL") String resultUrl,
        @JsonProperty("QueueTimeOutURL") String queueTimeOutUrl,
        @JsonProperty("Remarks") String remarks,
        @JsonProperty("Occasion") String occasion) {
}
