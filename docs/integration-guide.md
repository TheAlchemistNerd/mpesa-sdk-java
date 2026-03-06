# Integration Guide: Connecting the SDK to Your Application

This guide provides best practices for integrating the **M-Pesa Daraja SDK** into your existing Java or Spring Boot application.

## 1. Spring Boot Integration: Convention over Configuration

The SDK is designed with a **"Convention over Configuration"** philosophy. When used within a Spring Boot environment, you can get up and running with minimal boilerplate by following standard Spring patterns.

### Why "Convention over Configuration"?
-   **Standardized Naming**: All model fields use standard MPesa casing, mapped via Jackson.
-   **Predictable URL Patterns**: Callbacks are automatically derived from a single base URL.
-   **Bean-Ready**: The `MpesaClient` is designed to be easily exposed as a `@Bean`, allowing for dependency injection throughout your app.

### Step A: Define a Configuration Bean
Instead of manual instantiation in every service, define the client once.

```java
@Configuration
public class MpesaConfig {

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;
    
    // ... other fields

    @Bean
    public com.mpesa.sdk.client.MpesaClient mpesaClient() {
        MpesaSdkConfig config = MpesaSdkConfig.builder()
            .environment("sandbox")
            .consumerKey(consumerKey)
            .consumerSecret(consumerSecret)
            .shortcode(shortcode)
            .initiatorName("testapi")
            .initiatorPassword("Safaricom999!$")
            .certificatePath("certs/SandboxCertificate.cer")
            .callbackBaseUrl("https://api.myservice.com/mpesa")
            .build();

        return new com.mpesa.sdk.client.MpesaClient(config);
    }
}
```

### Step B: Inject and Use
Inject the `MpesaClient` into your services. Spring handles the lifecycle and wiring automatically.

```java
@Service
public class PaymentService {

    private final MpesaClient mpesa;

    public PaymentService(MpesaClient mpesa) {
        this.mpesa = mpesa;
    }

    public void checkout(String phone, Double amount) {
        StkPushResponse res = mpesa.stkPush().initiate(amount, phone, "REF-1", "Desc");
    }
}
```

## 2. Advanced Configuration: Customizing WebClient

For high-scale or specialized production environments, you might need to customize the underlying HTTP engine (timeouts, connection pooling, proxy settings).

The `MpesaClient` supports an optional `WebClient.Builder` constructor, allowing you to inject a pre-configured builder.

```java
@Bean
public MpesaClient mpesaClient(WebClient.Builder webClientBuilder) {
    // Customizing Netty timeouts
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofSeconds(10));

    webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));

    MpesaSdkConfig config = ... // load config
    return new MpesaClient(config, webClientBuilder);
}
```

---

## 3. Handling Callbacks (Webhooks)
...

Callbacks are the most critical part of the integration. This is how you know if a payment actually succeeded.

### Callback Endpoint Structure
Safaricom will POST a JSON payload to your specified URLs.

#### STK Push Body Example
```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "...",
      "CheckoutRequestID": "...",
      "ResultCode": 0,
      "ResultDesc": "The service request is processed successfully.",
      "CallbackMetadata": { "Item": [...] }
    }
  }
}
```

### Implementing the Controller
We recommend creating a specialized controller to handle these.

```java
@RestController
@RequestMapping("/mpesa")
public class MpesaCallbackController {

    @PostMapping("/stk/callback")
    public String handleStk(@RequestBody String payload) {
        // 1. Log the payload for audit trails
        // 2. Map to a JSON node or DTO
        // 3. Extract CheckoutRequestID
        // 4. Update transaction status in DB based on ResultCode
        return "Success";
    }
}
```

## 3. Idempotency and Reliability

Financial systems must be idempotent—processing the same request twice should not result in two payments.

1.  **Unique References**: Always provide a unique `accountReference` for STK Push.
2.  **Tracking IDs**: Store the `CheckoutRequestID` (STK) or `ConversationID` (B2C) as soon as you receive the synchronous response. Use this ID as a primary key or unique index in your transaction table.
3.  **Handling Delayed Callbacks**: If no callback is received within 2 minutes for an STK Push, use the `TransactionStatusClient` to query the status manually.

## 4. Production Deployment Considerations

### Certificate Management
When deploying to **Docker** or **Kubernetes**:
-   Mount the `.cer` file as a **ConfigMap** or **Secret**.
-   Ensure the environment variable `MPESA_CERT_PATH` points to the absolute path where the file is mounted.

### Resource Tuning
The SDK uses WebClient, which runs on Netty. Ensure your server has enough file descriptors if you expect high traffic.

## 5. C2B URL Registration

For C2B (Paybill/Till) to work, you MUST register your URLs once. You can do this via a simple script or a one-time startup task in your application.

```java
mpesa.c2b().registerUrls();
```

Safaricom will then send all customer payments (Paybill) to your `validationUrl` (where you can accept/reject the payment) and your `confirmationUrl` (where the final receipt is sent).

---
*For a complete example of a full Spring Boot integration, check the [examples/spring-boot-demo] folder.*
