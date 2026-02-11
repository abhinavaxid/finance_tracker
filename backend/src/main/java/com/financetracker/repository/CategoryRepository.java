package com.financetracker.repository;

import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by user and name
     */
    Optional<Category> findByUserAndNameAndType(User user, String name, TransactionType type);

    /**
     * Find all categories for a user including defaults
     */
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR (c.isDefault = true AND c.user IS NULL)) AND c.type = :type AND c.isActive = true ORDER BY c.user DESC")
    List<Category> findAllForUser(@Param("user") User user, @Param("type") TransactionType type);

    /**
     * Find all default categories
     */
    List<Category> findByIsDefaultTrueAndUserIsNullAndIsActiveTrue();

    /**
     * Find user categories only
     */
    List<Category> findByUserAndIsActiveTrueOrderByName(User user);

    /**
     * Find categories by type
     */
    List<Category> findByTypeAndIsActiveTrue(TransactionType type);

    /**
     * Count user categories for a specific type
     */
    long countByUserAndTypeAndIsActiveTrue(User user, TransactionType type);

    /**
     * Check if category exists for user
     */
    boolean existsByUserAndName(User user, String name);
}
