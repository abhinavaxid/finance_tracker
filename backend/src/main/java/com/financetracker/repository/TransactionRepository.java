package com.financetracker.repository;

import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.model.Category;
import com.financetracker.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity operations
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a user (paginated)
     */
    Page<Transaction> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);

    /**
     * Find transactions by user and date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    /**
     * Find transactions by user and category
     */
    Page<Transaction> findByUserAndCategoryOrderByTransactionDateDesc(User user, Category category, Pageable pageable);

    /**
     * Find transactions by user and type
     */
    Page<Transaction> findByUserAndTypeOrderByTransactionDateDesc(User user, TransactionType type, Pageable pageable);

    /**
     * Search transactions by description
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY t.transactionDate DESC")
    Page<Transaction> searchByDescription(
        @Param("user") User user,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    /**
     * Find transactions in amount range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserAndAmountRange(
        @Param("user") User user,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    /**
     * Get total income for user in date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = 'INCOME' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncome(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Get total expense for user in date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpense(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Get total by category for user in date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.category.id = :categoryId AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalByCategory(
        @Param("user") User user,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find transactions by user and status (not recurring)
     */
    List<Transaction> findByUserAndIsRecurringFalseOrderByTransactionDateDesc(User user);

    /**
     * Count transactions by user and type
     */
    long countByUserAndType(User user, TransactionType type);
}
