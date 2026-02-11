package com.financetracker.model;

import com.financetracker.model.enums.DateFormat;
import com.financetracker.model.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing user preferences and settings
 */
@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "date_format", length = 20)
    @Builder.Default
    private DateFormat dateFormat = DateFormat.MM_DD_YYYY;

    @Column(length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "budget_alerts")
    @Builder.Default
    private Boolean budgetAlerts = true;

    @Column(name = "monthly_summary")
    @Builder.Default
    private Boolean monthlySummary = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    @Column(length = 5)
    @Builder.Default
    private String language = "en";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
