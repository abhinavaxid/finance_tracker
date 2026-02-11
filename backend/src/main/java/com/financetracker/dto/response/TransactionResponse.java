package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for transaction response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethod;
    private String referenceNumber;
    private String[] tags;
    private Boolean isRecurring;
    private Integer fileCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
