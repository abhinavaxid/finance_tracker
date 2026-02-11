package com.financetracker.model;

import com.financetracker.model.enums.Frequency;
import com.financetracker.model.enums.PaymentMethod;
import com.financetracker.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing recurring transactions (salary, rent, etc.)
 */
@Entity
@Table(name = "recurring_transactions", indexes = {
    @Index(name = "idx_recurring_user_id", columnList = "user_id"),
    @Index(name = "idx_recurring_next_occurrence", columnList = "next_occurrence"),
    @Index(name = "idx_recurring_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransaction {

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

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Frequency is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull(message = "Next occurrence date is required")
    @Column(name = "next_occurrence", nullable = false)
    private LocalDate nextOccurrence;

    @Min(1)
    @Max(31)
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if recurring transaction is still valid
     */
    @Transient
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return isActive && 
               today.isAfter(startDate.minusDays(1)) &&
               (endDate == null || today.isBefore(endDate.plusDays(1)));
    }

    /**
     * Check if it's time to create a new transaction
     */
    @Transient
    public boolean isDue() {
        return isValid() && !nextOccurrence.isAfter(LocalDate.now());
    }
}
