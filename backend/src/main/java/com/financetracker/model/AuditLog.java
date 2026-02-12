package com.financetracker.model;

import com.financetracker.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing audit log entries for tracking user actions and changes
 * 
 * Stores complete audit trail including:
 * - User actions (login, create, update, delete, etc.)
 * - Old and new values as JSON strings
 * - IP address and user agent for security tracking
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_log_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_log_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @NotBlank(message = "Entity type is required")
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Old values in JSON format (can be NULL for CREATE actions)
     */
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    /**
     * New values in JSON format
     */
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
