package com.mpesa.sdk.model.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for Account Balance query.
 */
@Builder
public record AccountBalanceRequest(
        @JsonProperty("Initiator") String initiator,
        @JsonProperty("SecurityCredential") String securityCredential,
        @JsonProperty("CommandID") String commandId,
        @JsonProperty("PartyA") String partyA,
        @JsonProperty("IdentifierType") String identifierType,
        @JsonProperty("Remarks") String remarks,
        @JsonProperty("QueueTimeOutURL") String queueTimeOutUrl,
        @JsonProperty("ResultURL") String resultUrl) {
}
