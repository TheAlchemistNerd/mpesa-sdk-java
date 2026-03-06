package com.mpesa.sdk.model.b2c;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for Business to Customer (B2C) payments.
 */
@Builder
public record B2CRequest(
        @JsonProperty("OriginatorConversationID") String originatorConversationId,
        @JsonProperty("InitiatorName") String initiatorName,
        @JsonProperty("SecurityCredential") String securityCredential,
        @JsonProperty("CommandID") String commandId,
        @JsonProperty("Amount") Double amount,
        @JsonProperty("PartyA") String partyA,
        @JsonProperty("PartyB") String partyB,
        @JsonProperty("Remarks") String remarks,
        @JsonProperty("QueueTimeOutURL") String queueTimeOutUrl,
        @JsonProperty("ResultURL") String resultUrl,
        @JsonProperty("Occassion") String occassion) {
}
