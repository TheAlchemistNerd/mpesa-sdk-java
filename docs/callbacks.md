# Handling M-Pesa Callbacks (Webhooks)

Daraja is an asynchronous API. While your initial request receives a synchronous acknowledgment, the final result of the transaction is delivered via an HTTP POST request to your server.

This document details the expected JSON structures for each service's callback.

## 1. Lipa Na M-Pesa Online (STK Push)

**Default URL**: `{callbackBaseUrl}/stk/callback`

### Success Payload
```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "29115-3462053-1",
      "CheckoutRequestID": "ws_CO_191220191020363925",
      "ResultCode": 0,
      "ResultDesc": "The service request is processed successfully.",
      "CallbackMetadata": {
        "Item": [
          { "Name": "Amount", "Value": 1.00 },
          { "Name": "MpesaReceiptNumber", "Value": "NLJ7RT6S9A" },
          { "Name": "TransactionDate", "Value": 20191219102115 },
          { "Name": "PhoneNumber", "Value": 254708374149 }
        ]
      }
    }
  }
}
```

### Common Failure Codes
- `1032`: Request cancelled by user.
- `1`: Insufficient funds.
- `2001`: Invalid PIN.

---

## 2. Business to Customer (B2C)

**Result URL**: `{callbackBaseUrl}/b2c/result`
**Timeout URL**: `{callbackBaseUrl}/b2c/timeout`

### Success Payload
```json
{
  "Result": {
    "ResultType": 0,
    "ResultCode": 0,
    "ResultDesc": "Process completed successfully",
    "OriginatorConversationID": "...",
    "ConversationID": "...",
    "TransactionID": "NLJ7RT6S9A",
    "ResultParameters": {
      "ResultParameter": [
        { "Key": "TransactionAmount", "Value": 100.0 },
        { "Key": "TransactionReceipt", "Value": "NLJ7RT6S9A" },
        { "Key": "B2CWorkingAccountAvailableFunds", "Value": 50000.0 },
        { "Key": "ReceiverPartyPublicName", "Value": "254700000000 - John Doe" }
      ]
    }
  }
}
```

---

## 3. Account Balance

**Result URL**: `{callbackBaseUrl}/balance/result`

### Balance Result Format
The balance is returned as a string in the `ResultParameters`:
`"AccountName|Currency|AvailableBalance|UnclearedBalance|ReservedFunds|TerminatedFunds"`

```json
{
  "Result": {
    "ResultCode": 0,
    "ResultParameters": {
      "ResultParameter": [
        { "Key": "AccountBalance", "Value": "Utility Account|KES|500.00|0.00|0.00|0.00" }
      ]
    }
  }
}
```

---

## 4. Transaction Status

**Result URL**: `{callbackBaseUrl}/status/result`

### Payload Details
Returns the full details of the transaction, including:
- `ReceiptNo`
- `ConversationID`
- `FinalisedTime`
- `Amount`
- `TransactionStatus` (e.g., "Completed")

---

## 5. Reversal

**Result URL**: `{callbackBaseUrl}/reversal/result`

### Payload Details
A successful reversal will return a new `TransactionID` for the reversal entry and confirm that the original transaction has been offset.

---

## 🛠 Best Practices for Callbacks

1.  **Idempotency**: Always check if you have already processed a `MpesaReceiptNumber` or `CheckoutRequestID` before updating your records.
2.  **Acknowledgment**: Your server MUST return an HTTP 200 OK response immediately. If you take too long to process (over 5 seconds), Safaricom might consider the callback failed and retry or time out.
3.  **Logging**: Store the raw JSON payload in an audit log. This is life-saving when reconciling disputed transactions.
4.  **Security**: Verify that the request came from Safaricom IP ranges.
