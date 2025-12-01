package com.bancolombia.evaluation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado del procesamiento de un pago.
 */
public class PaymentResult {

    private String paymentId;
    private boolean successful;
    private String message;
    private String authorizationCode;
    private BigDecimal totalAmount;
    private BigDecimal commission;
    private LocalDateTime processedAt;
    private String errorCode;

    public PaymentResult() {
        this.processedAt = LocalDateTime.now();
    }

    public static PaymentResult success(String paymentId, String authorizationCode, 
                                         BigDecimal totalAmount, BigDecimal commission) {
        PaymentResult result = new PaymentResult();
        result.paymentId = paymentId;
        result.successful = true;
        result.message = "Pago procesado exitosamente";
        result.authorizationCode = authorizationCode;
        result.totalAmount = totalAmount;
        result.commission = commission;
        return result;
    }

    public static PaymentResult failure(String errorCode, String message) {
        PaymentResult result = new PaymentResult();
        result.successful = false;
        result.errorCode = errorCode;
        result.message = message;
        return result;
    }

    // Getters y Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
