package com.financetracker.repository;

import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email and check if active
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Find active users created after a specific date
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.createdAt >= :date")
    java.util.List<User> findActiveUsersByDate(@Param("date") LocalDateTime date);

    /**
     * Count users with unverified emails
     */
    long countByEmailVerifiedFalse();

    /**
     * Find users with specific role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    java.util.List<User> findUsersByRole(@Param("roleName") String roleName);
}
