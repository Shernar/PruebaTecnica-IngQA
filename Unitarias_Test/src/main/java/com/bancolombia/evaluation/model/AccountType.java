package com.bancolombia.evaluation.model;

/**
 * Tipos de cuenta disponibles en el sistema.
 */
public enum AccountType {
    SAVINGS("Cuenta de Ahorros", 0.01),
    CHECKING("Cuenta Corriente", 0.0),
    BUSINESS("Cuenta Empresarial", 0.005),
    PREMIUM("Cuenta Premium", 0.02);

    private final String description;
    private final double interestRate;

    AccountType(String description, double interestRate) {
        this.description = description;
        this.interestRate = interestRate;
    }

    public String getDescription() {
        return description;
    }

    public double getInterestRate() {
        return interestRate;
    }
}
