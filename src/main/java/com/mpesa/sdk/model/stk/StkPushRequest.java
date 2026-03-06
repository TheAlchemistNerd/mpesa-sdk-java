package com.mpesa.sdk.model.stk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for Lipa Na M-Pesa Online (STK Push).
 */
@Builder
public record StkPushRequest(
        @JsonProperty("BusinessShortCode") String businessShortCode,
        @JsonProperty("Password") String password,
        @JsonProperty("Timestamp") String timestamp,
        @JsonProperty("TransactionType") String transactionType,
        @JsonProperty("Amount") Double amount,
        @JsonProperty("PartyA") String partyA,
        @JsonProperty("PartyB") String partyB,
        @JsonProperty("PhoneNumber") String phoneNumber,
        @JsonProperty("CallBackURL") String callBackUrl,
        @JsonProperty("AccountReference") String accountReference,
        @JsonProperty("TransactionDesc") String transactionDesc) {
}
