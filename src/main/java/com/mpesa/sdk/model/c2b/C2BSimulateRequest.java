package com.mpesa.sdk.model.c2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for C2B Transaction Simulation (Sandbox only).
 */
@Builder
public record C2BSimulateRequest(
        @JsonProperty("ShortCode") String shortCode,
        @JsonProperty("CommandID") String commandId, // CustomerPayBillOnline or CustomerBuyGoodsOnline
        @JsonProperty("Amount") Double amount,
        @JsonProperty("Msisdn") String msisdn,
        @JsonProperty("BillRefNumber") String billRefNumber) {
}
