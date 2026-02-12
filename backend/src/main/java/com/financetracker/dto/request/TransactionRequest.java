package com.financetracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for transaction creation/update request
 * Validates transaction details including amount, type, date, and payment method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    @JsonProperty("category_id")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999999.99")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters")
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    @Pattern(regexp = "CASH|CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|UPI|WALLET|OTHER", 
             message = "Invalid payment method")
    @JsonProperty("payment_method")
    private String paymentMethod;

    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    @JsonProperty("reference_number")
    private String referenceNumber;

    private String[] tags;
}
