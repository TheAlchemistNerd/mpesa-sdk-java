# API Reference Guide

This document provides a detailed reference for all the classes, methods, and models available in the **M-Pesa Daraja SDK**.

## 1. MpesaClient

The main entry point to the SDK.

### Constructor
`MpesaClient(MpesaSdkConfig config)`
Initializes the SDK with the provided configuration.

### Methods
- `stkPush()`: Returns the `StkPushClient`.
- `b2c()`: Returns the `B2CClient`.
- `c2b()`: Returns the `C2BClient`.
- `balance()`: Returns the `AccountBalanceClient`.
- `status()`: Returns the `TransactionStatusClient`.
- `reversal()`: Returns the `ReversalClient`.

---

## 2. StkPushClient (Lipa Na M-Pesa Online)

Used to initiate payments from a customer's phone.

### `initiate(Double amount, String phoneNumber, String reference, String description)`
- **Parameters**: 
    - `amount`: The amount to be paid (KES).
    - `phoneNumber`: Customer phone (e.g., "254712345678").
    - `reference`: Unique reference for the account/order.
    - `description`: A brief description of the transaction.
- **Returns**: `StkPushResponse`
- **Exceptions**: `RuntimeException` on network or credential failure.

---

## 3. B2CClient (Business to Customer)

Used to send money to customers.

### `initiate(String commandId, Double amount, String phoneNumber, String remarks, String occasion)`
- **Parameters**:
    - `commandId`: Transaction type: `SalaryPayment`, `BusinessPayment`, or `PromotionPayment`.
    - `amount`: Amount to send (KES).
    - `phoneNumber`: Recipient phone number.
    - `remarks`: Remarks to be sent with the transaction.
    - `occasion`: (Optional) Occasion for the payment.
- **Returns**: `B2CResponse`

---

## 4. C2BClient (Customer to Business)

Provides methods for URL registration and simulation.

### `registerUrls()`
Registers the `validationUrl` and `confirmationUrl` with Safaricom.
- **Returns**: `Map<String, Object>` (Raw Daraja JSON response).

### `simulate(Double amount, String msisdn, String billRefNumber, String commandId)`
Simulates a C2B transaction (**Sandbox Only**).
- **Parameters**:
    - `amount`: Amount.
    - `msisdn`: Customer phone.
    - `billRefNumber`: Account number.
    - `commandId`: `CustomerPayBillOnline` or `CustomerBuyGoodsOnline`.
- **Returns**: `Map<String, Object>`

---

## 5. AccountBalanceClient

Proactively check your Paybill/B2C wallet balance.

### `queryBalance(String remarks)`
- **Parameters**:
    - `remarks`: Remarks for the query.
- **Returns**: `AccountBalanceResponse`

---

## 6. TransactionStatusClient

Query the state of any M-Pesa transaction.

### `queryStatus(String transactionId, String originalConversationId, String remarks)`
- **Parameters**:
    - `transactionId`: The M-Pesa Receipt Number.
    - `originalConversationId`: (Optional) The ID returned during the original initiation.
    - `remarks`: Query remarks.
- **Returns**: `TransactionStatusResponse`

---

## 7. ReversalClient

Request a reversal for a C2B transaction.

### `reverse(String transactionId, Double amount, String remarks)`
- **Parameters**:
    - `transactionId`: M-Pesa Receipt Number of the original transaction.
    - `amount`: Amount to reverse.
    - `remarks`: Reason for reversal.
- **Returns**: `ReversalResponse`

---

## 8. Models (Records)

All models are located in the `com.mpesa.sdk.model` package.

### `StkPushResponse`
| Field | Type | Description |
| :--- | :--- | :--- |
| `merchantRequestId` | String | Unique ID for the merchant request. |
| `checkoutRequestId` | String | Unique ID for the client request. |
| `responseCode` | String | `0` if successful initiation. |
| `responseDescription` | String | Description of the response. |
| `customerMessage` | String | Message for the customer. |

### `B2CResponse` / `AccountBalanceResponse` / `TransactionStatusResponse` / `ReversalResponse`
These share a standard Daraja acknowledgment structure:
- `conversationId`: Unique request tracking ID.
- `originatorConversationId`: Your tracking ID.
- `responseCode`: `0` for success.
- `responseDescription`: Status message.

---

## 9. MpesaSdkConfig

Immutable configuration object built using `MpesaSdkConfig.builder()`.

| Method | Description |
| :--- | :--- |
| `environment(String)` | `sandbox` or `production`. |
| `consumerKey(String)` | Your API Key. |
| `consumerSecret(String)` | Your API Secret. |
| `shortcode(String)` | Paybill or Till number. |
| `passkey(String)` | Lipa Na M-Pesa passkey (for STK). |
| `initiatorName(String)` | Organization initiator username. |
| `initiatorPassword(String)` | Raw initiator password. |
| `certificatePath(String)` | Path to the `.cer` file. |
| `callbackBaseUrl(String)` | Base URL for receiving webhooks. |
