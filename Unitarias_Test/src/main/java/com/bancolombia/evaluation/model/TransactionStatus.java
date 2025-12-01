package com.bancolombia.evaluation.model;

/**
 * Estados posibles de una transacción.
 */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED
}
