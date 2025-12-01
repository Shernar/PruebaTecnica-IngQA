package com.bancolombia.evaluation.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando se excede un límite de transacción.
 */
public class LimitExceededException extends BankingException {

    private final BigDecimal limit;
    private final BigDecimal attempted;

    public LimitExceededException(String limitType, BigDecimal limit, BigDecimal attempted) {
        super("LIMIT_EXCEEDED", 
              String.format("Límite %s excedido. Máximo: %s, Intentado: %s", 
                           limitType, limit, attempted));
        this.limit = limit;
        this.attempted = attempted;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getAttempted() {
        return attempted;
    }
}
