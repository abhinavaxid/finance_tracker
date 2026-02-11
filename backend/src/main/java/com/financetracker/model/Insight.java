package com.financetracker.model;

import com.financetracker.model.enums.InsightType;
import com.financetracker.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing smart insights generated for users
 */
@Entity
@Table(name = "insights", indexes = {
    @Index(name = "idx_insights_user_id", columnList = "user_id"),
    @Index(name = "idx_insights_insight_type", columnList = "insight_type"),
    @Index(name = "idx_insights_is_dismissed", columnList = "is_dismissed")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Insight type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false, length = 30)
    private InsightType insightType;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Severity severity = Severity.INFO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_dismissed")
    @Builder.Default
    private Boolean isDismissed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
