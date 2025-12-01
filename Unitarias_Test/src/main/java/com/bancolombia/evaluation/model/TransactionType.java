package com.bancolombia.evaluation.model;

/**
 * Tipos de transacción disponibles.
 */
public enum TransactionType {
    DEPOSIT("Depósito", false),
    WITHDRAWAL("Retiro", true),
    TRANSFER("Transferencia", true),
    PAYMENT("Pago", true),
    REFUND("Reembolso", false);

    private final String description;
    private final boolean requiresBalance;

    TransactionType(String description, boolean requiresBalance) {
        this.description = description;
        this.requiresBalance = requiresBalance;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresBalance() {
        return requiresBalance;
    }
}
