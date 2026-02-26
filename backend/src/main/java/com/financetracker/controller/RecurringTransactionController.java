package com.financetracker.controller;

import com.financetracker.dto.request.RecurringTransactionRequest;
import com.financetracker.dto.response.RecurringTransactionResponse;
import com.financetracker.model.RecurringTransaction;
import com.financetracker.model.User;
import com.financetracker.model.enums.Frequency;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.RecurringTransactionService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for recurring transaction management
 */
@RestController
@RequestMapping("/recurring-transactions")
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final UserRepository userRepository;

    /**
     * Create a new recurring transaction
     * POST /api/recurring-transactions
     */
    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurringTransaction(
            @Valid @RequestBody RecurringTransactionRequest request) {
        log.info("Creating recurring transaction");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        RecurringTransaction response = recurringTransactionService.createRecurringTransaction(
                user, request.getCategoryId(), request.getAmount(), request.getType(),
                request.getFrequency(), request.getStartDate(), request.getEndDate(),
                request.getDescription(), request.getPaymentMethod()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get recurring transaction by ID
     * GET /api/recurring-transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransaction> getRecurringTransaction(@PathVariable Long id) {
        log.info("Fetching recurring transaction: {}", id);
        RecurringTransaction response = recurringTransactionService.getRecurringTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active recurring transactions for user
     * GET /api/recurring-transactions
     */
    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getAllRecurringTransactions() {
        log.info("Fetching all recurring transactions");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<RecurringTransaction> responses = 
                recurringTransactionService.getActiveRecurringTransactions(user);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get due recurring transactions (ready to process)
     * GET /api/recurring-transactions/due
     */
    @GetMapping("/due")
    public ResponseEntity<List<RecurringTransaction>> getDueRecurringTransactions() {
        log.info("Fetching due recurring transactions");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<RecurringTransaction> responses = 
                recurringTransactionService.getDueTransactions(user);
        return ResponseEntity.ok(responses);
    }

    /**
     * Process due recurring transactions (create actual transactions)
     * POST /api/recurring-transactions/process
     */
    @PostMapping("/process")
    public ResponseEntity<Integer> processRecurringTransactions() {
        log.info("Processing due recurring transactions");
        int processedCount = recurringTransactionService.processRecurringTransactions();
        return ResponseEntity.ok(processedCount);
    }

    /**
     * Update recurring transaction
     * PUT /api/recurring-transactions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurringTransaction(
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        log.info("Updating recurring transaction: {}", id);
        
        RecurringTransaction response = recurringTransactionService.updateRecurringTransaction(
                id, request.getAmount(), 
                Frequency.valueOf(request.getFrequency().toUpperCase()),
                request.getStartDate(), request.getEndDate(), request.getDayOfMonth(),
                request.getDescription(), request.getPaymentMethod()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate recurring transaction
     * PATCH /api/recurring-transactions/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateRecurringTransaction(@PathVariable Long id) {
        log.info("Deactivating recurring transaction: {}", id);
        recurringTransactionService.deactivateRecurringTransaction(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete recurring transaction
     * DELETE /api/recurring-transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable Long id) {
        log.info("Deleting recurring transaction: {}", id);
        recurringTransactionService.deleteRecurringTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
