package com.financetracker.mcp;

import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.model.enums.TransactionType;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling MCP (Model Context Protocol) based transaction operations
 * Integrates with GLM-5/Claude for natural language processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MCPTransactionService {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;
    private final MCPCategoryResolver categoryResolver;
    private final TransactionRepository transactionRepository;

    /**
     * Create a transaction from MCP-extracted data
     * 
     * @param user The user creating the transaction
     * @param amount The transaction amount
     * @param categoryHint The category name/hint from MCP parsing
     * @param type Transaction type (INCOME/EXPENSE)
     * @param description Description of the transaction
     * @param transactionDate Date of the transaction (defaults to today if null)
     * @param paymentMethod Payment method (optional)
     * @return TransactionResponse with created transaction details
     * @throws MCPException if category cannot be resolved or transaction creation fails
     */
    public TransactionResponse createTransactionFromMCP(
            User user,
            BigDecimal amount,
            String categoryHint,
            String type,
            String description,
            LocalDate transactionDate,
            String paymentMethod) throws MCPException {

        log.info("Creating MCP transaction for user: {} with amount: {}, category: {}", 
                user.getId(), amount, categoryHint);

        // Validate inputs
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MCPException("Invalid amount: " + amount);
        }

        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }

        // Validate date is not in future
        if (transactionDate.isAfter(LocalDate.now())) {
            throw new MCPException("Transaction date cannot be in the future");
        }

        // Validate and parse type
        TransactionType transactionTypeEnum;
        if (type == null || type.trim().isEmpty()) {
            transactionTypeEnum = TransactionType.EXPENSE;
            log.debug("Type not specified, defaulting to EXPENSE");
        } else {
            try {
                transactionTypeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                transactionTypeEnum = TransactionType.EXPENSE;
                log.debug("Invalid type specified, defaulting to EXPENSE");
            }
        }

        // Get user's available categories for the transaction type
        List<Category> userCategories = categoryRepository.findAllForUser(user, transactionTypeEnum);

        // Resolve category from hint
        Optional<Category> resolvedCategory = categoryResolver.resolveCategory(categoryHint, userCategories);
        
        if (!resolvedCategory.isPresent()) {
            String suggestions = categoryResolver.getCategorySuggestions(userCategories);
            throw new MCPException("Could not match category '" + categoryHint + "'. Available categories: " + suggestions);
        }

        Category category = resolvedCategory.get();
        log.debug("Resolved category '{}' to '{}'", categoryHint, category.getName());

        // Create transaction using existing service
        TransactionResponse response = transactionService.createTransaction(
                user,
                category,
                amount,
                transactionTypeEnum.toString(),
                description,
                transactionDate,
                paymentMethod,
                null, // referenceNumber
                null  // tags
        );

        log.info("Successfully created MCP transaction with ID: {}", response.getId());
        return response;
    }

    /**
     * Delete a transaction by ID
     */
    public void deleteTransaction(Long transactionId, User user) throws MCPException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new MCPException("Transaction not found with ID: " + transactionId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new MCPException("Unauthorized: Transaction does not belong to the current user");
        }

        transactionRepository.deleteById(transactionId);
        log.info("Deleted transaction with ID: {}", transactionId);
    }

    /**
     * Get a transaction by ID
     */
    public TransactionResponse getTransaction(Long transactionId, User user) throws MCPException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new MCPException("Transaction not found with ID: " + transactionId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new MCPException("Unauthorized: Transaction does not belong to the current user");
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .categoryType(transaction.getCategory().getType().toString())
                .amount(transaction.getAmount())
                .type(transaction.getType().toString())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .paymentMethod(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().toString() : null)
                .referenceNumber(transaction.getReferenceNumber())
                .tags(transaction.getTags())
                .isRecurring(transaction.getIsRecurring())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
