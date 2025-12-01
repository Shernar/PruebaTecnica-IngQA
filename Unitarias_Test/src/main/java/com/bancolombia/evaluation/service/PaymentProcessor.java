package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.*;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.FraudDetectionService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NIVEL 3: AVANZADO
 * 
 * Procesador de pagos con lógica de negocio compleja.
 * 
 * Esta clase requiere múltiples mocks y tiene lógica condicional compleja.
 * 
 * El candidato debe demostrar:
 * - Manejo de múltiples mocks y su interacción
 * - Verificación de orden de llamadas con InOrder
 * - ArgumentCaptor para verificar argumentos
 * - Tests de excepciones personalizadas
 * - @Nested para organizar tests relacionados
 * - Uso de @BeforeEach y @AfterEach
 * - Mock de métodos void
 * - Tests con diferentes configuraciones de negocio
 */
public class PaymentProcessor {

    private static final int HIGH_RISK_THRESHOLD = 70;
    private static final int MEDIUM_RISK_THRESHOLD = 40;
    private static final LocalTime BUSINESS_HOURS_START = LocalTime.of(6, 0);
    private static final LocalTime BUSINESS_HOURS_END = LocalTime.of(22, 0);
    private static final BigDecimal SUSPICIOUS_AMOUNT_THRESHOLD = new BigDecimal("10000000");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public PaymentProcessor(AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            FraudDetectionService fraudDetectionService,
                            NotificationService notificationService,
                            AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Procesa un pago completo con validaciones de fraude y reglas de negocio.
     * 
     * @param request Solicitud de pago
     * @return Resultado del pago
     * @throws AccountNotFoundException si la cuenta no existe
     * @throws TransactionNotAllowedException si el pago no está permitido
     * @throws InsufficientFundsException si no hay saldo suficiente
     */
    public PaymentResult processPayment(PaymentRequest request) {
        validatePaymentRequest(request);

        // Verificar cuenta origen
        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getSourceAccountNumber()));

        // Validar estado de la cuenta
        validateAccountStatus(sourceAccount);

        // Verificar horario de operación para montos altos
        if (request.getAmount().compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
            validateBusinessHours();
        }

        // Verificar lista negra
        if (fraudDetectionService.isBlacklisted(request.getSourceAccountNumber())) {
            return PaymentResult.failure("BLACKLISTED", 
                "La cuenta se encuentra bloqueada por seguridad");
        }

        // Evaluar riesgo de fraude
        int riskScore = fraudDetectionService.evaluateTransactionRisk(
            request.getSourceAccountNumber(), request.getAmount());

        if (riskScore >= HIGH_RISK_THRESHOLD) {
            fraudDetectionService.reportSuspiciousActivity(
                UUID.randomUUID().toString(), 
                "Pago de alto riesgo detectado: score " + riskScore);
            return PaymentResult.failure("HIGH_RISK", 
                "Transacción rechazada por políticas de seguridad");
        }

        // Validar límites del método de pago
        validatePaymentMethodLimits(request);

        // Calcular monto total con comisiones
        BigDecimal commission = calculateCommission(request);
        BigDecimal totalAmount = request.getAmount().add(commission);

        // Verificar saldo suficiente
        if (sourceAccount.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException(sourceAccount.getBalance(), totalAmount);
        }

        // Validación adicional de fraude
        if (!fraudDetectionService.validatePayment(request)) {
            return PaymentResult.failure("FRAUD_VALIDATION_FAILED", 
                "El pago no pasó las validaciones de seguridad");
        }

        // Registrar auditoría
        Map<String, Object> metadata = createPaymentMetadata(request, riskScore);
        String auditId = auditService.logOperationStart("PAYMENT", 
            request.getSourceAccountNumber(), metadata);

        try {
            // Procesar pago
            PaymentResult result = executePayment(sourceAccount, request, totalAmount, commission);

            if (result.isSuccessful()) {
                auditService.logOperationSuccess(auditId, result.getAuthorizationCode());
                
                // Notificaciones según nivel de riesgo
                sendNotifications(request, result, riskScore);
            }

            return result;

        } catch (Exception e) {
            auditService.logOperationFailure(auditId, "PROCESSING_ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Procesa un reembolso de un pago anterior.
     * 
     * @param originalTransactionId ID de la transacción original
     * @param amount Monto a reembolsar (puede ser parcial)
     * @return Resultado del reembolso
     */
    public PaymentResult processRefund(String originalTransactionId, BigDecimal amount) {
        Transaction originalTransaction = transactionRepository.findById(originalTransactionId)
                .orElseThrow(() -> new BankingException("TRANSACTION_NOT_FOUND", 
                    "Transacción original no encontrada"));

        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new TransactionNotAllowedException(
                "Solo se pueden reembolsar transacciones completadas");
        }

        if (originalTransaction.getType() != TransactionType.PAYMENT) {
            throw new TransactionNotAllowedException(
                "Solo se pueden reembolsar pagos");
        }

        if (amount.compareTo(originalTransaction.getAmount()) > 0) {
            throw new LimitExceededException("REFUND", 
                originalTransaction.getAmount(), amount);
        }

        Account account = accountRepository.findByAccountNumber(
            originalTransaction.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                    originalTransaction.getSourceAccountNumber()));

        // Procesar reembolso
        BigDecimal previousBalance = account.getBalance();
        account.setBalance(previousBalance.add(amount));
        accountRepository.save(account);

        // Marcar transacción original como reembolsada
        originalTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);

        // Crear transacción de reembolso
        Transaction refundTransaction = new Transaction();
        refundTransaction.setTargetAccountNumber(account.getAccountNumber());
        refundTransaction.setAmount(amount);
        refundTransaction.setType(TransactionType.REFUND);
        refundTransaction.setStatus(TransactionStatus.COMPLETED);
        refundTransaction.setDescription("Reembolso de transacción: " + originalTransactionId);
        refundTransaction.setReferenceNumber(originalTransactionId);
        refundTransaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(refundTransaction);

        // Auditar
        auditService.logBalanceChange(account.getAccountNumber(), 
            previousBalance, account.getBalance(), refundTransaction.getTransactionId());

        // Notificar
        notificationService.notifyTransactionCompleted(refundTransaction);

        return PaymentResult.success(
            refundTransaction.getTransactionId(),
            "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            amount,
            BigDecimal.ZERO
        );
    }

    /**
     * Verifica si un pago puede ser procesado (validación previa).
     * 
     * @param request Solicitud de pago
     * @return true si el pago puede procesarse
     */
    public boolean canProcessPayment(PaymentRequest request) {
        try {
            validatePaymentRequest(request);

            Account account = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                    .orElse(null);

            if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
                return false;
            }

            if (fraudDetectionService.isBlacklisted(request.getSourceAccountNumber())) {
                return false;
            }

            BigDecimal totalWithCommission = request.getAmount()
                    .add(calculateCommission(request));

            return account.getBalance().compareTo(totalWithCommission) >= 0;

        } catch (Exception e) {
            return false;
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de pago no puede ser nula");
        }
        if (request.getSourceAccountNumber() == null || 
            request.getSourceAccountNumber().trim().isEmpty()) {
            throw new AccountValidationException("sourceAccountNumber", 
                "El número de cuenta origen es requerido");
        }
        if (request.getMerchantId() == null || request.getMerchantId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del comercio es requerido");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("El método de pago es requerido");
        }
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new TransactionNotAllowedException("ACCOUNT_BLOCKED",
                "La cuenta se encuentra bloqueada");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new TransactionNotAllowedException("ACCOUNT_CLOSED",
                "La cuenta se encuentra cerrada");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new TransactionNotAllowedException(
                "La cuenta no está activa. Estado: " + account.getStatus());
        }
    }

    private void validateBusinessHours() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        // No permitir pagos grandes los fines de semana
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new TransactionNotAllowedException("OUTSIDE_BUSINESS_HOURS",
                "Pagos de alto monto no permitidos en fines de semana");
        }

        // Verificar horario de operación
        if (currentTime.isBefore(BUSINESS_HOURS_START) || 
            currentTime.isAfter(BUSINESS_HOURS_END)) {
            throw new TransactionNotAllowedException("OUTSIDE_BUSINESS_HOURS",
                "Pagos de alto monto solo permitidos entre " + 
                BUSINESS_HOURS_START + " y " + BUSINESS_HOURS_END);
        }
    }

    private void validatePaymentMethodLimits(PaymentRequest request) {
        BigDecimal maxAmount = request.getPaymentMethod().getMaxAmount();
        if (request.getAmount().compareTo(maxAmount) > 0) {
            throw new LimitExceededException(
                request.getPaymentMethod().name(), 
                maxAmount, 
                request.getAmount()
            );
        }
    }

    private BigDecimal calculateCommission(PaymentRequest request) {
        double commissionRate = request.getPaymentMethod().getCommission();
        return request.getAmount()
                .multiply(BigDecimal.valueOf(commissionRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> createPaymentMetadata(PaymentRequest request, int riskScore) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("amount", request.getAmount());
        metadata.put("merchantId", request.getMerchantId());
        metadata.put("paymentMethod", request.getPaymentMethod());
        metadata.put("riskScore", riskScore);
        return metadata;
    }

    private PaymentResult executePayment(Account account, PaymentRequest request,
                                          BigDecimal totalAmount, BigDecimal commission) {
        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.subtract(totalAmount);
        account.setBalance(newBalance);
        account.setLastTransactionAt(LocalDateTime.now());
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(account.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.PAYMENT);
        transaction.setDescription(request.getDescription() != null ? 
            request.getDescription() : "Pago a " + request.getMerchantId());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        auditService.logBalanceChange(account.getAccountNumber(), 
            previousBalance, newBalance, saved.getTransactionId());

        String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return PaymentResult.success(
            saved.getTransactionId(),
            authCode,
            totalAmount,
            commission
        );
    }

    private void sendNotifications(PaymentRequest request, PaymentResult result, int riskScore) {
        notificationService.sendPushNotification(
            request.getSourceAccountNumber(),
            "Pago procesado",
            "Tu pago por " + request.getAmount() + " ha sido procesado exitosamente"
        );

        // Si el riesgo es medio, enviar alerta adicional
        if (riskScore >= MEDIUM_RISK_THRESHOLD) {
            notificationService.sendEmail(
                request.getSourceAccountNumber() + "@notification.com",
                "Alerta de seguridad",
                "Se ha procesado un pago que requiere tu atención"
            );
        }
    }
}
