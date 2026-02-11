package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for budget response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private Integer month;
    private Integer year;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal percentageUsed;
    private BigDecimal alertThreshold;
    private String status; // NORMAL, WARNING, EXCEEDED
    private Boolean alertSent;
    private Boolean exceededAlertSent;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
