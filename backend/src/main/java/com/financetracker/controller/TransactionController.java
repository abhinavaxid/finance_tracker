package com.financetracker.controller;

import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.TransactionService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for transaction management
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Create a new transaction
     * POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Creating transaction for amount: {}", request.getAmount());
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        TransactionResponse response = transactionService.createTransaction(
                user, category, request.getAmount(), request.getType(),
                request.getDescription(), request.getTransactionDate(),
                request.getPaymentMethod(), null, null
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get transaction by ID
     * GET /api/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.info("Fetching transaction: {}", id);
        Transaction response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for current user
     * GET /api/transactions
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching transactions - page: {}, size: {}", page, size);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.getUserTransactions(user, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get transactions by date range
     * GET /api/transactions/search/date-range
     */
    @GetMapping("/search/date-range")
    public ResponseEntity<Page<TransactionResponse>> getByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching transactions for date range: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.getTransactionsByDateRange(
                user, startDate, endDate, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Search transactions by description
     * GET /api/transactions/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(
            @RequestParam String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching transactions with description: {}", description);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.searchTransactions(
                user, description, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get total income for period
     * GET /api/transactions/analytics/income
     */
    @GetMapping("/analytics/income")
    public ResponseEntity<BigDecimal> getTotalIncome(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching total income for period: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal totalIncome = transactionService.getTotalIncome(user, startDate, endDate);
        return ResponseEntity.ok(totalIncome);
    }

    /**
     * Get total expense for period
     * GET /api/transactions/analytics/expense
     */
    @GetMapping("/analytics/expense")
    public ResponseEntity<BigDecimal> getTotalExpense(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching total expense for period: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal totalExpense = transactionService.getTotalExpense(user, startDate, endDate);
        return ResponseEntity.ok(totalExpense);
    }

    /**
     * Update transaction
     * PUT /api/transactions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        log.info("Updating transaction: {}", id);
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        TransactionResponse response = transactionService.updateTransaction(
                id, category, request.getAmount(), request.getType(),
                request.getDescription(), request.getTransactionDate(),
                request.getPaymentMethod(), null, null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete transaction
     * DELETE /api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("Deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
