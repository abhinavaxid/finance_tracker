package com.financetracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for budget creation/update request
 * Validates budget amount, category, and alert threshold
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotBlank(message = "Budget name is required")
    @Size(min = 2, max = 50, message = "Budget name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    @JsonProperty("category_id")
    private Long categoryId;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Budget amount cannot exceed 999999.99")
    private BigDecimal amount;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    @DecimalMin(value = "0", message = "Alert threshold must be between 0 and 100")
    @DecimalMax(value = "100", message = "Alert threshold must be between 0 and 100")
    @JsonProperty("alert_threshold")
    private BigDecimal alertThreshold;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
