package com.mpesa.sdk.client;

import com.mpesa.sdk.auth.MpesaAuthClient;
import com.mpesa.sdk.config.MpesaSdkConfig;
import org.springframework.web.reactive.function.client.WebClient;

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
        this(config, WebClient.builder());
    }

    public MpesaClient(MpesaSdkConfig config, WebClient.Builder webClientBuilder) {
        MpesaAuthClient authClient = new MpesaAuthClient(webClientBuilder, config);

        this.stkPush = new StkPushClient(webClientBuilder, config, authClient);
        this.b2c = new B2CClient(webClientBuilder, config, authClient);
        this.c2b = new C2BClient(webClientBuilder, config, authClient);
        this.balance = new AccountBalanceClient(webClientBuilder, config, authClient);
        this.status = new TransactionStatusClient(webClientBuilder, config, authClient);
        this.reversal = new ReversalClient(webClientBuilder, config, authClient);
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
