package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for recurring transaction response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecurringTransactionResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String type;
    private String description;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextOccurrence;
    private Integer dayOfMonth;
    private Boolean isActive;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
