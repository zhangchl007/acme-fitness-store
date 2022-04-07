package com.vmware.acmepayment.response;

public class PaymentResponse {

    private String success;
    private String message;
    private String amount;
    private String transactionID;
    private String status;

    public String getSuccess() {
        return success;
    }

    public PaymentResponse(String success, String message, String amount, String transactionID, String status) {
        this.success = success;
        this.message = message;
        this.amount = amount;
        this.transactionID = transactionID;
        this.status = status;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
