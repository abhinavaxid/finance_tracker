package com.financetracker.repository;

import com.financetracker.model.BudgetAlert;
import com.financetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BudgetAlert entity operations
 */
@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {

    /**
     * Find all alerts for a user (paginated)
     */
    Page<BudgetAlert> findByUserOrderBySentAtDesc(User user, Pageable pageable);

    /**
     * Find unread alerts for user
     */
    List<BudgetAlert> findByUserAndIsReadFalseOrderBySentAtDesc(User user);

    /**
     * Count unread alerts for user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Find alerts by budget
     */
    List<BudgetAlert> findByBudgetIdOrderBySentAtDesc(Long budgetId);

    /**
     * Find recent alerts for user
     */
    List<BudgetAlert> findTop10ByUserOrderBySentAtDesc(User user);
    
    // Alias method for backward compatibility
    default List<BudgetAlert> findRecentAlerts(User user) {
        return findTop10ByUserOrderBySentAtDesc(user);
    }
}
