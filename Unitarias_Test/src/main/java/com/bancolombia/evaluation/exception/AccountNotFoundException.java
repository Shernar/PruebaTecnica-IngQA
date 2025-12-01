package com.bancolombia.evaluation.exception;

/**
 * Excepción cuando una cuenta no es encontrada.
 */
public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountNumber) {
        super("ACCOUNT_NOT_FOUND", "Cuenta no encontrada: " + accountNumber);
    }
}
