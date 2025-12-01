package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.AccountValidationException;
import com.bancolombia.evaluation.model.Account;
import com.bancolombia.evaluation.model.AccountStatus;
import com.bancolombia.evaluation.model.AccountType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EJEMPLO DE TEST PARA REFERENCIA DEL CANDIDATO
 * <p>
 * Este archivo muestra algunas técnicas básicas de JUnit 5.
 * El candidato debe completar los tests faltantes y agregar más casos.
 * <p>
 * Este ejemplo cubre aproximadamente el 20% de los tests necesarios
 * para AccountValidator. El candidato debe llegar al 80%+ de cobertura.
 */
@DisplayName("AccountValidator - Tests de Ejemplo")
class AccountValidatorExampleTest {

    private AccountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountValidator();
    }

    // ==================== EJEMPLO: Tests básicos ====================

    @Nested
    @DisplayName("Validación de número de cuenta")
    class AccountNumberValidation {

        @Test
        @DisplayName("Debe aceptar número de cuenta válido de 10 dígitos")
        void shouldAcceptValidTenDigitAccountNumber() {
            // Arrange
            String accountNumber = "1234567890";
            // Act
            boolean result = validator.isValidAccountNumber(accountNumber);
            // Assert
            assertTrue(result, "Un número de 10 dígitos debería ser válido");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el número es null")
        void shouldThrowExceptionWhenAccountNumberIsNull() {
            // Act & Assert
            AccountValidationException exception = assertThrows(
                    AccountValidationException.class,
                    () -> validator.isValidAccountNumber(null)
            );

            assertEquals("accountNumber", exception.getField());
        }

        // TODO: El candidato debe agregar más tests para:
        // - Números con letras
        // - Números muy cortos (menos de 10 dígitos)
        // - Números muy largos (más de 16 dígitos)
        // - Números con espacios
        // - Números con caracteres especiales

        //NUEVOS TESTS AGREGADOS
        @ParameterizedTest(name = "Número válido: {0}")
        @DisplayName("Debe aceptar número de cuenta válido de 10 a 16 dígitos")
        @ValueSource(strings = {"1234567890", "1234567890123456"})
        void shouldAcceptValidAccountNumbers(String number) {
            assertTrue(validator.isValidAccountNumber(number));
        }

        @ParameterizedTest(name = "Número inválido: {0}")
        @ValueSource(strings = {"123", "12345678901234567", "12345A6789", "12 34567890", "!234567890"})
        @DisplayName("Debe rechazar números de cuenta inválidos")
        void shouldRejectInvalidAccountNumbers(String accountNumber) {
            assertFalse(validator.isValidAccountNumber(accountNumber));
        }
    }

    // ==================== EJEMPLO: Tests parametrizados ====================

    @Nested
    @DisplayName("Validación de ID de propietario - Parametrizados")
    class OwnerIdParameterizedTests {

        @ParameterizedTest(name = "ID válido: {0}")
        @ValueSource(strings = {"CC12345678", "CE1234567890", "PA123456"})
        @DisplayName("Debe aceptar IDs con formato correcto")
        void shouldAcceptValidOwnerIds(String ownerId) {
            assertTrue(validator.isValidOwnerId(ownerId));
        }

        @ParameterizedTest(name = "ID inválido: {0}")
        @NullAndEmptySource
        @ValueSource(strings = {"12345678", "CCABC", "cc12345678", "CC123"})
        @DisplayName("Debe rechazar IDs con formato incorrecto")
        void shouldRejectInvalidOwnerIds(String ownerId) {
            assertFalse(validator.isValidOwnerId(ownerId));
        }

        // TODO: El candidato debe agregar más casos usando @CsvSource


        // NUEVOS TESTS AGREGADO
        @ParameterizedTest(name = "ID inválido con caracteres especiales: {0}")
        @ValueSource(strings = {"CC1234!678", "AB12#3456"})
        @DisplayName("Debe rechazar IDs con caracteres especiales")
        void shouldRejectOwnerIdsWithSpecialCharacters(String ownerId) {
            assertFalse(validator.isValidOwnerId(ownerId));
        }
    }

    @Nested
    @DisplayName("Validación de saldo inicial")
    class BalanceValidation {

        @Test
        @DisplayName("Debe aceptar saldo mínimo y máximo permitido")
        void shouldAcceptValidBalances() {
            assertTrue(validator.isValidOpeningBalance(new BigDecimal("50000")));
            assertTrue(validator.isValidOpeningBalance(new BigDecimal("500000000")));
        }

        @Test
        @DisplayName("Debe rechazar saldo fuera de los límites")
        void shouldRejectInvalidBalances() {
            assertFalse(validator.isValidOpeningBalance(new BigDecimal("49999")));
            assertFalse(validator.isValidOpeningBalance(new BigDecimal("500000001")));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el saldo es null")
        void shouldThrowExceptionWhenBalanceIsNull() {
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.isValidOpeningBalance(null));
            assertEquals("balance", exception.getField());
        }
    }

    // ==================== TIPO DE CUENTA ====================

    // ==================== EJEMPLO: Tests con @CsvSource ====================
    @ParameterizedTest(name = "Tipo {0} requiere saldo mínimo de {1}")
    @CsvSource({
            "SAVINGS, 50000",
            "CHECKING, 50000",
            "BUSINESS, 1000000",
            "PREMIUM, 5000000"
    })
    @DisplayName("Debe retornar el saldo mínimo correcto por tipo de cuenta")
    void shouldReturnCorrectMinimumBalanceForAccountType(String typeName, String expectedBalance) {
        AccountType type = AccountType.valueOf(typeName);
        BigDecimal expected = new BigDecimal(expectedBalance);
        assertEquals(expected, validator.getMinimumBalanceForAccountType(type));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el tipo de cuenta es null")
    void shouldThrowExceptionWhenAccountTypeIsNull() {
        AccountValidationException exception = assertThrows(AccountValidationException.class,
                () -> validator.getMinimumBalanceForAccountType(null));
        assertEquals("accountType", exception.getField());
    }

    // ==================== OPERACIONES ====================

    @Test
    @DisplayName("Debe permitir operaciones solo si la cuenta está activa")
    void shouldAllowOperationsForActiveAccount() {
        Account account = new Account("1234567890", "Juan", "CC12345678",
                new BigDecimal("100000"), AccountType.SAVINGS);
        account.setStatus(AccountStatus.ACTIVE);
        assertTrue(validator.canPerformOperations(account));
    }

    @Test
    @DisplayName("Debe rechazar operaciones si la cuenta no está activa")
    void shouldRejectOperationsForInactiveAccount() {
        Account account = new Account("1234567890", "Juan", "CC12345678",
                new BigDecimal("100000"), AccountType.SAVINGS);
        account.setStatus(AccountStatus.INACTIVE);
        assertFalse(validator.canPerformOperations(account));
    }

    // ==================== EJEMPLO: Tests de excepción con assertThrows ====================
    @Test
    @DisplayName("Debe lanzar excepción si la cuenta es null")
    void shouldThrowExceptionWithDescriptiveMessageForNullAccount() {
        AccountValidationException exception = assertThrows(
                AccountValidationException.class,
                () -> validator.canPerformOperations(null)
        );

        assertAll(
                () -> assertEquals("account", exception.getField()),
                () -> assertTrue(exception.getMessage().contains("nula"))
        );
    }

    // ==================== EJEMPLO: Tests con objetos complejos ====================

    @Nested
    @DisplayName("Validación de cuenta para creación")
    class AccountCreationValidation {

        @Test
        @DisplayName("Debe aceptar cuenta con todos los datos válidos")
        void shouldAcceptValidAccountForCreation() {
            // Arrange
            Account account = new Account(
                    "1234567890",
                    "Juan Pérez",
                    "CC12345678",
                    new BigDecimal("100000"),
                    AccountType.SAVINGS
            );

            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> validator.validateForCreation(account));
        }

        // TODO: El candidato debe agregar tests para:
        // - Cuenta con número inválido
        // - Cuenta con propietario inválido
        // - Cuenta con saldo insuficiente para el tipo
        // - Cuenta con tipo null
        // - Combinaciones de errores

        // NUEVOS TESTS AGREGADOS

        @Test
        @DisplayName("Debe rechazar cuenta con número inválido")
        void shouldRejectAccountWithInvalidNumber() {
            Account account = new Account(
                    "123",
                    "Juan Pérez",
                    "CC12345678",
                    new BigDecimal("100000"),
                    AccountType.SAVINGS
            );
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(account));
            assertEquals("accountNumber", exception.getField());
        }

        @Test
        @DisplayName("Debe rechazar cuenta con ID de propietario inválido")
        void shouldRejectAccountWithInvalidOwnerId() {
            Account account = new Account(
                    "1234567890",
                    "Juan Pérez",
                    "12345678",
                    new BigDecimal("100000"),
                    AccountType.SAVINGS
            );
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(account));
            assertEquals("ownerId", exception.getField());
        }

        @Test
        @DisplayName("Debe rechazar cuenta con nombre inválido")
        void shouldRejectAccountWithInvalidOwnerName() {
            Account account = new Account(
                    "1234567890",
                    "Jo",
                    "CC12345678",
                    new BigDecimal("100000"),
                    AccountType.SAVINGS
            );
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(account));
            assertEquals("ownerName", exception.getField());
        }

        @Test
        @DisplayName("Debe rechazar cuenta con saldo insuficiente para el tipo")
        void shouldRejectAccountWithInsufficientBalanceForType() {
            Account account = new Account(
                    "1234567890",
                    "Juan Pérez",
                    "CC12345678",
                    new BigDecimal("100000"),
                    AccountType.BUSINESS
            );
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(account));
            assertEquals("balance", exception.getField());
        }

        @Test
        @DisplayName("Debe rechazar cuenta con tipo null")
        void shouldRejectAccountWithNullType() {
            Account account = new Account(
                    "1234567890",
                    "Juan Pérez",
                    "CC12345678",
                    new BigDecimal("100000"),
                    null
            );
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(account));
            assertEquals("accountType", exception.getField());
        }

        @Test
        @DisplayName("Debe rechazar cuenta null")
        void shouldRejectNullAccount() {
            AccountValidationException exception = assertThrows(AccountValidationException.class,
                    () -> validator.validateForCreation(null));
            assertEquals("account", exception.getField());
        }
    }

    // ==================== NOTA PARA EL CANDIDATO ====================
    /*
     * Este archivo es solo un EJEMPLO de cómo estructurar los tests.
     *
     * TAREAS PENDIENTES:
     * 1. Completar los tests marcados con TODO
     * 2. Agregar tests para todos los métodos de AccountValidator
     * 3. Alcanzar al menos 80% de cobertura de código
     * 4. Usar @Nested para organizar tests relacionados
     * 5. Usar nombres descriptivos con @DisplayName
     * 6. Seguir el patrón AAA (Arrange, Act, Assert)
     *
     * PISTAS:
     * - Revise los casos de borde (valores límite)
     * - Considere qué pasa con valores null
     * - Pruebe tanto casos positivos como negativos
     * - Use assertAll para múltiples verificaciones
     */
}
