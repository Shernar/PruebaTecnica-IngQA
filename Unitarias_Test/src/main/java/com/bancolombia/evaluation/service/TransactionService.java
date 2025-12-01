package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.*;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NIVEL 2: INTERMEDIO
 * 
 * Servicio para procesamiento de transacciones bancarias.
 * 
 * Esta clase tiene dependencias externas que deben ser mockeadas.
 * 
 * El candidato debe demostrar:
 * - Uso de @Mock y @InjectMocks
 * - Uso de when/thenReturn para configurar mocks
 * - Uso de verify para verificar interacciones
 * - Tests parametrizados con @ParameterizedTest
 * - @ValueSource, @CsvSource, @MethodSource
 * - Manejo de múltiples escenarios de error
 */
public class TransactionService {

    private static final int MAX_DAILY_TRANSACTIONS = 50;
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAX_SINGLE_TRANSACTION = new BigDecimal("100000000");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              NotificationService notificationService,
                              AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Procesa un depósito en una cuenta.
     * 
     * @param accountNumber Número de cuenta destino
     * @param amount Monto a depositar
     * @param description Descripción del depósito
     * @return Transacción procesada
     * @throws AccountNotFoundException si la cuenta no existe
     * @throws TransactionNotAllowedException si la cuenta no está activa
     * @throws IllegalArgumentException si el monto es inválido
     */
    public Transaction processDeposit(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new TransactionNotAllowedException(
                "No se puede depositar en una cuenta con estado: " + account.getStatus());
        }

        // Registrar inicio de operación en auditoría
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("amount", amount);
        metadata.put("type", "DEPOSIT");
        String auditId = auditService.logOperationStart("DEPOSIT", accountNumber, metadata);

        try {
            // Crear transacción
            Transaction transaction = new Transaction();
            transaction.setTargetAccountNumber(accountNumber);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDescription(description);
            transaction.setStatus(TransactionStatus.PROCESSING);

            // Actualizar saldo
            BigDecimal previousBalance = account.getBalance();
            BigDecimal newBalance = previousBalance.add(amount);
            account.setBalance(newBalance);
            account.setLastTransactionAt(LocalDateTime.now());

            // Persistir cambios
            accountRepository.save(account);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Auditar cambio de saldo
            auditService.logBalanceChange(accountNumber, previousBalance, newBalance, 
                                          savedTransaction.getTransactionId());
            auditService.logOperationSuccess(auditId, "COMPLETED");

            // Notificar al usuario
            notificationService.notifyTransactionCompleted(savedTransaction);

            return savedTransaction;

        } catch (Exception e) {
            auditService.logOperationFailure(auditId, "PROCESSING_ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Procesa un retiro de una cuenta.
     * 
     * @param accountNumber Número de cuenta origen
     * @param amount Monto a retirar
     * @param description Descripción del retiro
     * @return Transacción procesada
     * @throws AccountNotFoundException si la cuenta no existe
     * @throws InsufficientFundsException si no hay saldo suficiente
     * @throws TransactionNotAllowedException si se excede el límite diario
     */
    public Transaction processWithdrawal(String accountNumber, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new TransactionNotAllowedException(
                "No se puede retirar de una cuenta con estado: " + account.getStatus());
        }

        // Verificar límite diario de transacciones
        int todayTransactions = transactionRepository.countTodayTransactionsByAccount(accountNumber);
        if (todayTransactions >= MAX_DAILY_TRANSACTIONS) {
            throw new TransactionNotAllowedException("DAILY_LIMIT_REACHED",
                "Se ha alcanzado el límite diario de transacciones");
        }

        // Verificar saldo suficiente
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        // Registrar inicio de operación en auditoría
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("amount", amount);
        metadata.put("type", "WITHDRAWAL");
        String auditId = auditService.logOperationStart("WITHDRAWAL", accountNumber, metadata);

        try {
            // Crear transacción
            Transaction transaction = new Transaction();
            transaction.setSourceAccountNumber(accountNumber);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setDescription(description);
            transaction.setStatus(TransactionStatus.PROCESSING);

            // Actualizar saldo
            BigDecimal previousBalance = account.getBalance();
            BigDecimal newBalance = previousBalance.subtract(amount);
            account.setBalance(newBalance);
            account.setLastTransactionAt(LocalDateTime.now());

            // Persistir cambios
            accountRepository.save(account);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Auditar cambio de saldo
            auditService.logBalanceChange(accountNumber, previousBalance, newBalance, 
                                          savedTransaction.getTransactionId());
            auditService.logOperationSuccess(auditId, "COMPLETED");

            // Notificar al usuario
            notificationService.notifyTransactionCompleted(savedTransaction);

            return savedTransaction;

        } catch (Exception e) {
            auditService.logOperationFailure(auditId, "PROCESSING_ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene el historial de transacciones de una cuenta.
     * 
     * @param accountNumber Número de cuenta
     * @param startDate Fecha inicial (opcional)
     * @param endDate Fecha final (opcional)
     * @return Lista de transacciones
     * @throws AccountNotFoundException si la cuenta no existe
     */
    public List<Transaction> getTransactionHistory(String accountNumber, 
                                                    LocalDateTime startDate, 
                                                    LocalDateTime endDate) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountNotFoundException(accountNumber);
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(3);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                "La fecha inicial no puede ser posterior a la fecha final");
        }

        return transactionRepository.findByAccountAndDateRange(accountNumber, startDate, endDate);
    }

    /**
     * Cancela una transacción pendiente.
     * 
     * @param transactionId ID de la transacción
     * @return Transacción cancelada
     * @throws BankingException si la transacción no existe o no puede ser cancelada
     */
    public Transaction cancelTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BankingException("TRANSACTION_NOT_FOUND", 
                    "Transacción no encontrada: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new TransactionNotAllowedException(
                "Solo se pueden cancelar transacciones pendientes. Estado actual: " 
                + transaction.getStatus());
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        return transactionRepository.save(transaction);
    }

    /**
     * Calcula la suma total de transacciones de un tipo en un período.
     * 
     * @param accountNumber Número de cuenta
     * @param type Tipo de transacción
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Suma total de las transacciones
     */
    public BigDecimal calculateTotalByType(String accountNumber, TransactionType type,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = getTransactionHistory(accountNumber, startDate, endDate);
        
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                "El monto mínimo de transacción es: " + MIN_TRANSACTION_AMOUNT);
        }
        if (amount.compareTo(MAX_SINGLE_TRANSACTION) > 0) {
            throw new LimitExceededException("TRANSACTION", MAX_SINGLE_TRANSACTION, amount);
        }
    }
}
