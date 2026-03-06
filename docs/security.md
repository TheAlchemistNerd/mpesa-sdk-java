# Security in the M-Pesa Daraja SDK

Security is the foundation of any financial integration. The **M-Pesa Daraja SDK** implements several layers of security to protect your sensitive credentials and ensure that every transaction is authenticated and encrypted according to Safaricom’s strict standards.

## 1. Authentication: OAuth 2.0 Flow

The Daraja API uses OAuth 2.0 for all transaction requests.

### Token Generation
The `MpesaAuthClient` is responsible for generating the temporary Bearer token. This is done by sending a `GET` request to the `/oauth/v1/generate` endpoint.
-   **Credentials**: Your `consumerKey` and `consumerSecret`.
-   **Authorization Header**: A `Basic` auth header containing the Base64-encoded string of `key:secret`.
-   **Token Lifespan**: Tokens typically expire in **3600 seconds (1 hour)**.

### Token Caching (Best Practices)
While the current version of the SDK provides the mechanism to fetch tokens, it is highly recommended to implement a caching layer in your application (e.g., using Redis or an in-memory `ConcurrentHashMap`) to avoid calling the Daraja Auth API for every single transaction. This reduces latency and prevents being throttled by Safaricom.

## 2. Security Credentials & RSA Encryption

Certain APIs like **B2C**, **Transaction Status**, and **Reversal** require an encrypted `SecurityCredential`. This is not just a password; it is your initiator password encrypted with Safaricom’s public key.

### How it Works
1.  **Certificate Access**: You must download the official Safaricom certificate (`.cer` file).
2.  **RSA Encryption**: The SDK uses the `RSA/ECB/PKCS1Padding` algorithm.
3.  **Encoding**: The raw encrypted bytes are Base64-encoded.

### Implementation Detail
The `MpesaSecurityUtil` class handles this process automatically:

```java
// Logic inside the SDK
public static String generateSecurityCredential(String password, String certificatePath) {
    InputStream certStream = Files.newInputStream(Paths.get(certificatePath));
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate) cf.generateCertificate(certStream);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
    byte[] encrypted = cipher.doFinal(password.getBytes());

    return Base64.getEncoder().encodeToString(encrypted);
}
```

## 3. SSL/TLS Requirements

Safaricom requires all outgoing requests to use **TLS 1.2 or higher**. Since Java 17 (the minimum version for this SDK) defaults to TLS 1.3/1.2, you are secured by default.

### Callback Security (HTTPS)
Your callback URLs **must** be exposed via HTTPS. Safaricom will not send results to plain HTTP endpoints.
-   **Valid Certificates**: Ensure your server uses a certificate issued by a recognized Certificate Authority (CA). Let's Encrypt is fully supported.
-   **Firewall Rules**: It is highly recommended to whitelist Safaricom's IP ranges (found in the Daraja portal docs) to ensure that only legitimate M-Pesa results reach your callback endpoints.

## 4. Sensitive Data Handling

### Environment Variables
**Never hardcode credentials in your source code.** Use environment variables or a secret management service.

```java
// BAD
.consumerKey("ABC123XYZ")

// GOOD
.consumerKey(System.getenv("MPESA_KEY"))
```

### Logging
The SDK uses SLF4J for logging. While we log transaction IDs and response descriptions, **we never log sensitive fields like `Passkey`, `InitiatorPassword`, or the raw `SecurityCredential`**. Ensure your own application logs also follow this practice.

## 5. Security Checklist for Production

Before going live, verify the following:

- [ ] **Production Key/Secret**: You are using credentials from a "Production" app on the Daraja portal.
- [ ] **Production Certificate**: You have replaced the `SandboxCertificate.cer` with the production public key.
- [ ] **HTTPS Callbacks**: All your `ResultURL` and `QueueTimeOutURL` endpoints are secure.
- [ ] **Waitlist/Whitelisting**: If your server is behind a strict firewall, ensure Safaricom IPs are allowed.
- [ ] **Initiator Access**: Ensure the initiator user has the correct permissions (e.g., "Manage B2C") in the M-Pesa portal.

---
*By following these security protocols, the M-Pesa Daraja SDK ensures that your fintech operations remain safe and compliant.*
