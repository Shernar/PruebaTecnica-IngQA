package com.bancolombia.evaluation.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando el saldo es insuficiente para una operación.
 */
public class InsufficientFundsException extends BankingException {

    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super("INSUFFICIENT_FUNDS", 
              String.format("Saldo insuficiente. Disponible: %s, Requerido: %s", 
                           currentBalance, requiredAmount));
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }
}
