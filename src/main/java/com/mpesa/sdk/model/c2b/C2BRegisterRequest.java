package com.mpesa.sdk.model.c2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request model for C2B URL Registration.
 */
@Builder
public record C2BRegisterRequest(
        @JsonProperty("ShortCode") String shortCode,
        @JsonProperty("ResponseType") String responseType,
        @JsonProperty("ConfirmationURL") String confirmationUrl,
        @JsonProperty("ValidationURL") String validationUrl) {
}
