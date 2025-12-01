package com.bancolombia.evaluation.service;

import com.bancolombia.evaluation.exception.AccountValidationException;
import com.bancolombia.evaluation.model.Account;
import com.bancolombia.evaluation.model.AccountStatus;
import com.bancolombia.evaluation.model.AccountType;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * NIVEL 1: BÁSICO
 * 
 * Validador de cuentas bancarias.
 * 
 * Esta clase contiene lógica de validación pura sin dependencias externas,
 * ideal para evaluar conocimientos básicos de JUnit 5.
 * 
 * El candidato debe demostrar:
 * - Uso de @Test y @DisplayName
 * - Assertions básicas (assertEquals, assertTrue, assertFalse)
 * - Tests de excepciones con assertThrows
 * - Manejo de casos de borde (nulls, strings vacíos, valores límite)
 */
public class AccountValidator {

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{10,16}$");
    private static final Pattern OWNER_ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{6,10}$");
    private static final int MIN_OWNER_NAME_LENGTH = 3;
    private static final int MAX_OWNER_NAME_LENGTH = 100;
    private static final BigDecimal MINIMUM_OPENING_BALANCE = new BigDecimal("50000");
    private static final BigDecimal MAXIMUM_INITIAL_BALANCE = new BigDecimal("500000000");

    /**
     * Valida el formato de un número de cuenta.
     * El número debe contener solo dígitos y tener entre 10 y 16 caracteres.
     * 
     * @param accountNumber Número de cuenta a validar
     * @return true si el formato es válido
     * @throws AccountValidationException si el número es nulo o vacío
     */
    public boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountValidationException("accountNumber", 
                "El número de cuenta no puede ser nulo o vacío");
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
    }

    /**
     * Valida el formato del ID del propietario.
     * El formato debe ser: 2 letras mayúsculas seguidas de 6 a 10 dígitos.
     * Ejemplo: CC12345678, CE1234567890
     * 
     * @param ownerId ID del propietario
     * @return true si el formato es válido
     */
    public boolean isValidOwnerId(String ownerId) {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            return false;
        }
        return OWNER_ID_PATTERN.matcher(ownerId).matches();
    }

    /**
     * Valida el nombre del propietario.
     * - No puede ser nulo o vacío
     * - Debe tener entre 3 y 100 caracteres
     * - No puede contener solo espacios
     * - No puede contener números
     * 
     * @param ownerName Nombre del propietario
     * @return true si el nombre es válido
     */
    public boolean isValidOwnerName(String ownerName) {
        if (ownerName == null) {
            return false;
        }
        
        String trimmed = ownerName.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        
        if (trimmed.length() < MIN_OWNER_NAME_LENGTH || 
            trimmed.length() > MAX_OWNER_NAME_LENGTH) {
            return false;
        }
        
        // No debe contener números
        return !trimmed.matches(".*\\d.*");
    }

    /**
     * Valida el saldo inicial de una cuenta.
     * - No puede ser nulo
     * - Debe ser mayor o igual al mínimo de apertura (50,000)
     * - No puede exceder el máximo inicial (500,000,000)
     * 
     * @param balance Saldo a validar
     * @return true si el saldo es válido para apertura
     * @throws AccountValidationException si el saldo es nulo
     */
    public boolean isValidOpeningBalance(BigDecimal balance) {
        if (balance == null) {
            throw new AccountValidationException("balance", 
                "El saldo no puede ser nulo");
        }
        
        return balance.compareTo(MINIMUM_OPENING_BALANCE) >= 0 && 
               balance.compareTo(MAXIMUM_INITIAL_BALANCE) <= 0;
    }

    /**
     * Determina si un tipo de cuenta requiere un saldo mínimo más alto.
     * Las cuentas BUSINESS y PREMIUM requieren saldo mínimo de 1,000,000
     * 
     * @param type Tipo de cuenta
     * @return Saldo mínimo requerido para el tipo de cuenta
     */
    public BigDecimal getMinimumBalanceForAccountType(AccountType type) {
        if (type == null) {
            throw new AccountValidationException("accountType", 
                "El tipo de cuenta no puede ser nulo");
        }
        
        return switch (type) {
            case BUSINESS -> new BigDecimal("1000000");
            case PREMIUM -> new BigDecimal("5000000");
            case SAVINGS, CHECKING -> MINIMUM_OPENING_BALANCE;
        };
    }

    /**
     * Valida si una cuenta puede realizar operaciones.
     * Solo las cuentas ACTIVE pueden operar.
     * 
     * @param account Cuenta a validar
     * @return true si la cuenta puede operar
     * @throws AccountValidationException si la cuenta es nula
     */
    public boolean canPerformOperations(Account account) {
        if (account == null) {
            throw new AccountValidationException("account",
                "La cuenta no puede ser nula");
        }
        return account.getStatus() == AccountStatus.ACTIVE;
    }

    /**
     * Valida completamente una cuenta antes de crearla.
     * 
     * @param account Cuenta a validar
     * @throws AccountValidationException si alguna validación falla
     */
    public void validateForCreation(Account account) {
        if (account == null) {
            throw new AccountValidationException("account", 
                "La cuenta no puede ser nula");
        }

        if (!isValidAccountNumber(account.getAccountNumber())) {
            throw new AccountValidationException("accountNumber", 
                "Formato de número de cuenta inválido");
        }

        if (!isValidOwnerId(account.getOwnerId())) {
            throw new AccountValidationException("ownerId", 
                "Formato de ID de propietario inválido");
        }

        if (!isValidOwnerName(account.getOwnerName())) {
            throw new AccountValidationException("ownerName", 
                "Nombre de propietario inválido");
        }

        if (account.getType() == null) {
            throw new AccountValidationException("accountType", 
                "El tipo de cuenta es requerido");
        }

        BigDecimal minimumBalance = getMinimumBalanceForAccountType(account.getType());
        if (account.getBalance() == null || 
            account.getBalance().compareTo(minimumBalance) < 0) {
            throw new AccountValidationException("balance", 
                "El saldo inicial no cumple con el mínimo requerido para este tipo de cuenta: " 
                + minimumBalance);
        }

        if (!isValidOpeningBalance(account.getBalance())) {
            throw new AccountValidationException("balance", 
                "El saldo inicial excede el máximo permitido");
        }
    }

    /**
     * Calcula el máximo número de cuentas permitidas para un tipo de cliente.
     * 
     * @param isBusinessClient true si es cliente empresarial
     * @param currentAccountCount Número actual de cuentas
     * @return Número de cuentas adicionales permitidas
     */
    public int calculateAllowedAdditionalAccounts(boolean isBusinessClient, int currentAccountCount) {
        if (currentAccountCount < 0) {
            throw new IllegalArgumentException("El conteo de cuentas no puede ser negativo");
        }
        
        int maxAccounts = isBusinessClient ? 10 : 5;
        return Math.max(0, maxAccounts - currentAccountCount);
    }

    /**
     * Verifica si dos cuentas pertenecen al mismo propietario.
     * 
     * @param account1 Primera cuenta
     * @param account2 Segunda cuenta
     * @return true si pertenecen al mismo propietario
     */
    public boolean belongToSameOwner(Account account1, Account account2) {
        if (account1 == null || account2 == null) {
            return false;
        }
        
        if (account1.getOwnerId() == null || account2.getOwnerId() == null) {
            return false;
        }
        
        return account1.getOwnerId().equals(account2.getOwnerId());
    }
}
