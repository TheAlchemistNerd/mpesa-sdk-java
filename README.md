# M-Pesa Daraja SDK (Java)

> **Exhaustive, Multi-module, Production-ready Java SDK for Safaricom's M-Pesa Daraja API.**

[![Maven Central](https://img.shields.io/maven-central/v/com.mpesa.sdk/mpesa-daraja-sdk.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mpesa.sdk%22%20AND%20a:%22mpesa-daraja-sdk%22)
[![Java Support](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📖 Introduction: The Future of M-Pesa Integrations

Integrating Safaricom’s M-Pesa Daraja API has historically been a challenging task for Java developers. From managing complex OAuth2 token lifecycles and RSA encryption for security credentials to correctly handling the nuances of STK Push, B2C, and C2B flows, the barrier to entry was high. Developers often found themselves writing thousands of lines of boilerplate code, managing low-level HTTP clients, and struggling with Safaricom's specific idiosyncratic API behaviors (like the famous "Reciever" typo in the reversal endpoint).

**This SDK changes everything.**

Designed from the ground up for modern Java (LTS 17), the **M-Pesa Daraja SDK** provides a granular, developer-friendly interface that abstracts away the complexities of the Daraja Gateway. It follows a **"Convention over Configuration"** philosophy, making it the perfect fit for Spring Boot applications where standard patterns and minimal boilerplate are prized.

### Why use this SDK?

1.  **Convention over Configuration**: Designed to "just work" in Spring environments. Standardized naming, predictable URL patterns, and easy `@Bean` integration mean you spend less time configuring and more time building.
2.  **Reactive & Async**: Built on Spring WebFlux's `WebClient`, the SDK supports high-concurrency non-blocking I/O, ensuring your application remains responsive even under heavy load.
3.  **Granular Responsibility**: Every service (STK, B2C, C2B, Reversal, Account Balance, Transaction Status) is isolated into its own client, following the Single Responsibility Principle.
4.  **Automated Security**: The SDK handles RSA encryption of initiator passwords using official Safaricom certificates automatically. You provide the `.cer` file; we do the rest.
5.  **Production-Ready Models**: All Request and Response objects are implemented as Java **records**, ensuring immutability and efficient JSON serialization via Jackson.
6.  **Simplified Error Handling**: Meaningful exceptions and structured logging help you debug issues in seconds rather than hours.
7.  **Built-in Testing**: The SDK is designed to be testable, featuring a suite of unit tests powered by `MockWebServer`.

---

## 🚀 Getting Started

### Prerequisites

To get started with the M-Pesa Daraja SDK, you will need:
-   **Java 17 or higher** (JDK 21 is also supported and recommended).
-   **Maven** (version 3.6+) or Gradle.
-   **Daraja Developer Account**: Access to the [Safaricom Daraja Portal](https://developer.safaricom.co.ke/) to obtain your Consumer Key and Consumer Secret.
-   **Sandbox/Production Certificates**: The public key certificate provided by Safaricom for the security credential encryption.

### Installation

Add the following dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.mpesa.sdk</groupId>
    <artifactId>mpesa-daraja-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

If you are using Gradle, add this to your `build.gradle`:

```gradle
implementation 'com.mpesa.sdk:mpesa-daraja-sdk:1.1.0'
```

---

## ⚙️ Configuration & Initialization

Core to the SDK is the `MpesaSdkConfig` object. This holds all the credentials, environment settings, and callback URLs required for the various Daraja services.

### Comprehensive Configuration Guide

The recommended way to instantiate the configuration is using the provided **fluent builder**.

```java
MpesaSdkConfig config = MpesaSdkConfig.builder()
    .environment("sandbox") // or "production"
    .consumerKey("your_consumer_key")
    .consumerSecret("your_consumer_secret")
    .shortcode("174379") // Your Paybill or Till number
    .passkey("your_lipa_na_mpesa_online_passkey")
    .initiatorName("testapi") // Used for SecurityCredential generation
    .initiatorPassword("test_password") // Used for SecurityCredential generation
    .certificatePath("certs/SandboxCertificate.cer") // Required for B2C, Status, Reversal
    .callbackBaseUrl("https://api.yourdomain.com/v1/mpesa")
    .build();
```

### Key Configuration Parameters

| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `environment` | String | Yes | `sandbox` for testing, `production` for live. |
| `baseUrl` | String | No | Overrides the default Safaricom base URL (useful for testing). |
| `consumerKey` | String | Yes | Your App's Consumer Key from the Daraja portal. |
| `consumerSecret` | String | Yes | Your App's Consumer Secret from the Daraja portal. |
| `shortcode` | String | Yes | The shortcode (Paybill/Till) used for transactions. |
| `passkey` | String | Yes | Required specifically for STK Push. |
| `initiatorName` | String | Yes | The username for the initiator used in the M-Pesa portal. |
| `initiatorPassword` | String | Yes | The password (not encrypted) for the initiator. |
| `certificatePath` | String | Yes | Absolute path to the `.cer` certificate file. |
| `callbackBaseUrl` | String | Yes | The base URL where M-Pesa will send transaction results. |

### Initializing the MpesaClient

Once the configuration is ready, the `MpesaClient` serves as the primary entry point to all SDK services.

```java
MpesaClient client = new MpesaClient(config);
```

---

## 💰 Core Payment Services

The SDK separates different Daraja functionalities into standalone sub-clients, accessible via the main `MpesaClient`.

### 1. Lipa Na M-Pesa Online (STK Push)

The STK Push service (Lipa Na M-Pesa Online) allows you to initiate an on-screen prompt on a customer’s phone, asking them to enter their M-Pesa PIN to complete a payment.

#### Implementation Flow

```java
StkPushResponse response = client.stkPush().initiate(
    1.0,                    // Amount (Double)
    "254708374149",         // Customer Phone Number (MSISDN format)
    "Invoice_12345",        // Account Reference (appears on customer's phone)
    "Payment for Goods"     // Transaction Description
);

if ("0".equals(response.responseCode())) {
    System.out.println("Push triggered successfully! CheckoutRequestID: " + response.checkoutRequestId());
} else {
    System.err.println("Failed to trigger push: " + response.responseDescription());
}
```

**What happens under the hood?**
-   The SDK automatically generates the `Timestamp` in the required format (`yyyyMMddHHmmss`).
-   It generates the `Password` by concatenating the Shortcode, Passkey, and Timestamp, and encoding the result in Base64.
-   It obtains a fresh OAuth2 token (or uses a cached one if implemented).
-   It constructs the full `CallBackURL` by appending `/stk/callback` to your `callbackBaseUrl`.

### 2. Business to Customer (B2C)

B2C allows a business to send money directly to a customer's M-Pesa wallet. Common use cases include salary disbursements, promotion payouts, and dividend distributions.

#### Implementation Flow

```java
B2CResponse response = client.b2c().initiate(
    "BusinessPayment",      // CommandID (SalaryPayment, BusinessPayment, PromotionPayment)
    10.50,                  // Amount
    "254705912645",         // Recipient Phone Number
    "Monthly Salary",       // Remarks
    "Payroll"               // Occasion (Optional)
);
```

**Security Consideration**: B2C requires a `SecurityCredential`. The SDK automatically reads your `initiatorPassword` and the certificate at `certificatePath` to perform the RSA encryption required by Safaricom.

---

## 🔄 Lifecycle & Transaction Management

Managing payments doesn't end when a request is sent. The SDK provides full-lifecycle tools to handle reversals, check statuses, and reconcile balances.

### 3. Transaction Reversal

Refunds and corrections are made via the Reversal service. **Note**: Safaricom only allows the reversal of C2B transactions via this API. B2C reversals are generally handled via the M-Pesa portal.

#### Implementation Flow

```java
ReversalResponse response = client.reversal().reverse(
    "NEF61H8J60",           // The M-Pesa Receipt Number / Transaction ID
    100.0,                  // Amount to reverse (Full amount is recommended)
    "Customer refund"       // Remarks
);
```

> **Developer Hint**: The SDK internally handles the notorious "RecieverIdentifierType" typo in the Daraja API protocol. You don't need to worry about the spelling; we've fixed it in the model layer.

### 4. Transaction Status Query

In cases where you do not receive a callback (due to network timeout or server downtime), the SDK allows you to proactively query the status of a specific transaction.

```java
TransactionStatusResponse response = client.status().queryStatus(
    "NEF61H8J60",           // Transaction ID
    null,                   // Original Conversation ID (optional)
    "Reconciliation query"  // Remarks
);
```

### 5. Account Balance Query

Useful for ensuring your B2C wallet has enough funds before attempting a payout or for periodic financial reporting.

```java
AccountBalanceResponse response = client.balance().queryBalance("Audit Check");
```

**Asynchronous Nature**: Like B2C and Status queries, the actual balance data is sent to your `ResultURL` via a callback. The synchronous response from this method only acknowledges that the request was accepted by Safaricom.

---

## 🔒 Security Deep Dive

Security is paramount when handling financial transactions. The SDK implements multiple layers of protection to ensure your credentials and data remain secure.

### RSA Encryption using Certificates

M-Pesa requires certain sensitive fields (like initiator passwords) to be encrypted using a public-key infrastructure.
-   **Certificates**: Safaricom provides `.cer` files (Sandbox and Production).
-   **Auto-Encryption**: The `MpesaSecurityUtil` class inside the SDK uses `Cipher.getInstance("RSA/ECB/PKCS1Padding")` to encrypt your raw password with the provided certificate's public key.
-   **SecurityCredential**: The resulting Base64-encoded string is passed as the `SecurityCredential` in API requests.

### OAuth 2.0 Token Management

All Daraja APIs (except the token generation endpoint itself) require an `Authorization: Bearer <token>` header.
-   **Token Generation**: The `MpesaAuthClient` uses your `consumerKey` and `consumerSecret` to generate tokens.
-   **Caching Strategy**: While the current SDK provides a robust `block()` implementation for ease of use, it is designed to be easily extensible for token caching (e.g., in Redis or an in-memory map) to avoid redundant network calls.

### 4. Advanced Configuration: Customizing WebClient

For enterprise environments, the SDK allows you to inject a pre-configured `WebClient.Builder`. This lets you control connection pools, timeouts, and logging at the infrastructure level.

```java
MpesaClient client = new MpesaClient(config, customWebClientBuilder);
```

---

## 📡 Handling Callbacks (Webhooks)

Daraja is an asynchronous API. While the initial request gives you a synchronous response, the final result of the transaction is sent to your server.

**Check the [Documentation: Callbacks](./docs/callbacks.md) for full JSON payload examples for every service.**

**Callback Base URL Logic**: If your `callbackBaseUrl` is `https://api.myapp.com/mpesa`, the SDK will automatically target:
-   `https://api.myapp.com/mpesa/stk/callback`
-   `https://api.myapp.com/mpesa/b2c/result`
-   `https://api.myapp.com/mpesa/c2b/confirmation`

---

## 🧪 Development and Testing

The SDK is built with test-driven development in mind. We use **MockWebServer** to simulate the Safaricom gateway responses, allowing you to run tests without actually hitting the live Daraja endpoints.

### Running the SDK Tests

To build the project and execute the full test suite:

```bash
mvn clean install
```

### Writing Your Own Integration Tests

You can mock the M-Pesa responses in your own application using `MockWebServer`:

```java
@Test
void testMyPaymentLogic() throws IOException {
    MockWebServer server = new MockWebServer();
    server.start();

    // Enqueue a mock response
    server.enqueue(new MockResponse()
        .setBody("{\"ResponseCode\":\"0\", \"ResponseDescription\":\"Success\"}")
        .addHeader("Content-Type", "application/json"));

    // Configure SDK to point to MockWebServer
    MpesaSdkConfig config = MpesaSdkConfig.builder()
        .baseUrl(server.url("/").toString())
        // ... rest of config
        .build();

    // Call your logic and assert
    assertNotNull(new MpesaClient(config).stkPush().initiate(...));

    server.shutdown();
}
```

---

## 🛠️ Advanced Usage & Best Practices

1.  **Environment Isolation**: Always use environment variables or a secure vault (like AWS Secrets Manager or HashiCorp Vault) for your `consumerKey` and `consumerSecret`. Never hardcode them in your source code.
2.  **Idempotency**: Use the `AccountReference` or `OriginatorConversationID` to track transactions in your system and prevent double-processing.
3.  **Logging**: The SDK uses **SLF4J**. Highly recommended to configure your logging framework (Logback/Log4j2) to capture logs from `com.mpesa.sdk`, especially during the integration phase.
4.  **Transaction Timeout**: Daraja callbacks can sometimes be delayed. Implement a retry or status-query mechanism that triggers if no callback is received within 1-2 minutes for STK Push.
5.  **Certificate Management**: Ensure your server has read access to the certificate path. If deploying to Docker, ensure the certificate file is correctly bind-mounted or included in the image.

---

## 🗺️ Roadmap

- [ ] **Spring Boot Starter**: Auto-configuration and easier injection of `MpesaClient`.
- [ ] **Redis Token Caching**: Out-of-the-box caching for OAuth2 tokens.
- [ ] **Native JSON Parsing**: Built-in callback listeners for easier webhook processing.
- [ ] **Kotlin DSL Support**: For even more expressive configuration.

---

## 🤝 Contribution

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## ⚖️ License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Support and Community

- **Issues**: Please use the GitHub [Issue Tracker](https://github.com/TheAlchemistNerd/rosca-saas-platform/issues) for bug reports and feature requests.
- **Discussions**: For general questions and architectural discussions, join our community forums.

---

*This SDK is maintained by Nevo and the deepmind team. We are committed to providing the most stable and feature-rich Java library for the M-Pesa ecosystem.*
