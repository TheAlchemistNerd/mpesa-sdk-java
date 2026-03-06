# Getting Started with the M-Pesa Daraja SDK

Welcome to the comprehensive guide for getting started with the **M-Pesa Daraja SDK**. This document will walk you through the initial setup, installation, and first transaction using the library.

## Introduction

The M-Pesa Daraja SDK is a high-level wrapper around the Safaricom Daraja API 2.0. It is designed to provide a seamless developer experience for Java developers, abstracting away the complexities of security credential encryption, token management, and request/response modeling.

## 1. Prerequisites

Before you begin, ensure you have the following:

- **Java Development Kit (JDK) 17+**: The SDK leverages modern Java features like Records and the new HTTP client capabilities.
- **Maven or Gradle**: To manage your project dependencies.
- **Safaricom Developer Account**: Go to the [Daraja Portal](https://developer.safaricom.co.ke/), create an account, and set up a new application to get your:
    - **Consumer Key**
    - **Consumer Secret**
- **Test Credentials (Sandbox)**:
    - **Business Shortcode**: e.g., `174379` (Paybill).
    - **Lipa Na M-Pesa Passkey**: Found in the portal under the "Test Credentials" tab.
    - **Initiator Username/Password**: Also found in the test credentials section.
- **Public Key Certificate**: Download the `SandboxCertificate.cer` from the Daraja portal.

## 2. Installation

### Maven

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.mpesa.sdk</groupId>
    <artifactId>mpesa-daraja-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

Add this to your `build.gradle`:

```gradle
implementation 'com.mpesa.sdk:mpesa-daraja-sdk:1.1.0'
```

## 3. Configuration

The configuration is managed via the `MpesaSdkConfig` class. It's recommended to load these values from environment variables or a secure configuration file.

```java
import com.mpesa.sdk.config.MpesaSdkConfig;

MpesaSdkConfig config = MpesaSdkConfig.builder()
    .environment("sandbox")
    .consumerKey(System.getenv("MPESA_CONSUMER_KEY"))
    .consumerSecret(System.getenv("MPESA_CONSUMER_SECRET"))
    .shortcode("174379")
    .passkey("bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919")
    .initiatorName("testapi")
    .initiatorPassword("Safaricom999!$")
    .certificatePath("/path/to/SandboxCertificate.cer")
    .callbackBaseUrl("https://myapi.com/webhooks/mpesa")
    .build();
```

### Understanding the Environment Flag
- `sandbox`: Targets the Safaricom test environment. No real money is moved.
- `production`: Targets the live Safaricom gateway. Ensure you have went through the "Go Live" process on Daraja.

## 4. Initializing the Client

The `MpesaClient` is the main entry point for all operations.

```java
import com.mpesa.sdk.client.MpesaClient;

MpesaClient mpesa = new MpesaClient(config);
```

Once initialized, you can access the various services like `mpesa.stkPush()`, `mpesa.b2c()`, etc.

## 5. Your First Transaction: STK Push

STK Push is the easiest way to start. It prompts a customer to pay by entering their PIN on their phone.

```java
import com.mpesa.sdk.model.stk.StkPushResponse;

StkPushResponse response = mpesa.stkPush().initiate(
    10.0,               // Amount in KES
    "2547XXXXXXXX",     // Customer Phone Number
    "ORDER_001",        // Reference
    "Coffee Purchase"   // Description
);

if ("0".equals(response.responseCode())) {
    System.out.println("STK Push initiated! Waiting for customer PIN...");
}
```

## 6. What's Next?

Now that you have the basic setup running, you should explore:
- **[Architecture Guide](./architecture.md)**: Deep dive into how the SDK is structured.
- **[Integration Guide](./integration-guide.md)**: How to handle callbacks and integrate with your application.
- **[Security Guide](./security.md)**: Understanding encryption and token management.
- **[API Reference](./api-reference.md)**: Complete list of all clients and models.

## Troubleshooting Common Issues

### Certificate Not Found
Ensure the `certificatePath` is an absolute path or correctly relative to the working directory. If you are running in a container, make sure the file is mounted.

### Invalid Token
Double-check your `consumerKey` and `consumerSecret`. In sandbox, these are sometimes refreshed; ensure they match what's in the Daraja portal.

### Callback URL Issues
Safaricom requires your callback URL to be publicly accessible over HTTPS (with a valid, non-self-signed certificate). For local development, use tools like **ngrok** or **Localtunnel** to expose your local server.

---
*For more help, visit the project's GitHub issues page.*
