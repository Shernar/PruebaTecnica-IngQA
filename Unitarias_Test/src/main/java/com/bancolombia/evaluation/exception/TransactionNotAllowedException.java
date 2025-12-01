package com.bancolombia.evaluation.exception;

/**
 * Excepción cuando una transacción no está permitida.
 */
public class TransactionNotAllowedException extends BankingException {

    public TransactionNotAllowedException(String message) {
        super("TRANSACTION_NOT_ALLOWED", message);
    }

    public TransactionNotAllowedException(String errorCode, String message) {
        super(errorCode, message);
    }
}
