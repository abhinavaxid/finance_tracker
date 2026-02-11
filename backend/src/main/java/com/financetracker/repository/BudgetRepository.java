package com.financetracker.repository;

import com.financetracker.model.Budget;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Budget entity operations
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find budget for user, category, month and year
     */
    Optional<Budget> findByUserAndCategoryIdAndMonthAndYear(User user, Long categoryId, Integer month, Integer year);

    /**
     * Find all budgets for user in specific month/year
     */
    List<Budget> findByUserAndMonthAndYearOrderByCategory(User user, Integer month, Integer year);

    /**
     * Find all budgets for user
     */
    List<Budget> findByUserOrderByMonthDescYearDesc(User user);

    /**
     * Find budgets that need alert notification
     */
    @Query("SELECT b FROM Budget b WHERE b.alertSent = false AND b.spentAmount >= (b.amount * (b.alertThreshold / 100)) AND b.exceededAlertSent = false")
    List<Budget> findBudgetsNeedingAlert();

    /**
     * Find exceeded budgets
     */
    @Query("SELECT b FROM Budget b WHERE b.exceededAlertSent = false AND b.spentAmount > b.amount")
    List<Budget> findExceededBudgets();

    /**
     * Find budgets for user that exceeded
     */
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.spentAmount > b.amount")
    List<Budget> findUserExceededBudgets(@Param("user") User user);

    /**
     * Count total budgets for user
     */
    long countByUser(User user);

    /**
     * Find budgets for warning status
     */
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.spentAmount >= (b.amount * (b.alertThreshold / 100)) AND b.spentAmount < b.amount")
    List<Budget> findUserBudgetsWithWarning(@Param("user") User user);
}
