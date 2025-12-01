package com.bancolombia.evaluation.model;

import java.math.BigDecimal;

/**
 * Métodos de pago disponibles.
 */
public enum PaymentMethod {
    DEBIT_CARD("Tarjeta Débito", 0.0, new BigDecimal("5000000")),
    CREDIT_CARD("Tarjeta Crédito", 0.015, new BigDecimal("10000000")),
    BANK_TRANSFER("Transferencia", 0.0, new BigDecimal("50000000")),
    PSE("PSE", 0.0, new BigDecimal("20000000")),
    QR_CODE("Código QR", 0.0, new BigDecimal("2000000"));

    private final String description;
    private final double commission;
    private final java.math.BigDecimal maxAmount;

    PaymentMethod(String description, double commission, java.math.BigDecimal maxAmount) {
        this.description = description;
        this.commission = commission;
        this.maxAmount = maxAmount;
    }

    public String getDescription() {
        return description;
    }

    public double getCommission() {
        return commission;
    }

    public java.math.BigDecimal getMaxAmount() {
        return maxAmount;
    }
}
