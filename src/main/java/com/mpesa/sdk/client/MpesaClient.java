package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Unified entry point for the M-Pesa SDK.
 */
public class MpesaClient {

    private final StkPushClient stkPush;
    private final B2CClient b2c;
    private final C2BClient c2b;
    private final AccountBalanceClient balance;
    private final TransactionStatusClient status;
    private final ReversalClient reversal;

    public MpesaClient(MpesaSdkConfig config) {
        this(config, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public MpesaClient(MpesaSdkConfig config, HttpClient httpClient) {
        MpesaAuthClient authClient = new MpesaAuthClient(httpClient, config);

        this.stkPush = new StkPushClient(httpClient, config, authClient);
        this.b2c = new B2CClient(httpClient, config, authClient);
        this.c2b = new C2BClient(httpClient, config, authClient);
        this.balance = new AccountBalanceClient(httpClient, config, authClient);
        this.status = new TransactionStatusClient(httpClient, config, authClient);
        this.reversal = new ReversalClient(httpClient, config, authClient);
    }

    public StkPushClient stkPush() {
        return stkPush;
    }

    public B2CClient b2c() {
        return b2c;
    }

    public C2BClient c2b() {
        return c2b;
    }

    public AccountBalanceClient balance() {
        return balance;
    }

    public TransactionStatusClient status() {
        return status;
    }

    public ReversalClient reversal() {
        return reversal;
    }
}
