package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.AccountNotFoundException;
import com.bancolombia.evaluation.exception.InsufficientFundsException;
import com.bancolombia.evaluation.exception.TransactionNotAllowedException;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EJEMPLO DE TEST CON MOCKITO PARA REFERENCIA DEL CANDIDATO
 * <p>
 * Este archivo muestra técnicas de Mockito para tests con dependencias.
 * El candidato debe completar los tests faltantes.
 * <p>
 * Este ejemplo cubre aproximadamente el 15% de los tests necesarios
 * para TransactionService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - Tests de Ejemplo con Mockito")
class TransactionServiceExampleTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    // ==================== EJEMPLO: Setup de datos de prueba ====================

    private Account createActiveAccount(String accountNumber, BigDecimal balance) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.SAVINGS);
        return account;
    }

    // ==================== EJEMPLO: Test básico con mock ====================

    @Nested
    @DisplayName("Proceso de Depósito")
    class DepositTests {

        @Test
        @DisplayName("Debe procesar depósito exitosamente cuando la cuenta existe y está activa")
        void shouldProcessDepositSuccessfully() {
            // Arrange
            String accountNumber = "1234567890";
            BigDecimal amount = new BigDecimal("100000");
            BigDecimal initialBalance = new BigDecimal("500000");
            Account account = createActiveAccount(accountNumber, initialBalance);

            // Configurar mocks
            when(accountRepository.findByAccountNumber(accountNumber))
                    .thenReturn(Optional.of(account));
            when(auditService.logOperationStart(anyString(), anyString(), anyMap()))
                    .thenReturn("AUDIT-123");
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Transaction result = transactionService.processDeposit(
                    accountNumber, amount, "Depósito de prueba");

            // Assert
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(TransactionType.DEPOSIT, result.getType()),
                    () -> assertEquals(TransactionStatus.COMPLETED, result.getStatus()),
                    () -> assertEquals(amount, result.getAmount())
            );

            // Verificar interacciones
            verify(accountRepository).findByAccountNumber(accountNumber);
            verify(accountRepository).save(any(Account.class));
            verify(transactionRepository).save(any(Transaction.class));
            verify(notificationService).notifyTransactionCompleted(any(Transaction.class));
        }

        @Test
        @DisplayName("Debe lanzar AccountNotFoundException cuando la cuenta no existe")
        void shouldThrowExceptionWhenAccountNotFound() {
            // Arrange
            String accountNumber = "9999999999";
            when(accountRepository.findByAccountNumber(accountNumber))
                    .thenReturn(Optional.empty());

            // Act & Assert
            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> transactionService.processDeposit(
                            accountNumber, new BigDecimal("100000"), "Test")
            );

            assertTrue(exception.getMessage().contains(accountNumber));

            // Verificar que NO se guardó nada
            verify(transactionRepository, never()).save(any());
        }

        // TODO: El candidato debe agregar tests para:
        // - Cuenta bloqueada
        // - Cuenta inactiva
        // - Monto inválido (null, negativo, muy pequeño)
        // - Error en auditService

        // NUEVOS TESTS AGREGADOS

        @Test
        @DisplayName("Debe lanzar excepción si la cuenta está bloqueada")
        void shouldThrowExceptionIfAccountBlocked() {
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            account.setStatus(AccountStatus.BLOCKED);
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));

            TransactionNotAllowedException ex = assertThrows(TransactionNotAllowedException.class,
                    () -> transactionService.processDeposit("123", new BigDecimal("10000"), "Depósito"));
            assertTrue(ex.getMessage().contains("No se puede depositar"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si la cuenta está inactiva")
        void shouldThrowExceptionIfAccountInactive() {
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            account.setStatus(AccountStatus.INACTIVE);
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));

            TransactionNotAllowedException ex = assertThrows(TransactionNotAllowedException.class,
                    () -> transactionService.processDeposit("123", new BigDecimal("10000"), "Depósito"));
            assertTrue(ex.getMessage().contains("No se puede depositar"));
        }

        @ParameterizedTest(name = "Monto inválido: {0}")
        @NullSource
        @ValueSource(doubles = {-1000, 0, 500})
        @DisplayName("Debe lanzar IllegalArgumentException si el monto es inválido")
        void shouldThrowExceptionIfInvalidAmount(Double amountDouble) {
            BigDecimal amount = amountDouble != null ? BigDecimal.valueOf(amountDouble) : null;
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            lenient().when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.processDeposit("123", amount, "Depósito inválido"));
        }

        @Test
        @DisplayName("Debe registrar fallo en auditoría si ocurre error durante depósito")
        void shouldLogFailureIfErrorDuringDeposit() {
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));
            when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-123");

            when(transactionRepository.save(any())).thenThrow(new RuntimeException("DB error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> transactionService.processDeposit("123", new BigDecimal("5000"), "Depósito"));

            assertEquals("DB error", ex.getMessage());
            verify(auditService).logOperationFailure(eq("AUDIT-123"), eq("PROCESSING_ERROR"), eq("DB error"));
        }
    }

    // ==================== EJEMPLO: Verificación de interacciones ====================

    @Nested
    @DisplayName("Verificación de interacciones con servicios externos")
    class InteractionVerificationTests {

        @Test
        @DisplayName("Debe registrar auditoría antes y después del depósito")
        void shouldLogAuditBeforeAndAfterDeposit() {
            // Arrange
            Account account = createActiveAccount("1234567890", new BigDecimal("500000"));
            when(accountRepository.findByAccountNumber(anyString()))
                    .thenReturn(Optional.of(account));
            when(auditService.logOperationStart(anyString(), anyString(), anyMap()))
                    .thenReturn("AUDIT-123");
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> {
                        Transaction t = inv.getArgument(0);
                        t.setTransactionId("TXN-001");
                        return t;
                    });

            // Act
            transactionService.processDeposit("1234567890",
                    new BigDecimal("100000"), "Test");

            // Assert - Verificar orden de llamadas
            InOrder inOrder = inOrder(auditService);
            inOrder.verify(auditService).logOperationStart(
                    eq("DEPOSIT"), eq("1234567890"), anyMap());
            inOrder.verify(auditService).logBalanceChange(
                    anyString(), any(BigDecimal.class), any(BigDecimal.class), anyString());
            inOrder.verify(auditService).logOperationSuccess(eq("AUDIT-123"), anyString());
        }

        // TODO: El candidato debe agregar tests para verificar:
        // - Que se notifica al usuario después del depósito
        // - Que se registra el fallo en auditoría cuando hay error
        // - El orden correcto de las operaciones

        // NUEVOS TESTS AGREGADOS

        @Nested
        @DisplayName("Escenarios adicionales de Depósito")
        class DepositAdditionalTests {

            @Test
            @DisplayName("Debe notificar al usuario después del depósito exitoso")
            void shouldNotifyUserAfterDeposit() {
                // Arrange
                String accountNumber = "1234567890";
                BigDecimal amount = new BigDecimal("100000");
                Account account = createActiveAccount(accountNumber, new BigDecimal("500000"));

                when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
                when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-001");
                when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
                when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                transactionService.processDeposit(accountNumber, amount, "Depósito prueba");

                // Assert
                verify(notificationService, times(1)).notifyTransactionCompleted(any(Transaction.class));
            }

            @Test
            @DisplayName("Debe registrar fallo en auditoría si ocurre un error en el depósito")
            void shouldLogAuditFailureWhenDepositFails() {
                // Arrange
                String accountNumber = "1234567890";
                BigDecimal amount = new BigDecimal("100000");
                Account account = createActiveAccount(accountNumber, new BigDecimal("500000"));

                when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
                when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-002");

                // Forzar error en transactionRepository
                when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("DB error"));

                // Act & Assert
                assertThrows(RuntimeException.class, () ->
                        transactionService.processDeposit(accountNumber, amount, "Depósito prueba"));

                verify(auditService, times(1)).logOperationFailure(eq("AUDIT-002"), eq("PROCESSING_ERROR"), eq("DB error"));
            }

            @Test
            @DisplayName("Debe verificar el orden correcto de auditoría, actualización y notificación")
            void shouldVerifyOperationOrder() {
                // Arrange
                String accountNumber = "1234567890";
                BigDecimal amount = new BigDecimal("100000");
                Account account = createActiveAccount(accountNumber, new BigDecimal("500000"));

                when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
                when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-003");
                when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
                when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

                // Act
                transactionService.processDeposit(accountNumber, amount, "Depósito prueba");

                // Assert - InOrder
                InOrder inOrder = inOrder(auditService, accountRepository, transactionRepository, notificationService);
                inOrder.verify(auditService).logOperationStart(eq("DEPOSIT"), eq(accountNumber), anyMap());
                inOrder.verify(accountRepository).save(any(Account.class));
                inOrder.verify(transactionRepository).save(any(Transaction.class));
                inOrder.verify(auditService).logBalanceChange(eq(accountNumber), any(BigDecimal.class), any(BigDecimal.class), anyString());
                inOrder.verify(auditService).logOperationSuccess(eq("AUDIT-003"), anyString());
                inOrder.verify(notificationService).notifyTransactionCompleted(any(Transaction.class));
            }
        }
    }

    // ==================== EJEMPLO: ArgumentCaptor ====================

    @Test
    @DisplayName("Debe actualizar el saldo correctamente después del depósito")
    void shouldUpdateBalanceCorrectlyAfterDeposit() {
        // Arrange
        BigDecimal initialBalance = new BigDecimal("500000");
        BigDecimal depositAmount = new BigDecimal("100000");
        BigDecimal expectedFinalBalance = new BigDecimal("600000");

        Account account = createActiveAccount("1234567890", initialBalance);

        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(account));
        when(auditService.logOperationStart(anyString(), anyString(), anyMap()))
                .thenReturn("AUDIT-123");
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Capturar el argumento guardado
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // Act
        transactionService.processDeposit("1234567890", depositAmount, "Test");

        // Assert
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();

        assertEquals(expectedFinalBalance, savedAccount.getBalance());
    }

    // ==================== NOTA PARA EL CANDIDATO ====================
    /*
     * Este archivo es solo un EJEMPLO de cómo usar Mockito.
     *
     * TAREAS PENDIENTES PARA TransactionService:
     * 1. Tests completos para processDeposit
     * 2. Tests completos para processWithdrawal
     * 3. Tests para getTransactionHistory
     * 4. Tests para cancelTransaction
     * 5. Tests para calculateTotalByType
     *
     * TÉCNICAS A DEMOSTRAR:
     * - @Mock y @InjectMocks
     * - when/thenReturn y when/thenThrow
     * - verify con times(), never(), atLeastOnce()
     * - InOrder para verificar secuencia
     * - ArgumentCaptor para capturar argumentos
     * - ArgumentMatchers (any(), eq(), anyString(), etc.)
     * - doThrow/doReturn para métodos void
     *
     * RECUERDE:
     * - Cada test debe ser independiente
     * - Use @BeforeEach para setup común
     * - Nombre sus tests descriptivamente
     * - Verifique tanto el resultado como las interacciones
     */


    // NUEVOS TESTS AGREGADOS
    // ==================== Proceso de Retiro ====================

    @Nested
    @DisplayName("Proceso de Retiro")
    class WithdrawalTests {

        @Test
        @DisplayName("Debe procesar retiro exitosamente con saldo suficiente y límite diario no alcanzado")
        void shouldProcessWithdrawalSuccessfully() {
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));
            when(transactionRepository.countTodayTransactionsByAccount("123")).thenReturn(10);
            when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-001");
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = transactionService.processWithdrawal("123", new BigDecimal("50000"), "Retiro efectivo");

            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
            assertEquals(new BigDecimal("50000"), account.getBalance());
            verify(notificationService).notifyTransactionCompleted(result);
        }

        @Test
        @DisplayName("Debe lanzar InsufficientFundsException si el saldo es insuficiente")
        void shouldThrowIfInsufficientFunds() {
            Account account = createActiveAccount("123", new BigDecimal("10000"));
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));
            when(transactionRepository.countTodayTransactionsByAccount("123")).thenReturn(0);

            assertThrows(InsufficientFundsException.class,
                    () -> transactionService.processWithdrawal("123", new BigDecimal("50000"), "Retiro"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si se alcanza el límite diario")
        void shouldThrowIfDailyLimitReached() {
            Account account = createActiveAccount("123", new BigDecimal("100000"));
            when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(account));
            when(transactionRepository.countTodayTransactionsByAccount("123")).thenReturn(50);

            TransactionNotAllowedException ex = assertThrows(TransactionNotAllowedException.class,
                    () -> transactionService.processWithdrawal("123", new BigDecimal("1000"), "Retiro"));
            assertEquals("Se ha alcanzado el límite diario de transacciones", ex.getMessage());
        }
    }

    // ==================== Historial de transacciones ====================

    @Nested
    @DisplayName("Historial de transacciones")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Debe lanzar AccountNotFoundException si la cuenta no existe")
        void shouldThrowIfAccountNotFound() {
            when(accountRepository.existsByAccountNumber("999")).thenReturn(false);

            assertThrows(AccountNotFoundException.class,
                    () -> transactionService.getTransactionHistory("999", null, null));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si startDate es posterior a endDate")
        void shouldThrowIfStartDateAfterEndDate() {
            when(accountRepository.existsByAccountNumber("123")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getTransactionHistory("123",
                            java.time.LocalDateTime.now().plusDays(1),
                            java.time.LocalDateTime.now()));
        }
    }

    // ==================== Cancelación de transacciones ====================

    @Nested
    @DisplayName("Cancelación de transacciones")
    class CancelTransactionTests {

        @Test
        @DisplayName("Debe cancelar transacción pendiente exitosamente")
        void shouldCancelPendingTransaction() {
            Transaction txn = new Transaction();
            txn.setTransactionId("TXN-001");
            txn.setStatus(TransactionStatus.PENDING);
            when(transactionRepository.findById("TXN-001")).thenReturn(Optional.of(txn));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = transactionService.cancelTransaction("TXN-001");
            assertEquals(TransactionStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la transacción no está pendiente")
        void shouldThrowIfTransactionNotPending() {
            Transaction txn = new Transaction();
            txn.setTransactionId("TXN-001");
            txn.setStatus(TransactionStatus.COMPLETED);
            when(transactionRepository.findById("TXN-001")).thenReturn(Optional.of(txn));

            assertThrows(TransactionNotAllowedException.class,
                    () -> transactionService.cancelTransaction("TXN-001"));
        }
    }

    // ==================== Cálculo de totales por tipo ====================

    @Nested
    @DisplayName("Cálculo de totales por tipo")
    class CalculateTotalByTypeTests {

        @Test
        @DisplayName("Debe calcular correctamente el total de depósitos completados")
        void shouldCalculateTotalDeposits() {
            when(accountRepository.existsByAccountNumber("123")).thenReturn(true);
            Transaction t1 = new Transaction();
            t1.setAmount(new BigDecimal("5000"));
            t1.setType(TransactionType.DEPOSIT);
            t1.setStatus(TransactionStatus.COMPLETED);
            Transaction t2 = new Transaction();
            t2.setAmount(new BigDecimal("7000"));
            t2.setType(TransactionType.DEPOSIT);
            t2.setStatus(TransactionStatus.COMPLETED);
            Transaction t3 = new Transaction();
            t3.setAmount(new BigDecimal("3000"));
            t3.setType(TransactionType.WITHDRAWAL);
            t3.setStatus(TransactionStatus.COMPLETED);

            when(transactionRepository.findByAccountAndDateRange(anyString(), any(), any()))
                    .thenReturn(java.util.List.of(t1, t2, t3));

            BigDecimal total = transactionService.calculateTotalByType("123", TransactionType.DEPOSIT, null, null);
            assertEquals(new BigDecimal("12000"), total);
        }
    }
}
