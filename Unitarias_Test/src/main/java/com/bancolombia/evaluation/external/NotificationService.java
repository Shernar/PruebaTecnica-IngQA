package com.bancolombia.evaluation.external;

import com.bancolombia.evaluation.model.Transaction;

/**
 * Servicio externo para envío de notificaciones.
 */
public interface NotificationService {

    /**
     * Envía notificación por email.
     * @param email Dirección de email
     * @param subject Asunto
     * @param body Cuerpo del mensaje
     * @return true si se envió correctamente
     */
    boolean sendEmail(String email, String subject, String body);

    /**
     * Envía notificación por SMS.
     * @param phoneNumber Número de teléfono
     * @param message Mensaje
     * @return true si se envió correctamente
     */
    boolean sendSms(String phoneNumber, String message);

    /**
     * Envía notificación push.
     * @param userId ID del usuario
     * @param title Título
     * @param message Mensaje
     * @return true si se envió correctamente
     */
    boolean sendPushNotification(String userId, String title, String message);

    /**
     * Notifica sobre una transacción completada.
     * @param transaction Transacción completada
     */
    void notifyTransactionCompleted(Transaction transaction);
}
