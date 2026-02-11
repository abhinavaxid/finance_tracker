package com.financetracker.repository;

import com.financetracker.model.RecurringTransaction;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RecurringTransaction entity operations
 */
@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    /**
     * Find all active recurring transactions for a user
     */
    List<RecurringTransaction> findByUserAndIsActiveTrueOrderByNextOccurrence(User user);

    /**
     * Find recurring transactions that are due for processing
     */
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user = :user AND rt.isActive = true AND rt.nextOccurrence <= :date")
    List<RecurringTransaction> findDueTransactions(
        @Param("user") User user,
        @Param("date") LocalDate date
    );

    /**
     * Find all recurring transactions due today globally
     */
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.isActive = true AND rt.nextOccurrence <= :date")
    List<RecurringTransaction> findAllDueTransactions(@Param("date") LocalDate date);

    /**
     * Find active recurring transactions by category
     */
    List<RecurringTransaction> findByUserAndCategoryIdAndIsActiveTrueOrderByNextOccurrence(User user, Long categoryId);

    /**
     * Count active recurring transactions for user
     */
    long countByUserAndIsActiveTrue(User user);

    /**
     * Find recurring transactions between date range
     */
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user = :user AND rt.nextOccurrence BETWEEN :startDate AND :endDate ORDER BY rt.nextOccurrence")
    List<RecurringTransaction> findByUserAndNextOccurrenceBetween(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
