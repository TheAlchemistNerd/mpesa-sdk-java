# M-Pesa Daraja SDK (Java)

> **Exhaustive, Lightweight, Production-ready Java SDK for Safaricom's M-Pesa Daraja API.**

[![Maven Central](https://img.shields.io/maven-central/v/io.github.TheAlchemistNerd/mpesa-daraja-sdk.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.TheAlchemistNerd%22%20AND%20a:%22mpesa-daraja-sdk%22)
[![Java Support](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📖 Introduction: The Future of M-Pesa Integrations

Integrating Safaricom’s M-Pesa Daraja API has historically been a challenging task for Java developers. From managing complex OAuth2 token lifecycles and RSA encryption for security credentials to correctly handling the nuances of STK Push, B2C, and C2B flows, the barrier to entry was high. Developers often found themselves writing thousands of lines of boilerplate code, managing low-level HTTP clients, and struggling with Safaricom's specific idiosyncratic API behaviors (like the famous "Reciever" typo in the reversal endpoint).

**This SDK changes everything.**

Designed from the ground up for modern Java (11+), the **M-Pesa Daraja SDK** provides a granular, developer-friendly interface that abstracts away the complexities of the Daraja Gateway. It is a **zero-external-networking-dependency** library, utilizing the native Java `HttpClient` for maximum portability and performance.

### Why use this SDK?

1.  **Zero External Networking Dependencies**: Built on the native Java `HttpClient` (Java 11+), this SDK is incredibly lightweight and won't conflict with your project's existing networking libraries.
2.  **Lightweight & Standalone**: No Spring or external reactive stack required. It works in any Java environment, from simple CLI tools to complex enterprise applications.
3.  **Granular Responsibility**: Every service (STK, B2C, C2B, Reversal, Account Balance, Transaction Status) is isolated into its own client, following the Single Responsibility Principle.
4.  **Automated Security**: The SDK handles RSA encryption of initiator passwords using official Safaricom certificates automatically. You provide the `.cer` file; we do the rest.
5.  **Modern Java Models**: All Request and Response objects are implemented as Java **records** (Java 16+), ensuring immutability and efficiency.
6.  **Simplified Error Handling**: Meaningful exceptions and structured logging help you debug issues in seconds rather than hours.
7.  **Built-in Testing**: The SDK is designed to be testable, featuring a suite of unit tests powered by `MockWebServer`.

---

## 🚀 Getting Started

### Prerequisites

To get started with the M-Pesa Daraja SDK, you will need:
-   **Java 11 or higher** (JDK 17 or higher recommended for Record support).
-   **Maven** (version 3.6+) or Gradle.
-   **Daraja Developer Account**: Access to the [Safaricom Daraja Portal](https://developer.safaricom.co.ke/) to obtain your Consumer Key and Consumer Secret.
-   **Sandbox/Production Certificates**: The public key certificate provided by Safaricom for the security credential encryption.

### Installation

Add the following dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.TheAlchemistNerd</groupId>
    <artifactId>mpesa-daraja-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

If you are using Gradle, add this to your `build.gradle`:

```gradle
implementation 'io.github.TheAlchemistNerd:mpesa-daraja-sdk:1.0.0'
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
| `certificatePath` | String | Yes | Path to the `.cer` certificate file. |
| `callbackBaseUrl` | String | Yes | The base URL where M-Pesa will send transaction results. |

### Initializing the MpesaClient

Once the configuration is ready, the `MpesaClient` serves as the primary entry point to all SDK services.

```java
MpesaClient client = new MpesaClient(config);
```

You can also provide your own `HttpClient` instance:

```java
HttpClient customClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(20))
    .build();
MpesaClient client = new MpesaClient(config, customClient);
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

---

## 🔄 Lifecycle & Transaction Management

Managing payments doesn't end when a request is sent. The SDK provides full-lifecycle tools to handle reversals, check statuses, and reconcile balances.

### 2. Business to Customer (B2C)

B2C allows a business to send money directly to a customer's M-Pesa wallet.

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

### 3. Transaction Reversal

Refunds and corrections are made via the Reversal service.

```java
ReversalResponse response = client.reversal().reverse(
    "NEF61H8J60",           // The M-Pesa Receipt Number / Transaction ID
    100.0,                  // Amount to reverse
    "Customer refund"       // Remarks
);
```

### 4. Transaction Status Query

In cases where you do not receive a callback, the SDK allows you to proactively query the status of a specific transaction.

```java
TransactionStatusResponse response = client.status().queryStatus(
    "NEF61H8J60",           // Transaction ID
    null,                   // Original Conversation ID (optional)
    "Reconciliation query"  // Remarks
);
```

---

## 🔒 Security Deep Dive

### RSA Encryption using Certificates

M-Pesa requires certain sensitive fields (like initiator passwords) to be encrypted using a public-key infrastructure.
-   **Certificates**: Safaricom provides `.cer` files (Sandbox and Production).
-   **Auto-Encryption**: The SDK uses `RSA/ECB/PKCS1Padding` to encrypt your raw password with the provided certificate's public key.
-   **SecurityCredential**: The resulting Base64-encoded string is passed as the `SecurityCredential` in API requests.

### OAuth 2.0 Token Management

All Daraja APIs require an `Authorization: Bearer <token>` header.
-   **Token Generation**: The `MpesaAuthClient` uses your `consumerKey` and `consumerSecret` to generate tokens.
-   **HttpClient Advantage**: Using the native `HttpClient`, the SDK provides efficient synchronous and asynchronous request handling without the overhead of heavy frameworks.

---

## 🧪 Development and Testing

The SDK is built with test-driven development in mind. We use **MockWebServer** to simulate the Safaricom gateway responses.

### Running the SDK Tests

To build the project and execute the full test suite:

```bash
mvn clean install
```

---

## 🛠️ Advanced Usage & Best Practices

1.  **Environment Isolation**: Always use environment variables for your `consumerKey` and `consumerSecret`.
2.  **Idempotency**: Use the `AccountReference` or `OriginatorConversationID` to track transactions.
3.  **Logging**: The SDK uses **SLF4J**. Configure your logging framework (Logback/Log4j2) to capture logs from `com.mpesa.sdk`.
4.  **Transaction Timeout**: implement a retry or status-query mechanism if no callback is received.

---

## 🗺️ Roadmap

- [ ] **Spring Boot Starter**: Auto-configuration and easier injection of `MpesaClient`.
- [ ] **Token Caching**: Built-in support for token caching.
- [ ] **Native JSON Parsing**: Callback listeners for easier webhook processing.

---

## ⚖️ License

Distributed under the MIT License. See `LICENSE` for more information.

---

*This SDK is maintained by Nevo and the deepmind team. We are committed to providing the most stable and lightweight Java library for the M-Pesa ecosystem.*
