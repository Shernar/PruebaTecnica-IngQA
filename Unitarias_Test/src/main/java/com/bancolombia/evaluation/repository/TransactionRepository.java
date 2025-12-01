package com.bancolombia.evaluation.repository;

import com.bancolombia.evaluation.model.Transaction;
import com.bancolombia.evaluation.model.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de transacciones.
 */
public interface TransactionRepository {

    /**
     * Busca una transacción por su ID.
     * @param transactionId ID de la transacción
     * @return Optional con la transacción si existe
     */
    Optional<Transaction> findById(String transactionId);

    /**
     * Busca transacciones por número de cuenta origen.
     * @param accountNumber Número de cuenta
     * @return Lista de transacciones
     */
    List<Transaction> findBySourceAccountNumber(String accountNumber);

    /**
     * Busca transacciones por número de cuenta destino.
     * @param accountNumber Número de cuenta
     * @return Lista de transacciones
     */
    List<Transaction> findByTargetAccountNumber(String accountNumber);

    /**
     * Guarda una transacción.
     * @param transaction Transacción a guardar
     * @return Transacción guardada
     */
    Transaction save(Transaction transaction);

    /**
     * Busca transacciones en un rango de fechas.
     * @param accountNumber Número de cuenta
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de transacciones
     */
    List<Transaction> findByAccountAndDateRange(String accountNumber, 
                                                 LocalDateTime startDate, 
                                                 LocalDateTime endDate);

    /**
     * Busca transacciones por estado.
     * @param status Estado de la transacción
     * @return Lista de transacciones
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Cuenta las transacciones de hoy para una cuenta.
     * @param accountNumber Número de cuenta
     * @return Número de transacciones
     */
    int countTodayTransactionsByAccount(String accountNumber);
}
