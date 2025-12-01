package com.bancolombia.evaluation.model;

import java.math.BigDecimal;

/**
 * Tipos de transferencia disponibles.
 */
public enum TransferType {
    SAME_BANK("Mismo banco", BigDecimal.ZERO, new BigDecimal("100000000")),
    OTHER_BANK("Otro banco", new BigDecimal("5000"), new BigDecimal("50000000")),
    INTERNATIONAL("Internacional", new BigDecimal("50000"), new BigDecimal("20000000"));

    private final String description;
    private final BigDecimal fee;
    private final BigDecimal maxAmount;

    TransferType(String description, BigDecimal fee, BigDecimal maxAmount) {
        this.description = description;
        this.fee = fee;
        this.maxAmount = maxAmount;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
}
