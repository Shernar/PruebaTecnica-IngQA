package com.bancolombia.evaluation.exception;

/**
 * Excepción cuando la validación de una cuenta falla.
 */
public class AccountValidationException extends BankingException {

    private final String field;

    public AccountValidationException(String field, String message) {
        super("VALIDATION_ERROR", message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
