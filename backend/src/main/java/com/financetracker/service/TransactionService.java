package com.financetracker.service;

import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.model.enums.TransactionType;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Service for transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Create a new transaction
     */
    public TransactionResponse createTransaction(
            User user,
            Category category,
            BigDecimal amount,
            String type,
            String description,
            LocalDate transactionDate,
            String paymentMethodStr,
            String referenceNumber,
            String[] tags) {

        log.info("Creating transaction for user: {} with amount: {}", user.getId(), amount);

        // Parse payment method
        com.financetracker.model.enums.PaymentMethod paymentMethod = null;
        if (paymentMethodStr != null && !paymentMethodStr.isEmpty()) {
            paymentMethod = com.financetracker.model.enums.PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(amount)
                .type(TransactionType.valueOf(type.toUpperCase()))
                .description(description)
                .transactionDate(transactionDate)
                .paymentMethod(paymentMethod)
                .referenceNumber(referenceNumber)
                .tags(tags != null ? tags : new String[0])
                .isRecurring(false)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully: {}", transaction.getId());

        return mapToResponse(transaction);
    }

    /**
     * Get all transactions for user (paginated)
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get transactions in date range
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByDateRange(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return transactionRepository.findByUserAndDateRange(user, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get transactions by category
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByCategory(
            User user,
            Category category,
            Pageable pageable) {

        return transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get transactions by type
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByType(
            User user,
            String type,
            Pageable pageable) {

        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        return transactionRepository.findByUserAndTypeOrderByTransactionDateDesc(user, transactionType, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search transactions by description
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchTransactions(
            User user,
            String searchTerm,
            Pageable pageable) {

        return transactionRepository.searchByDescription(user, searchTerm, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    /**
     * Update transaction
     */
    public TransactionResponse updateTransaction(
            Long transactionId,
            Category category,
            BigDecimal amount,
            String type,
            String description,
            LocalDate transactionDate,
            String paymentMethodStr,
            String referenceNumber,
            String[] tags) {

        Transaction transaction = getTransactionById(transactionId);

        log.info("Updating transaction: {}", transactionId);

        if (category != null) {
            transaction.setCategory(category);
        }
        if (amount != null) {
            transaction.setAmount(amount);
        }
        if (type != null) {
            transaction.setType(TransactionType.valueOf(type.toUpperCase()));
        }
        if (description != null) {
            transaction.setDescription(description);
        }
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate);
        }
        if (paymentMethodStr != null) {
            transaction.setPaymentMethod(com.financetracker.model.enums.PaymentMethod.valueOf(paymentMethodStr.toUpperCase()));
        }
        if (referenceNumber != null) {
            transaction.setReferenceNumber(referenceNumber);
        }
        if (tags != null) {
            transaction.setTags(tags);
        }

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    /**
     * Delete transaction
     */
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        log.info("Deleting transaction: {}", transactionId);
        transactionRepository.delete(transaction);
    }

    /**
     * Get total income for user in date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalIncome(user, startDate, endDate);
    }

    /**
     * Get total expense for user in date range
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpense(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalExpense(user, startDate, endDate);
    }

    /**
     * Get total by category
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalByCategory(User user, Long categoryId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalByCategory(user, categoryId, startDate, endDate);
    }

    /**
     * Verify user owns the transaction
     */
    @Transactional(readOnly = true)
    public boolean userOwnsTransaction(User user, Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        return transaction.getUser().getId().equals(user.getId());
    }

    /**
     * Map Transaction entity to response DTO
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .categoryType(transaction.getCategory().getType().name())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .paymentMethod(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null)
                .referenceNumber(transaction.getReferenceNumber())
                .tags(transaction.getTags())
                .isRecurring(transaction.getIsRecurring())
                .fileCount(transaction.getFiles() != null ? transaction.getFiles().size() : 0)
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
