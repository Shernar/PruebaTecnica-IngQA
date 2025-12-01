package com.bancolombia.evaluation.model;

import java.math.BigDecimal;

/**
 * Request para procesar un pago.
 */
public class PaymentRequest {

    private String sourceAccountNumber;
    private String merchantId;
    private String merchantName;
    private BigDecimal amount;
    private String currency;
    private String description;
    private PaymentMethod paymentMethod;
    private boolean requiresConfirmation;

    public PaymentRequest() {
        this.currency = "COP";
        this.requiresConfirmation = true;
    }

    public PaymentRequest(String sourceAccountNumber, String merchantId, 
                          BigDecimal amount, PaymentMethod paymentMethod) {
        this();
        this.sourceAccountNumber = sourceAccountNumber;
        this.merchantId = merchantId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // Getters y Setters
    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
}
