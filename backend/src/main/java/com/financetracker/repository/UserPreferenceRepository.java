package com.financetracker.repository;

import com.financetracker.model.UserPreference;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserPreference entity operations
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * Find preferences by user
     */
    Optional<UserPreference> findByUser(User user);

    /**
     * Find preferences by user ID
     */
    Optional<UserPreference> findByUserId(Long userId);

    /**
     * Check if user has preferences
     */
    boolean existsByUser(User user);
}
