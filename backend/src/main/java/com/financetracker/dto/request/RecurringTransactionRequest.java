package com.financetracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for recurring transaction creation/update request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionRequest {

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999999.99")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY", 
             message = "Frequency must be DAILY, WEEKLY, MONTHLY, QUARTERLY, or YEARLY")
    private String frequency;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    private LocalDate endDate;

    @Min(1)
    @Max(31)
    private Integer dayOfMonth;

    @Pattern(regexp = "CASH|CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|UPI|WALLET|OTHER", 
             message = "Invalid payment method")
    private String paymentMethod;
}
