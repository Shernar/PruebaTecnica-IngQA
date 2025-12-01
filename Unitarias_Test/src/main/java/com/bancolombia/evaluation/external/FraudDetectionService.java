package com.bancolombia.evaluation.external;

import com.bancolombia.evaluation.model.PaymentRequest;
import com.bancolombia.evaluation.model.TransferRequest;

import java.math.BigDecimal;

/**
 * Servicio externo para detección de fraude.
 */
public interface FraudDetectionService {

    /**
     * Evalúa el riesgo de una transacción.
     * @param accountNumber Número de cuenta origen
     * @param amount Monto de la transacción
     * @return Puntuación de riesgo (0-100, donde 100 es máximo riesgo)
     */
    int evaluateTransactionRisk(String accountNumber, BigDecimal amount);

    /**
     * Verifica si una cuenta está en lista negra.
     * @param accountNumber Número de cuenta
     * @return true si está en lista negra
     */
    boolean isBlacklisted(String accountNumber);

    /**
     * Valida un pago contra reglas de fraude.
     * @param request Request del pago
     * @return true si el pago es seguro
     */
    boolean validatePayment(PaymentRequest request);

    /**
     * Valida una transferencia contra reglas de fraude.
     * @param request Request de la transferencia
     * @return true si la transferencia es segura
     */
    boolean validateTransfer(TransferRequest request);

    /**
     * Reporta una transacción sospechosa.
     * @param transactionId ID de la transacción
     * @param reason Razón de la sospecha
     */
    void reportSuspiciousActivity(String transactionId, String reason);
}
