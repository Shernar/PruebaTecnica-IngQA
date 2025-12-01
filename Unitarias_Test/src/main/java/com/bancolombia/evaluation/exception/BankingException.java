package com.bancolombia.evaluation.exception;

/**
 * Excepción base para errores del dominio bancario.
 */
public class BankingException extends RuntimeException {

    private final String errorCode;

    public BankingException(String message) {
        super(message);
        this.errorCode = "BANKING_ERROR";
    }

    public BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
