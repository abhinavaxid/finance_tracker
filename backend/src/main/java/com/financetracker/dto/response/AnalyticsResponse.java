package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for financial analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    
    private Integer month;
    
    private Integer year;
    
    @JsonProperty("total_income")
    private BigDecimal totalIncome;
    
    @JsonProperty("total_expense")
    private BigDecimal totalExpense;
    
    @JsonProperty("net_balance")
    private BigDecimal netBalance;
    
    @JsonProperty("savings_rate")
    private BigDecimal savingsRate;
    
    @JsonProperty("category_breakdown")
    private Map<String, BigDecimal> categoryBreakdown;
    
    @JsonProperty("transaction_count")
    private Integer transactionCount;
    
    @JsonProperty("average_transaction")
    private BigDecimal averageTransaction;
    
    private String summary;
}
