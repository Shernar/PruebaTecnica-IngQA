package com.bancolombia.evaluation.external;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de auditoría para registro de operaciones.
 */
public interface AuditService {

    /**
     * Registra el inicio de una operación.
     * @param operationType Tipo de operación
     * @param accountNumber Número de cuenta
     * @param metadata Metadatos adicionales
     * @return ID del registro de auditoría
     */
    String logOperationStart(String operationType, String accountNumber, Map<String, Object> metadata);

    /**
     * Registra el fin exitoso de una operación.
     * @param auditId ID del registro de auditoría
     * @param result Resultado de la operación
     */
    void logOperationSuccess(String auditId, String result);

    /**
     * Registra el fallo de una operación.
     * @param auditId ID del registro de auditoría
     * @param errorCode Código de error
     * @param errorMessage Mensaje de error
     */
    void logOperationFailure(String auditId, String errorCode, String errorMessage);

    /**
     * Registra un cambio de saldo.
     * @param accountNumber Número de cuenta
     * @param previousBalance Saldo anterior
     * @param newBalance Nuevo saldo
     * @param transactionId ID de la transacción que causó el cambio
     */
    void logBalanceChange(String accountNumber, BigDecimal previousBalance, 
                          BigDecimal newBalance, String transactionId);
}
