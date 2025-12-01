package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.*;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.FraudDetectionService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * NIVEL 4: EXPERTO
 * 
 * Orquestador de transferencias entre cuentas con soporte para rollback.
 * 
 * Esta clase orquesta múltiples servicios y maneja transacciones distribuidas.
 * 
 * El candidato debe demostrar:
 * - Dominio completo de Mockito (spy, doReturn, doThrow, doAnswer)
 * - Manejo de escenarios de rollback y compensación
 * - @TestFactory para tests dinámicos
 * - Custom ArgumentMatcher
 * - Tests con timeout
 * - Verificación de comportamiento complejo
 * - Mock de métodos estáticos (opcional)
 * - Assertions personalizadas con AssertJ
 * - @RepeatedTest para tests de estabilidad
 */
public class TransferOrchestrator {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;
    private static final int FRAUD_CHECK_TIMEOUT_SECONDS = 5;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final AccountValidator accountValidator;

    public TransferOrchestrator(AccountRepository accountRepository,
                                TransactionRepository transactionRepository,
                                FraudDetectionService fraudDetectionService,
                                NotificationService notificationService,
                                AuditService auditService,
                                AccountValidator accountValidator) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.accountValidator = accountValidator;
    }

    /**
     * Ejecuta una transferencia entre cuentas con soporte completo de rollback.
     * 
     * El proceso sigue estos pasos:
     * 1. Validación de request
     * 2. Validación de cuentas (origen y destino)
     * 3. Verificación de fraude
     * 4. Débito de cuenta origen
     * 5. Crédito de cuenta destino
     * 6. Registro de transacción
     * 7. Notificaciones
     * 
     * Si cualquier paso falla después del débito, se ejecuta rollback automático.
     * 
     * @param request Solicitud de transferencia
     * @return Resultado de la transferencia
     */
    public TransferResult executeTransfer(TransferRequest request) {
        validateTransferRequest(request);

        String auditId = null;
        Account sourceAccount = null;
        Account targetAccount = null;
        BigDecimal originalSourceBalance = null;
        BigDecimal originalTargetBalance = null;
        boolean debitExecuted = false;
        boolean creditExecuted = false;

        try {
            // Obtener y validar cuentas
            sourceAccount = getAndValidateAccount(request.getSourceAccountNumber(), "origen");
            targetAccount = getAndValidateAccount(request.getTargetAccountNumber(), "destino");

            // Validar que no sea la misma cuenta
            if (sourceAccount.getAccountNumber().equals(targetAccount.getAccountNumber())) {
                throw new TransactionNotAllowedException(
                    "No se puede transferir a la misma cuenta");
            }

            // Validar que las cuentas pueden operar
            if (!accountValidator.canPerformOperations(sourceAccount)) {
                throw new TransactionNotAllowedException(
                    "La cuenta origen no puede realizar operaciones");
            }
            if (!accountValidator.canPerformOperations(targetAccount)) {
                throw new TransactionNotAllowedException(
                    "La cuenta destino no puede recibir transferencias");
            }

            // Calcular monto total con comisión
            BigDecimal fee = request.getTransferType().getFee();
            BigDecimal totalDebit = request.getAmount().add(fee);

            // Verificar saldo suficiente
            if (sourceAccount.getBalance().compareTo(totalDebit) < 0) {
                throw new InsufficientFundsException(sourceAccount.getBalance(), totalDebit);
            }

            // Validar límites del tipo de transferencia
            validateTransferLimits(request);

            // Verificación de fraude con timeout
            TransferFraudCheckResult fraudResult = performFraudCheck(request);
            if (!fraudResult.isApproved()) {
                return TransferResult.failure(fraudResult.getRejectCode(), 
                    fraudResult.getRejectReason());
            }

            // Iniciar auditoría
            Map<String, Object> metadata = createTransferMetadata(request, fraudResult);
            auditId = auditService.logOperationStart("TRANSFER", 
                request.getSourceAccountNumber(), metadata);

            // Guardar balances originales para posible rollback
            originalSourceBalance = sourceAccount.getBalance();
            originalTargetBalance = targetAccount.getBalance();

            // PASO CRÍTICO 1: Débito de cuenta origen
            sourceAccount.setBalance(originalSourceBalance.subtract(totalDebit));
            sourceAccount.setLastTransactionAt(LocalDateTime.now());
            accountRepository.save(sourceAccount);
            debitExecuted = true;

            // PASO CRÍTICO 2: Crédito de cuenta destino
            targetAccount.setBalance(originalTargetBalance.add(request.getAmount()));
            targetAccount.setLastTransactionAt(LocalDateTime.now());
            accountRepository.save(targetAccount);
            creditExecuted = true;

            // Crear y guardar transacción
            Transaction transaction = createTransferTransaction(request, fee);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Registrar cambios de saldo en auditoría
            auditService.logBalanceChange(sourceAccount.getAccountNumber(),
                originalSourceBalance, sourceAccount.getBalance(), 
                savedTransaction.getTransactionId());
            auditService.logBalanceChange(targetAccount.getAccountNumber(),
                originalTargetBalance, targetAccount.getBalance(), 
                savedTransaction.getTransactionId());

            auditService.logOperationSuccess(auditId, savedTransaction.getTransactionId());

            // Enviar notificaciones (no crítico, no afecta el resultado)
            sendTransferNotifications(sourceAccount, targetAccount, request, savedTransaction);

            return TransferResult.success(
                savedTransaction.getTransactionId(),
                generateConfirmationNumber(),
                request.getAmount(),
                fee,
                sourceAccount.getBalance(),
                targetAccount.getBalance()
            );

        } catch (Exception e) {
            // Ejecutar rollback si es necesario
            if (debitExecuted || creditExecuted) {
                executeRollback(sourceAccount, targetAccount, 
                    originalSourceBalance, originalTargetBalance,
                    debitExecuted, creditExecuted);
            }

            if (auditId != null) {
                String errorCode = e instanceof BankingException ? 
                    ((BankingException) e).getErrorCode() : "UNEXPECTED_ERROR";
                auditService.logOperationFailure(auditId, errorCode, e.getMessage());
            }

            if (e instanceof BankingException) {
                throw e;
            }
            throw new BankingException("TRANSFER_FAILED", 
                "Error inesperado durante la transferencia: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta múltiples transferencias en lote.
     * Cada transferencia es independiente - el fallo de una no afecta las demás.
     * 
     * @param requests Lista de solicitudes de transferencia
     * @return Mapa de resultados por cada request
     */
    public Map<TransferRequest, TransferResult> executeBatchTransfer(List<TransferRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista de transferencias no puede estar vacía");
        }

        if (requests.size() > 100) {
            throw new LimitExceededException("BATCH_SIZE", 
                new BigDecimal(100), new BigDecimal(requests.size()));
        }

        Map<TransferRequest, TransferResult> results = new LinkedHashMap<>();

        for (TransferRequest request : requests) {
            try {
                TransferResult result = executeTransfer(request);
                results.put(request, result);
            } catch (Exception e) {
                String errorCode = e instanceof BankingException ? 
                    ((BankingException) e).getErrorCode() : "BATCH_ITEM_FAILED";
                results.put(request, TransferResult.failure(errorCode, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Programa una transferencia para ejecución futura.
     * 
     * @param request Solicitud de transferencia
     * @param scheduledTime Fecha y hora programada
     * @return ID de la transferencia programada
     */
    public String scheduleTransfer(TransferRequest request, LocalDateTime scheduledTime) {
        validateTransferRequest(request);

        if (scheduledTime == null) {
            throw new IllegalArgumentException("La fecha programada es requerida");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "La fecha programada debe ser en el futuro");
        }

        if (scheduledTime.isAfter(LocalDateTime.now().plusMonths(6))) {
            throw new IllegalArgumentException(
                "No se pueden programar transferencias con más de 6 meses de anticipación");
        }

        // Validar que las cuentas existen
        getAndValidateAccount(request.getSourceAccountNumber(), "origen");
        getAndValidateAccount(request.getTargetAccountNumber(), "destino");

        // Crear transacción programada
        Transaction scheduledTransaction = new Transaction();
        scheduledTransaction.setSourceAccountNumber(request.getSourceAccountNumber());
        scheduledTransaction.setTargetAccountNumber(request.getTargetAccountNumber());
        scheduledTransaction.setAmount(request.getAmount());
        scheduledTransaction.setType(TransactionType.TRANSFER);
        scheduledTransaction.setStatus(TransactionStatus.PENDING);
        scheduledTransaction.setDescription("Transferencia programada: " + request.getDescription());

        Transaction saved = transactionRepository.save(scheduledTransaction);

        return saved.getTransactionId();
    }

    /**
     * Cancela una transferencia programada.
     * 
     * @param scheduledTransferId ID de la transferencia programada
     * @return true si se canceló exitosamente
     */
    public boolean cancelScheduledTransfer(String scheduledTransferId) {
        Transaction transaction = transactionRepository.findById(scheduledTransferId)
                .orElseThrow(() -> new BankingException("SCHEDULED_NOT_FOUND",
                    "Transferencia programada no encontrada"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new TransactionNotAllowedException(
                "Solo se pueden cancelar transferencias pendientes");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        return true;
    }

    /**
     * Verifica el estado de una transferencia.
     * 
     * @param transferId ID de la transferencia
     * @return Estado actual de la transferencia
     */
    public TransactionStatus getTransferStatus(String transferId) {
        return transactionRepository.findById(transferId)
                .map(Transaction::getStatus)
                .orElseThrow(() -> new BankingException("TRANSFER_NOT_FOUND",
                    "Transferencia no encontrada: " + transferId));
    }

    /**
     * Calcula la comisión estimada para una transferencia.
     * 
     * @param amount Monto de la transferencia
     * @param transferType Tipo de transferencia
     * @return Comisión estimada
     */
    public BigDecimal estimateFee(BigDecimal amount, TransferType transferType) {
        if (amount == null || transferType == null) {
            throw new IllegalArgumentException("Monto y tipo de transferencia son requeridos");
        }

        BigDecimal baseFee = transferType.getFee();
        
        // Para transferencias internacionales, agregar porcentaje adicional
        if (transferType == TransferType.INTERNATIONAL) {
            BigDecimal percentageFee = amount.multiply(new BigDecimal("0.005"));
            return baseFee.add(percentageFee);
        }

        return baseFee;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de transferencia es requerida");
        }
        if (request.getSourceAccountNumber() == null || 
            request.getSourceAccountNumber().trim().isEmpty()) {
            throw new AccountValidationException("sourceAccountNumber",
                "La cuenta origen es requerida");
        }
        if (request.getTargetAccountNumber() == null || 
            request.getTargetAccountNumber().trim().isEmpty()) {
            throw new AccountValidationException("targetAccountNumber",
                "La cuenta destino es requerida");
        }
        if (request.getAmount() == null || 
            request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (request.getTransferType() == null) {
            throw new IllegalArgumentException("El tipo de transferencia es requerido");
        }
    }

    private Account getAndValidateAccount(String accountNumber, String accountRole) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                    "Cuenta " + accountRole + " no encontrada: " + accountNumber));
    }

    private void validateTransferLimits(TransferRequest request) {
        BigDecimal maxAmount = request.getTransferType().getMaxAmount();
        if (request.getAmount().compareTo(maxAmount) > 0) {
            throw new LimitExceededException(
                request.getTransferType().name(),
                maxAmount,
                request.getAmount()
            );
        }
    }

    private TransferFraudCheckResult performFraudCheck(TransferRequest request) {
        try {
            CompletableFuture<Boolean> fraudCheck = CompletableFuture.supplyAsync(() -> 
                fraudDetectionService.validateTransfer(request)
            );

            boolean isValid = fraudCheck.get(FRAUD_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!isValid) {
                int riskScore = fraudDetectionService.evaluateTransactionRisk(
                    request.getSourceAccountNumber(), request.getAmount());
                
                fraudDetectionService.reportSuspiciousActivity(
                    UUID.randomUUID().toString(),
                    "Transferencia rechazada por fraude. Score: " + riskScore
                );

                return new TransferFraudCheckResult(false, "FRAUD_DETECTED",
                    "La transferencia fue rechazada por políticas de seguridad", riskScore);
            }

            return new TransferFraudCheckResult(true, null, null, 0);

        } catch (TimeoutException e) {
            // En caso de timeout, rechazar por seguridad
            return new TransferFraudCheckResult(false, "FRAUD_CHECK_TIMEOUT",
                "El servicio de validación no respondió a tiempo", -1);
        } catch (Exception e) {
            throw new BankingException("FRAUD_CHECK_ERROR",
                "Error al validar la transferencia: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createTransferMetadata(TransferRequest request, 
                                                        TransferFraudCheckResult fraudResult) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceAccount", request.getSourceAccountNumber());
        metadata.put("targetAccount", request.getTargetAccountNumber());
        metadata.put("amount", request.getAmount());
        metadata.put("transferType", request.getTransferType());
        metadata.put("fraudRiskScore", fraudResult.getRiskScore());
        return metadata;
    }

    private Transaction createTransferTransaction(TransferRequest request, BigDecimal fee) {
        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(request.getSourceAccountNumber());
        transaction.setTargetAccountNumber(request.getTargetAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(request.getDescription() != null ? 
            request.getDescription() : "Transferencia");
        transaction.setProcessedAt(LocalDateTime.now());
        return transaction;
    }

    public void executeRollback(Account sourceAccount, Account targetAccount,
                                  BigDecimal originalSourceBalance, BigDecimal originalTargetBalance,
                                  boolean debitExecuted, boolean creditExecuted) {
        String rollbackAuditId = auditService.logOperationStart("ROLLBACK", 
            sourceAccount.getAccountNumber(), 
            Map.of("reason", "Transfer failed, executing compensation"));

        try {
            if (debitExecuted && originalSourceBalance != null) {
                sourceAccount.setBalance(originalSourceBalance);
                accountRepository.save(sourceAccount);
            }

            if (creditExecuted && originalTargetBalance != null) {
                targetAccount.setBalance(originalTargetBalance);
                accountRepository.save(targetAccount);
            }

            auditService.logOperationSuccess(rollbackAuditId, "ROLLBACK_COMPLETED");

        } catch (Exception rollbackError) {
            // Log crítico: el rollback falló
            auditService.logOperationFailure(rollbackAuditId, "ROLLBACK_FAILED", 
                rollbackError.getMessage());
            
            // Notificar sobre inconsistencia crítica
            notificationService.sendEmail(
                "operations@bank.com",
                "ALERTA CRÍTICA: Rollback fallido",
                "Transferencia fallida con rollback incompleto. " +
                "Cuenta origen: " + sourceAccount.getAccountNumber() +
                ", Cuenta destino: " + targetAccount.getAccountNumber()
            );
        }
    }

    private void sendTransferNotifications(Account sourceAccount, Account targetAccount,
                                            TransferRequest request, Transaction transaction) {
        try {
            // Notificar al origen
            notificationService.sendPushNotification(
                sourceAccount.getAccountNumber(),
                "Transferencia enviada",
                "Has enviado " + request.getAmount() + " a " + 
                    targetAccount.getAccountNumber()
            );

            // Notificar al destino
            notificationService.sendPushNotification(
                targetAccount.getAccountNumber(),
                "Transferencia recibida",
                "Has recibido " + request.getAmount() + " de " + 
                    sourceAccount.getAccountNumber()
            );

        } catch (Exception e) {
            // Las notificaciones no son críticas, solo logear el error
            auditService.logOperationFailure(
                "NOTIFICATION_" + transaction.getTransactionId(),
                "NOTIFICATION_FAILED",
                e.getMessage()
            );
        }
    }

    private String generateConfirmationNumber() {
        return "TRF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    // ==================== CLASE INTERNA ====================

    /**
     * Resultado de la verificación de fraude para transferencias.
     */
    public static class TransferFraudCheckResult {
        private final boolean approved;
        private final String rejectCode;
        private final String rejectReason;
        private final int riskScore;

        public TransferFraudCheckResult(boolean approved, String rejectCode, 
                                        String rejectReason, int riskScore) {
            this.approved = approved;
            this.rejectCode = rejectCode;
            this.rejectReason = rejectReason;
            this.riskScore = riskScore;
        }

        public boolean isApproved() {
            return approved;
        }

        public String getRejectCode() {
            return rejectCode;
        }

        public String getRejectReason() {
            return rejectReason;
        }

        public int getRiskScore() {
            return riskScore;
        }
    }
}
