# Troubleshooting and Common Errors

Integrations with financial gateways often encounter "gotchas." This document lists common issues found when working with the Daraja API and how to resolve them using the SDK.

## 1. Common API Error Codes

| Code | Meaning | Solution |
| :--- | :--- | :--- |
| `400.002.02` | Invalid Request Payload | Check for missing fields or incorrect data types. The SDK models generally prevent this. |
| `401.002.01` | Invalid Access Token | Your Consumer Key/Secret are incorrect or the token has expired. |
| `404.001.03` | Invalid Service ID | You are calling an endpoint not available to your shortcode type. |
| `500.205.01` | System Down | Safaricom's internal system is experiencing issues. Retry with exponential backoff. |
| `1` | Insufficient Funds | (B2C specifically) Your B2C utility account has reached its limit. |

## 2. Certificate and Encryption Issues

### Error: "Invalid Security Credential"
This usually happens during B2C or Status queries.
-   **Wrong Certificate**: Check if you are using the `SandboxCertificate.cer` on the production environment.
-   **Initiator Password**: Ensure the password you are providing is the raw password, not an already encoded one.
-   **Initiator Name**: Must match exactly what is in the Daraja portal.

## 3. Callback (Webhook) Issues

### I'm not receiving any callbacks
-   **HTTPS**: Is your callback URL secure?
-   **Public Access**: Can your URL be reached from the open internet?
-   **Firewall**: Check your server logs; are you seeing incoming requests from Safaricom IPs?
-   **Callback Path**: Ensure you didn’t add a trailing slash or miss a path component (e.g., `/stk/callback`).

## 4. STK PushSpecific Problems

### Push does not appear on phone
-   **Phone Number**: Ensure it begins with `254`. Using `07...` or `+254...` will often fail.
-   **Busy Signal**: If the customer already has an active USSD session, the push might fail.
-   **Inactive Number**: The customer might have M-Pesa disabled or locked.

## 5. SDK Specific Troubleshooting

### Logging for Debugging
Enable detailed logs for the SDK in your `logback.xml` or `application.properties`:

```properties
logging.level.com.mpesa.sdk=DEBUG
```

This will reveal the raw request URLs and the error responses returned by Safaricom.

### HttpClient Timeouts
If you are getting `HttpTimeoutException`, you may need to increase the timeout in the `HttpClient` configuration within the `MpesaClient` class or provide a custom `HttpClient` with longer timeouts.

# Troubleshooting and Common Errors

Integrations with financial gateways often encounter "gotchas." This document lists common issues found when working with the Daraja API and how to resolve them using the SDK.

## 1. Common API Error Codes

| Code | Meaning | Solution |
| :--- | :--- | :--- |
| `400.002.02` | Invalid Request Payload | Check for missing fields or incorrect data types. The SDK models generally prevent this. |
| `401.002.01` | Invalid Access Token | Your Consumer Key/Secret are incorrect or the token has expired. |
| `404.001.03` | Invalid Service ID | You are calling an endpoint not available to your shortcode type. |
| `500.205.01` | System Down | Safaricom's internal system is experiencing issues. Retry with exponential backoff. |
| `1` | Insufficient Funds | (B2C specifically) Your B2C utility account has reached its limit. |

## 2. Certificate and Encryption Issues

### Error: "Invalid Security Credential"
This usually happens during B2C or Status queries.
-   **Wrong Certificate**: Check if you are using the `SandboxCertificate.cer` on the production environment.
-   **Initiator Password**: Ensure the password you are providing is the raw password, not an already encoded one.
-   **Initiator Name**: Must match exactly what is in the Daraja portal.

## 3. Callback (Webhook) Issues

### I'm not receiving any callbacks
-   **HTTPS**: Is your callback URL secure?
-   **Public Access**: Can your URL be reached from the open internet?
-   **Firewall**: Check your server logs; are you seeing incoming requests from Safaricom IPs?
-   **Callback Path**: Ensure you didn’t add a trailing slash or miss a path component (e.g., `/stk/callback`).

## 4. STK PushSpecific Problems

### Push does not appear on phone
-   **Phone Number**: Ensure it begins with `254`. Using `07...` or `+254...` will often fail.
-   **Busy Signal**: If the customer already has an active USSD session, the push might fail.
-   **Inactive Number**: The customer might have M-Pesa disabled or locked.

## 5. SDK Specific Troubleshooting

### Logging for Debugging
Enable detailed logs for the SDK in your `logback.xml` or `application.properties`:

```properties
logging.level.com.mpesa.sdk=DEBUG
```

This will reveal the raw request URLs and the error responses returned by Safaricom.

### HttpClient Timeouts
If you are getting `HttpTimeoutException`, you may need to increase the timeout in the `HttpClient` configuration within the `MpesaClient` class or provide a custom `HttpClient` with longer timeouts.

## 6. Known Daraja Quirks

### The "Reciever" Typo
As noted in our documentation, the Reversal API requires the field `RecieverIdentifierType` (missing 'v'). The SDK handles this, but if you are building your own models, keep this in mind.

### Sandbox Consistency
The sandbox environment is shared and can sometimes return unpredictable results. If a test fails once but passes the next time without code changes, it is likely a sandbox quirk.


---
*Still stuck? Open an issue on our GitHub repository with as much detail (and logs!) as possible.*
