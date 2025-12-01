package com.bancolombia.evaluation.service;


import com.bancolombia.evaluation.exception.*;
import com.bancolombia.evaluation.external.AuditService;
import com.bancolombia.evaluation.external.FraudDetectionService;
import com.bancolombia.evaluation.external.NotificationService;
import com.bancolombia.evaluation.model.*;
import com.bancolombia.evaluation.repository.AccountRepository;
import com.bancolombia.evaluation.repository.TransactionRepository;
import com.sun.nio.sctp.MessageInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessor - Unit Tests Avanzados")
class PaymentProcessorAdvancedTest {

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

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    private Account activeAccount;

    @BeforeEach
    void setup() {
        activeAccount = new Account();
        activeAccount.setAccountNumber("ACC123");
        activeAccount.setBalance(new BigDecimal("2000000"));
        activeAccount.setStatus(AccountStatus.ACTIVE);
    }

    private PaymentRequest createPayment(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setSourceAccountNumber("ACC123");
        request.setMerchantId("MERCHANT123");
        request.setAmount(amount);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setDescription("Pago de prueba");
        return request;
    }

    @Nested
    @DisplayName("Escenarios exitosos de negocio")
    class SuccessfulPayments {

        @Test
        @DisplayName("Pago exitoso con notificación y auditoría en orden")
        void shouldProcessPaymentSuccessfullyAndVerifyOrder() {
            PaymentRequest request = createPayment(new BigDecimal("100000"));

            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            when(fraudDetectionService.isBlacklisted(anyString())).thenReturn(false);
            when(fraudDetectionService.evaluateTransactionRisk(anyString(), any())).thenReturn(30);
            when(fraudDetectionService.validatePayment(any())).thenReturn(true);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
            when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-1");

            paymentProcessor.processPayment(request);

            InOrder inOrder = inOrder(auditService, transactionRepository, accountRepository, notificationService);

            inOrder.verify(auditService).logOperationStart(eq("PAYMENT"), eq("ACC123"), anyMap());
            inOrder.verify(accountRepository).save(any(Account.class));
            inOrder.verify(transactionRepository).save(any(Transaction.class));
            inOrder.verify(auditService).logBalanceChange(eq("ACC123"), any(BigDecimal.class), any(BigDecimal.class), anyString());
            inOrder.verify(auditService).logOperationSuccess(eq("AUDIT-1"), anyString());
            inOrder.verify(notificationService).sendPushNotification(eq("ACC123"), anyString(), anyString());
        }

        @Test
        @DisplayName("Pago de bajo riesgo con notificación simple")
        void shouldProcessLowRiskPayment() {
            PaymentRequest request = createPayment(new BigDecimal("50000"));

            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            when(fraudDetectionService.isBlacklisted(anyString())).thenReturn(false);
            when(fraudDetectionService.evaluateTransactionRisk(anyString(), any())).thenReturn(10);
            when(fraudDetectionService.validatePayment(any())).thenReturn(true);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
            when(auditService.logOperationStart(anyString(), anyString(), anyMap())).thenReturn("AUDIT-2");

            PaymentResult result = paymentProcessor.processPayment(request);

            assertTrue(result.isSuccessful());
            verify(notificationService, times(1)).sendPushNotification(anyString(), anyString(), anyString());
            verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Escenarios de excepciones y validaciones")
    class PaymentErrors {

        @Test
        @DisplayName("Cuenta bloqueada lanza excepción personalizada")
        void shouldThrowIfAccountBlocked() {
            activeAccount.setStatus(AccountStatus.BLOCKED);
            PaymentRequest request = createPayment(new BigDecimal("100000"));
            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));

            TransactionNotAllowedException ex = assertThrows(TransactionNotAllowedException.class,
                    () -> paymentProcessor.processPayment(request));

            assertTrue(ex.getMessage().contains("La cuenta se encuentra bloqueada"));
        }

        @Test
        @DisplayName("Pago fuera de horario de negocio para monto sospechoso")
        void shouldRejectHighAmountOutsideBusinessHours() {
            PaymentRequest request = createPayment(new BigDecimal("15000000"));
            lenient().when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            lenient().when(fraudDetectionService.isBlacklisted(anyString())).thenReturn(false);
            lenient().when(fraudDetectionService.evaluateTransactionRisk(anyString(), any())).thenReturn(20);

            TransactionNotAllowedException ex = assertThrows(TransactionNotAllowedException.class,
                    () -> paymentProcessor.processPayment(request));

            assertTrue(ex.getMessage().contains("Pagos de alto monto no permitidos en fines de semana"));
        }

        @Test
        @DisplayName("Pago de alto riesgo reporta actividad sospechosa")
        void shouldReportHighRiskPayment() {
            PaymentRequest request = createPayment(new BigDecimal("500000"));

            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            when(fraudDetectionService.isBlacklisted(anyString())).thenReturn(false);
            when(fraudDetectionService.evaluateTransactionRisk(anyString(), any())).thenReturn(75);

            PaymentResult result = paymentProcessor.processPayment(request);

            assertFalse(result.isSuccessful());
            assertEquals("Transacción rechazada por políticas de seguridad", result.getMessage());
            verify(fraudDetectionService).reportSuspiciousActivity(anyString(), contains("alto riesgo"));
        }

        @Test
        @DisplayName("Saldo insuficiente lanza InsufficientFundsException")
        void shouldFailIfInsufficientFunds() {
            PaymentRequest request = createPayment(new BigDecimal("5000000"));
            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            when(fraudDetectionService.isBlacklisted(anyString())).thenReturn(false);
            when(fraudDetectionService.evaluateTransactionRisk(anyString(), any())).thenReturn(10);
            lenient().when(fraudDetectionService.validatePayment(any())).thenReturn(true);

            assertThrows(InsufficientFundsException.class, () -> paymentProcessor.processPayment(request));
        }
    }

    @Nested
    @DisplayName("Escenarios de reembolsos")
    class RefundTests {

        @Test
        @DisplayName("Reembolso parcial exitoso con auditoría y notificación")
        void shouldProcessRefundSuccessfully() {
            Transaction original = new Transaction();
            original.setTransactionId("TXN123");
            original.setSourceAccountNumber("ACC123");
            original.setAmount(new BigDecimal("100000"));
            original.setStatus(TransactionStatus.COMPLETED);
            original.setType(TransactionType.PAYMENT);

            when(transactionRepository.findById("TXN123")).thenReturn(Optional.of(original));
            when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(activeAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult result = paymentProcessor.processRefund("TXN123", new BigDecimal("50000"));

            assertTrue(result.isSuccessful());
            verify(notificationService).notifyTransactionCompleted(any(Transaction.class));
            verify(auditService).logBalanceChange(eq("ACC123"), any(BigDecimal.class), any(BigDecimal.class), anyString());
        }
    }
}