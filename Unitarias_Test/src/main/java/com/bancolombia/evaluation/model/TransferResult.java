package com.bancolombia.evaluation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado del procesamiento de una transferencia.
 */
public class TransferResult {

    private String transferId;
    private boolean successful;
    private String message;
    private String confirmationNumber;
    private BigDecimal transferredAmount;
    private BigDecimal fee;
    private BigDecimal sourceNewBalance;
    private BigDecimal targetNewBalance;
    private LocalDateTime processedAt;
    private String errorCode;

    public TransferResult() {
        this.processedAt = LocalDateTime.now();
    }

    public static TransferResult success(String transferId, String confirmationNumber,
                                          BigDecimal transferredAmount, BigDecimal fee,
                                          BigDecimal sourceNewBalance, BigDecimal targetNewBalance) {
        TransferResult result = new TransferResult();
        result.transferId = transferId;
        result.successful = true;
        result.message = "Transferencia realizada exitosamente";
        result.confirmationNumber = confirmationNumber;
        result.transferredAmount = transferredAmount;
        result.fee = fee;
        result.sourceNewBalance = sourceNewBalance;
        result.targetNewBalance = targetNewBalance;
        return result;
    }

    public static TransferResult failure(String errorCode, String message) {
        TransferResult result = new TransferResult();
        result.successful = false;
        result.errorCode = errorCode;
        result.message = message;
        return result;
    }

    // Getters y Setters
    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
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

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public BigDecimal getTransferredAmount() {
        return transferredAmount;
    }

    public void setTransferredAmount(BigDecimal transferredAmount) {
        this.transferredAmount = transferredAmount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getSourceNewBalance() {
        return sourceNewBalance;
    }

    public void setSourceNewBalance(BigDecimal sourceNewBalance) {
        this.sourceNewBalance = sourceNewBalance;
    }

    public BigDecimal getTargetNewBalance() {
        return targetNewBalance;
    }

    public void setTargetNewBalance(BigDecimal targetNewBalance) {
        this.targetNewBalance = targetNewBalance;
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
