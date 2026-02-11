package com.financetracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing monthly budgets per category
 */
@Entity
@Table(name = "budgets", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category_id", "month", "year"})
    },
    indexes = {
        @Index(name = "idx_budgets_user_id", columnList = "user_id"),
        @Index(name = "idx_budgets_user_month_year", columnList = "user_id,month,year")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(nullable = false)
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    @Column(nullable = false)
    private Integer year;

    @Column(name = "spent_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0", message = "Alert threshold must be between 0 and 100")
    @DecimalMax(value = "100", message = "Alert threshold must be between 0 and 100")
    @Column(name = "alert_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = new BigDecimal("80.00");

    @Column(name = "alert_sent")
    @Builder.Default
    private Boolean alertSent = false;

    @Column(name = "exceeded_alert_sent")
    @Builder.Default
    private Boolean exceededAlertSent = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate percentage of budget used
     */
    @Transient
    public BigDecimal getPercentageUsed() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.divide(amount, 2, BigDecimal.ROUND_HALF_UP)
                         .multiply(new BigDecimal("100"));
    }

    /**
     * Get remaining budget amount
     */
    @Transient
    public BigDecimal getRemainingAmount() {
        return amount.subtract(spentAmount);
    }

    /**
     * Check if budget is exceeded
     */
    @Transient
    public boolean isExceeded() {
        return spentAmount.compareTo(amount) > 0;
    }

    /**
     * Check if budget has reached alert threshold
     */
    @Transient
    public boolean shouldAlert() {
        return getPercentageUsed().compareTo(alertThreshold) >= 0;
    }

    /**
     * Get budget status
     */
    @Transient
    public String getStatus() {
        if (isExceeded()) {
            return "EXCEEDED";
        } else if (shouldAlert()) {
            return "WARNING";
        } else {
            return "NORMAL";
        }
    }
}
