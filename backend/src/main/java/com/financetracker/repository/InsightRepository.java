package com.financetracker.repository;

import com.financetracker.model.Insight;
import com.financetracker.model.User;
import com.financetracker.model.enums.InsightType;
import com.financetracker.model.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Insight entity operations
 */
@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {

    /**
     * Find insights for user (paginated)
     */
    Page<Insight> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find active insights for user
     */
    List<Insight> findByUserAndIsDismissedFalseOrderByCreatedAtDesc(User user);

    /**
     * Find insights by type
     */
    Page<Insight> findByUserAndInsightTypeOrderByCreatedAtDesc(User user, InsightType type, Pageable pageable);

    /**
     * Find critical insights
     */
    List<Insight> findByUserAndSeverityOrderByCreatedAtDesc(User user, Severity severity);

    /**
     * Find recent insights for user
     */
    List<Insight> findTop5ByUserAndIsDismissedFalseOrderByCreatedAtDesc(User user);
    
    // Alias method for backward compatibility
    default List<Insight> findRecentInsights(User user) {
        return findTop5ByUserAndIsDismissedFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Find insights by category
     */
    List<Insight> findByUserAndCategoryIdOrderByCreatedAtDesc(User user, Long categoryId);

    /**
     * Count active insights for user
     */
    long countByUserAndIsDismissedFalse(User user);

    /**
     * Find overspending insights
     */
    @Query("SELECT i FROM Insight i WHERE i.user = :user AND i.insightType = 'OVERSPENDING' AND i.isDismissed = false ORDER BY i.createdAt DESC")
    List<Insight> findOverspendingInsights(@Param("user") User user);
}
