package com.bancolombia.evaluation.repository;

import com.bancolombia.evaluation.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de cuentas.
 */
public interface AccountRepository {

    /**
     * Busca una cuenta por su número.
     * @param accountNumber Número de cuenta
     * @return Optional con la cuenta si existe
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Busca todas las cuentas de un propietario.
     * @param ownerId ID del propietario
     * @return Lista de cuentas
     */
    List<Account> findByOwnerId(String ownerId);

    /**
     * Guarda o actualiza una cuenta.
     * @param account Cuenta a guardar
     * @return Cuenta guardada
     */
    Account save(Account account);

    /**
     * Verifica si existe una cuenta con el número dado.
     * @param accountNumber Número de cuenta
     * @return true si existe
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Cuenta el número total de cuentas de un propietario.
     * @param ownerId ID del propietario
     * @return Número de cuentas
     */
    int countByOwnerId(String ownerId);
}
