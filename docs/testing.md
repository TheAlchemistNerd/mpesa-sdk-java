# Testing Your M-Pesa Integration

Testing is vital when dealing with financial APIs. The **M-Pesa Daraja SDK** provides tools and follows patterns that make testing easy and reliable.

## 1. Unit Testing with MockWebServer

You should never hit the real Safaricom API in your unit tests. Instead, use `MockWebServer` to simulate the API behavior.

### Setting Up a Mock Test

```java
class MyPaymentTest {
    private MockWebServer mockWebServer;
    private MpesaClient mpesaClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        MpesaSdkConfig config = MpesaSdkConfig.builder()
            .environment("sandbox")
            .baseUrl(mockWebServer.url("/").toString()) // Important!
            .consumerKey("key")
            .consumerSecret("secret")
            .build();

        mpesaClient = new MpesaClient(config);
    }

    @Test
    void testStkPushSuccess() throws Exception {
        // Enqueue a successful response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"ResponseCode\":\"0\", \"ResponseDescription\":\"Success\"}")
            .addHeader("Content-Type", "application/json"));

        StkPushResponse res = mpesaClient.stkPush().initiate(10.0, "254...", "Ref", "Desc");

        assertEquals("0", res.responseCode());
    }
}
```

## 2. Using the Daraja Sandbox

Safaricom provides a sandbox environment for manual integration testing.

-   **Test Phone Numbers**: Use the numbers provided in the "Test Credentials" page.
-   **Test PIN**: Usually `1234`.
-   **Latency Simulation**: The sandbox can be slower than production. This is useful for testing your application's timeout logic.

## 3. Testing Callback Handling

Testing how your server handles callbacks from M-Pesa can be tricky because your local machine isn't reachable by Safaricom.

### Use an HTTP Tunnel
Tools like **ngrok** are perfect for this.
1.  Run your local server (e.g., on port 8080).
2.  Start ngrok: `ngrok http 8080`.
3.  Copy the HTTPS URL (e.g., `https://xyz.ngrok-free.app`).
4.  Set your `callbackBaseUrl` in the SDK config to this URL.
5.  Perform a transaction in sandbox; Safaricom will now be able to reach your local controller!

## 4. Integration Test Suites

We recommend writing integration tests that verify:
-   **Auth Token Cache**: Ensure your application doesn't fetch a new token for every request if you've implemented caching.
-   **Security Credential Rotation**: Test that your application can swap out the `.cer` file without needing a code rebuild.
-   **Database Consistency**: Verify that if a callback reports failure, your database record correctly reflects the "FAILED" state.

## 5. Performance Testing

Since the SDK uses the native Java `HttpClient`, you can performance test it using tools like **JMeter** or **Gatling**.
-   Monitor CPU and Memory usage.
-   Verify that the non-blocking nature allows your application to handle many simultaneous STK push initiations without exhausting the thread pool.

---
*Proper testing ensures that when you finalmente "Go Live," your payment system is stable and prepared for real-world traffic.*
