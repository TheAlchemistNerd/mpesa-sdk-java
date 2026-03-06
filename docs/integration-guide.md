# Integration Guide: Connecting the SDK to Your Application

This guide provides best practices for integrating the **M-Pesa Daraja SDK** into your existing Java or Spring Boot application.

## 1. Spring Boot Integration

The SDK is designed to be easily integrated into Spring Boot applications by exposing the `MpesaClient` as a bean.

### Step A: Define a Configuration Bean

Instead of manual instantiation in every service, define the client once in a configuration class.

```java
@Configuration
public class MpesaConfig {

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Bean
    public MpesaClient mpesaClient() {
        MpesaSdkConfig config = MpesaSdkConfig.builder()
            .environment("sandbox") // or "production"
            .consumerKey(consumerKey)
            .consumerSecret(consumerSecret)
            .shortcode(shortcode)
            .passkey(passkey)
            .initiatorName("testapi")
            .initiatorPassword("test_password")
            .certificatePath("certs/SandboxCertificate.cer")
            .callbackBaseUrl("https://api.myservice.com/mpesa")
            .build();

        return new MpesaClient(config);
    }
}
```

### Step B: Inject and Use

Inject the `MpesaClient` into your services. Spring handles the lifecycle and wiring automatically.

```java
@Service
public class PaymentService {

    private final MpesaClient mpesa;

    @Autowired
    public PaymentService(MpesaClient mpesa) {
        this.mpesa = mpesa;
    }

    public void checkout(String phone, Double amount) {
        StkPushResponse res = mpesa.stkPush().initiate(amount, phone, "REF-1", "Desc");
        // Logic for handling response
    }
}
```

## 2. Advanced Configuration: Customizing HttpClient

The SDK uses the native Java `HttpClient` (introduced in Java 11). For high-scale or specialized production environments, you might need to customize the underlying HTTP engine (timeouts, proxy settings, etc.).

The `MpesaClient` allows you to inject your own `HttpClient` instance.

```java
@Bean
public MpesaClient mpesaClient() {
    // Custom native HttpClient
    HttpClient customHttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    MpesaSdkConfig config = ... // load config
    return new MpesaClient(config, customHttpClient);
}
```

---

## 3. Handling Callbacks (Webhooks)

Callbacks are critical for knowing the final result of an asynchronous transaction (like STK Push or B2C).

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
In a Spring Boot environment, you can handle these with a standard `@RestController`.

```java
@RestController
@RequestMapping("/mpesa")
public class MpesaCallbackController {

    @PostMapping("/stk/callback")
    public ResponseEntity<String> handleStk(@RequestBody String payload) {
        // 1. Log the payload for audit trails
        // 2. Parse payload using Jackson
        // 3. Update transaction status in DB based on ResultCode
        return ResponseEntity.ok("Success");
    }
}
```

## 4. Idempotency and Reliability

Financial systems must be idempotent—processing the same request twice should not result in two payments.

1.  **Unique References**: Always provide a unique `accountReference` for STK Push.
2.  **Tracking IDs**: Store the `CheckoutRequestID` (STK) or `ConversationID` (B2C) as soon as you receive the synchronous response.
3.  **Handling Delayed Callbacks**: If no callback is received within a reasonable window, use the `TransactionStatusClient` (for B2C/C2B) or query specific statuses manually.

## 5. Production Deployment Considerations

### Certificate Management
When deploying to **Docker** or **Kubernetes**:
-   Mount the `.cer` file as a **Secret**.
-   Ensure the `certificatePath` in your configuration points to the absolute path where the secret is mounted.

### Resource Tuning
The native `HttpClient` is efficient, but ensure your server has appropriate connection limits and resource allocation for high-traffic scenarios.

---
*For a complete example, refer to the unit tests in the `src/test` directory.*
