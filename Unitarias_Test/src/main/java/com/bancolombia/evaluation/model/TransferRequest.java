package com.bancolombia.evaluation.model;

import java.math.BigDecimal;

/**
 * Request para procesar una transferencia entre cuentas.
 */
public class TransferRequest {

    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String description;
    private TransferType transferType;
    private boolean isScheduled;
    private String scheduledDate;

    public TransferRequest() {
        this.transferType = TransferType.SAME_BANK;
        this.isScheduled = false;
    }

    public TransferRequest(String sourceAccountNumber, String targetAccountNumber,
                           BigDecimal amount, TransferType transferType) {
        this();
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.transferType = transferType;
    }

    // Getters y Setters
    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
}
