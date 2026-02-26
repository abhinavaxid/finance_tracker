package com.financetracker.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for MCP-based transaction request
 * Contains data extracted from natural language by GLM-5/Claude
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCPTransactionRequest {

    /**
     * MCP action type (CREATE, READ, UPDATE, DELETE)
     */
    @JsonProperty("action")
    private String action;

    /**
     * Original natural language input from user
     * e.g., "Add ₹1200 for groceries today"
     */
    @JsonProperty("original_input")
    private String originalInput;

    /**
     * Transaction amount extracted by LLM
     * e.g., 1200.00
     */
    @JsonProperty("amount")
    private BigDecimal amount;

    /**
     * Category hint extracted by LLM
     * e.g., "groceries" or "Food & Dining"
     */
    @JsonProperty("category_hint")
    private String categoryHint;

    /**
     * Transaction type: INCOME or EXPENSE
     * If null, defaults to EXPENSE
     */
    @JsonProperty("type")
    private String type;

    /**
     * Transaction description
     * e.g., "groceries"
     */
    @JsonProperty("description")
    private String description;

    /**
     * Transaction date
     * If null, defaults to today
     */
    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    /**
     * Payment method if provided
     * e.g., "CREDIT_CARD", "UPI", "CASH"
     */
    @JsonProperty("payment_method")
    private String paymentMethod;

    /**
     * Transaction ID for READ/UPDATE/DELETE operations
     */
    @JsonProperty("transaction_id")
    private Long transactionId;

    /**
     * Confidence score from LLM (0-1)
     * Indicates how confident the LLM is about the extraction
     */
    @JsonProperty("confidence")
    private Double confidence;

    /**
     * Any clarification questions from LLM if multiple interpretations exist
     */
    @JsonProperty("clarification_needed")
    private Boolean clarificationNeeded;

    /**
     * Suggestions for user if extraction is ambiguous
     */
    @JsonProperty("suggestions")
    private String suggestions;
}
