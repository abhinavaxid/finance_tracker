package com.financetracker.dto.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financetracker.dto.response.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MCP-based transaction response
 * Contains action result and user-friendly message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MCPResponse {

    /**
     * Overall success status
     */
    private Boolean success;

    /**
     * User-friendly confirmation message
     * e.g., "✓ Added ₹1,200 to Food & Dining for today"
     */
    private String message;

    /**
     * The action that was performed
     */
    private String action;

    /**
     * The created/updated transaction (if applicable)
     */
    private TransactionResponse transaction;

    /**
     * Error code if operation failed
     */
    private String errorCode;

    /**
     * Error details/explanation
     */
    private String errorDetails;

    /**
     * Suggestions or next steps for user
     */
    private String suggestions;

    /**
     * Confidence score for the operation
     */
    private Double confidence;

    /**
     * Any clarification questions for user
     */
    private String clarificationQuestion;

    /**
     * Available options if clarification is needed
     */
    private String[] options;

    public static MCPResponse success(String message, TransactionResponse transaction) {
        return MCPResponse.builder()
                .success(true)
                .message(message)
                .transaction(transaction)
                .build();
    }

    public static MCPResponse error(String errorCode, String errorDetails) {
        return MCPResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .build();
    }

    public static MCPResponse clarification(String question, String[] options) {
        return MCPResponse.builder()
                .success(false)
                .clarificationQuestion(question)
                .options(options)
                .build();
    }
}
