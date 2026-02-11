package com.financetracker.repository;

import com.financetracker.model.AuditLog;
import com.financetracker.model.User;
import com.financetracker.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity operations
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs for a user (paginated)
     */
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    /**
     * Find audit logs for user by action
     */
    Page<AuditLog> findByUserAndActionOrderByCreatedAtDesc(User user, AuditAction action, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    /**
     * Find audit logs for user by entity type
     */
    Page<AuditLog> findByUserAndEntityTypeOrderByCreatedAtDesc(User user, String entityType, Pageable pageable);

    /**
     * Find audit logs by entity ID
     */
    List<AuditLog> findByEntityIdOrderByCreatedAtDesc(Long entityId);

    /**
     * Find audit logs in date range
     */
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find recent user logins
     */
    @Query("SELECT al FROM AuditLog al WHERE al.user = :user AND al.action = 'LOGIN' ORDER BY al.createdAt DESC LIMIT 10")
    List<AuditLog> findRecentLogins(@Param("user") User user);

    /**
     * Find failed login attempts
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = 'FAILED_LOGIN' ORDER BY al.createdAt DESC")
    Page<AuditLog> findFailedLogins(Pageable pageable);

    /**
     * Find failed login attempts for user
     */
    @Query("SELECT al FROM AuditLog al WHERE al.user = :user AND al.action = 'FAILED_LOGIN' ORDER BY al.createdAt DESC")
    List<AuditLog> findUserFailedLogins(@Param("user") User user);
}
