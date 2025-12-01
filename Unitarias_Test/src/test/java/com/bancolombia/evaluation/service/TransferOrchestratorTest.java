package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.*;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.FraudDetectionService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TransferOrchestratorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @Mock
    private AccountValidator accountValidator;

    private TransferOrchestrator transferOrchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferOrchestrator = new TransferOrchestrator(
                accountRepository, transactionRepository, fraudDetectionService,
                notificationService, auditService, accountValidator
        );
    }

    @Test
    @DisplayName("Debe ejecutar la transferencia exitosamente")
    void testExecuteTransfer_Success() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN123");

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccount.getAccountNumber());
        request.setTargetAccountNumber(targetAccount.getAccountNumber());
        request.setAmount(new BigDecimal("1000"));
        request.setTransferType(TransferType.SAME_BANK);

        // Mocks
        when(accountRepository.findByAccountNumber("1234567899")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("1234567897")).thenReturn(Optional.of(targetAccount));
        when(accountValidator.canPerformOperations(sourceAccount)).thenReturn(true);
        when(accountValidator.canPerformOperations(targetAccount)).thenReturn(true);
        when(fraudDetectionService.validateTransfer(any())).thenReturn(true);
        when(fraudDetectionService.evaluateTransactionRisk(any(), any())).thenReturn(0);
        when(auditService.logOperationStart(any(), any(), any())).thenReturn("AUDIT123");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Ejecutar
        TransferResult result = transferOrchestrator.executeTransfer(request);

        // Verificar resultados
        assertEquals("TXN123", result.getTransferId());
        assertEquals("TRF-", result.getConfirmationNumber().substring(0, 4)); // Confirmación debe iniciar con "TRF"
        assertEquals(new BigDecimal("1000"), result.getTransferredAmount());
        assertEquals(new BigDecimal("0"), result.getFee());
    }

    @Test
    @DisplayName("Debe lanzar una excepción de fondos insuficientes cuando no haya suficiente saldo")
    void testExecuteTransfer_InsufficientFunds() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN123");

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccount.getAccountNumber());
        request.setTargetAccountNumber(targetAccount.getAccountNumber());
        request.setAmount(new BigDecimal("3000"));
        request.setTransferType(TransferType.SAME_BANK);

        // Mocks
        when(accountRepository.findByAccountNumber(sourceAccount.getAccountNumber())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetAccount.getAccountNumber())).thenReturn(Optional.of(targetAccount));
        when(accountValidator.canPerformOperations(sourceAccount)).thenReturn(true);
        when(accountValidator.canPerformOperations(targetAccount)).thenReturn(true);

        // Ejecutar y verificar que se lance la excepción esperada
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            transferOrchestrator.executeTransfer(request);
        });

        assertEquals("Saldo insuficiente. Disponible: 2000, Requerido: 3000", exception.getMessage());

    }

    @Test
    @DisplayName("Debe rechazar la transferencia por detección de fraude")
    void testExecuteTransfer_FraudDetected() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);
        TransferOrchestrator.TransferFraudCheckResult fraudResult = new TransferOrchestrator.TransferFraudCheckResult(false, "FRAUD_DETECTED", "Fraude detectado", 100);


        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccount.getAccountNumber());
        request.setTargetAccountNumber(targetAccount.getAccountNumber());
        request.setAmount(new BigDecimal("1000"));
        request.setTransferType(TransferType.SAME_BANK);

        // Mocks
        when(accountRepository.findByAccountNumber(sourceAccount.getAccountNumber())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetAccount.getAccountNumber())).thenReturn(Optional.of(targetAccount));
        when(accountValidator.canPerformOperations(sourceAccount)).thenReturn(true);
        when(accountValidator.canPerformOperations(targetAccount)).thenReturn(true);
        when(fraudDetectionService.validateTransfer(any())).thenReturn(false);
        when(fraudDetectionService.evaluateTransactionRisk(any(), any())).thenReturn(100);

        // Ejecutar
        TransferResult result = transferOrchestrator.executeTransfer(request);

        // Verificar resultado de rechazo
        assertEquals("FRAUD_DETECTED", result.getErrorCode());
        assertEquals("La transferencia fue rechazada por políticas de seguridad", result.getMessage());

    }

    @Test
    @DisplayName("Debe lanzar una excepción cuando no se permita la transferencia a la misma cuenta")
    void testExecuteTransfer_TransactionNotAllowed() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567899", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);


        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccount.getAccountNumber());
        request.setTargetAccountNumber(targetAccount.getAccountNumber());
        request.setAmount(new BigDecimal("1000"));
        request.setTransferType(TransferType.SAME_BANK);

        // Mocks
        when(accountRepository.findByAccountNumber(sourceAccount.getAccountNumber())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetAccount.getAccountNumber())).thenReturn(Optional.of(targetAccount));

        // Ejecutar y verificar que se lance la excepción
        TransactionNotAllowedException exception = assertThrows(TransactionNotAllowedException.class, () -> {
            transferOrchestrator.executeTransfer(request);
        });

        assertEquals("No se puede transferir a la misma cuenta", exception.getMessage());

    }

    @Test
    @DisplayName("Debe agendar la transferencia exitosamente")
    void testScheduleTransfer_Success() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);


        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccount.getAccountNumber());
        request.setTargetAccountNumber(targetAccount.getAccountNumber());
        request.setAmount(new BigDecimal("1000"));
        request.setTransferType(TransferType.SAME_BANK);

        // Mocks
        when(accountRepository.findByAccountNumber(sourceAccount.getAccountNumber())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetAccount.getAccountNumber())).thenReturn(Optional.of(targetAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        String scheduledTransferId = transferOrchestrator.scheduleTransfer(request, LocalDateTime.now().plusDays(1));

        assertNotNull(scheduledTransferId);

    }

    @Test
    @DisplayName("Debe ejecutar el rollback exitosamente")
    void testExecuteRollback_Success() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("2000"), AccountType.SAVINGS);

        BigDecimal originalSourceBalance = sourceAccount.getBalance();
        BigDecimal originalTargetBalance = targetAccount.getBalance();

        // Simulamos que los métodos de repositorio funcionan sin errores
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount, targetAccount);  // Simula la persistencia sin problemas

        // Mocks para auditoría
        when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("auditId");
        doNothing().when(auditService).logOperationSuccess(anyString(), anyString());

        // Ejecutar rollback
        transferOrchestrator.executeRollback(sourceAccount, targetAccount,
                originalSourceBalance, originalTargetBalance,
                true, true);

        // Verificar que los balances se restauren correctamente
        assertEquals(originalSourceBalance, sourceAccount.getBalance());
        assertEquals(originalTargetBalance, targetAccount.getBalance());

        // Verificar que las llamadas de auditoría fueron ejecutadas correctamente
        verify(auditService, times(1)).logOperationSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe manejar fallo en el rollback correctamente")
    void testExecuteRollback_FailureDuringRollback() {
        Account sourceAccount = new Account("1234567899", "Ander", "3", new BigDecimal("2000"), AccountType.SAVINGS);
        Account targetAccount = new Account("1234567897", "Santi", "4", new BigDecimal("500"), AccountType.SAVINGS);

        BigDecimal originalSourceBalance = sourceAccount.getBalance();
        BigDecimal originalTargetBalance = targetAccount.getBalance();

        // Simulamos que el método de repositorio para guardar las cuentas lanza una excepción
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Error en la restauración de cuenta"));

        // Mocks para auditoría
        when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("auditId");
        doNothing().when(auditService).logOperationFailure(anyString(), anyString(), anyString());

        // Ejecutar rollback
        transferOrchestrator.executeRollback(sourceAccount, targetAccount,
                originalSourceBalance, originalTargetBalance,
                true, true);

        // Verificar que las llamadas de auditoría se ejecutaron correctamente (aunque hubo un fallo)
        verify(auditService, times(1)).logOperationFailure(anyString(), eq("ROLLBACK_FAILED"), anyString());

        // Verificar que las cuentas no han sido modificadas por el fallo en el rollback
        assertEquals(new BigDecimal("2000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("500"), targetAccount.getBalance());
    }
}
