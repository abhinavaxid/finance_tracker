package com.financetracker.service;

import com.financetracker.model.RecurringTransaction;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.model.enums.Frequency;
import com.financetracker.repository.RecurringTransactionRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing recurring transactions and their processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Get recurring transaction by ID
     */
    @Transactional(readOnly = true)
    public RecurringTransaction getRecurringTransactionById(Long recurringId) {
        return recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
    }

    /**
     * Get all due recurring transactions for a user
     */
    @Transactional(readOnly = true)
    public List<RecurringTransaction> getDueTransactions(User user) {
        return recurringTransactionRepository.findAll().stream()
                .filter(rt -> rt.getUser().getId().equals(user.getId()) && rt.isDue())
                .collect(Collectors.toList());
    }

    /**
     * Get all active recurring transactions for a user
     */
    @Transactional(readOnly = true)
    public List<RecurringTransaction> getActiveRecurringTransactions(User user) {
        return recurringTransactionRepository.findAll().stream()
                .filter(rt -> rt.getUser().getId().equals(user.getId()) && rt.getIsActive())
                .sorted((a, b) -> a.getNextOccurrence().compareTo(b.getNextOccurrence()))
                .collect(Collectors.toList());
    }

    /**
     * Process due recurring transactions - creates actual transactions
     */
    public int processRecurringTransactions() {
        log.info("Processing due recurring transactions...");
        
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository.findAll().stream()
                .filter(RecurringTransaction::isDue)
                .collect(Collectors.toList());
        
        int processedCount = 0;
        for (RecurringTransaction recurring : dueTransactions) {
            try {
                processRecurringTransaction(recurring);
                processedCount++;
            } catch (Exception e) {
                log.error("Error processing recurring transaction: {}", recurring.getId(), e);
            }
        }
        
        log.info("Processed {} recurring transactions", processedCount);
        return processedCount;
    }

    /**
     * Process a single recurring transaction
     */
    private void processRecurringTransaction(RecurringTransaction recurring) {
        LocalDate nextDate = recurring.getNextOccurrence();
        
        // Create transaction from recurring template
        Transaction transaction = Transaction.builder()
                .user(recurring.getUser())
                .category(recurring.getCategory())
                .amount(recurring.getAmount())
                .type(recurring.getType())
                .description(recurring.getDescription())
                .transactionDate(nextDate)
                .paymentMethod(recurring.getPaymentMethod())
                .referenceNumber(null)
                .tags(new String[0])
                .isRecurring(true)
                .build();
        
        transactionRepository.save(transaction);
        log.info("Created transaction from recurring transaction: {}", recurring.getId());
        
        // Calculate and update next occurrence date
        LocalDate nextOccurrence = calculateNextOccurrence(nextDate, recurring.getFrequency(), recurring.getDayOfMonth());
        
        // Check if end date is reached
        if (recurring.getEndDate() != null && nextOccurrence.isAfter(recurring.getEndDate())) {
            recurring.setIsActive(false);
            log.info("Recurring transaction {} ended", recurring.getId());
        } else {
            recurring.setNextOccurrence(nextOccurrence);
        }
        
        recurringTransactionRepository.save(recurring);
    }

    /**
     * Calculate next occurrence date based on frequency
     */
    private LocalDate calculateNextOccurrence(LocalDate currentDate, Frequency frequency, Integer dayOfMonth) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                // Use specified day of month if available
                if (dayOfMonth != null && dayOfMonth > 0) {
                    LocalDate nextMonth = currentDate.plusMonths(1).withDayOfMonth(dayOfMonth);
                    // Handle month with fewer days
                    int maxDay = nextMonth.getMonth().length(nextMonth.getYear() % 4 == 0);
                    if (dayOfMonth > maxDay) {
                        nextMonth = nextMonth.withDayOfMonth(maxDay);
                    }
                    return nextMonth;
                }
                return currentDate.plusMonths(1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            case YEARLY:
                return currentDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Unknown frequency: " + frequency);
        }
    }

    /**
     * Update recurring transaction
     */
    public RecurringTransaction updateRecurringTransaction(
            Long recurringId,
            java.math.BigDecimal amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            Integer dayOfMonth,
            String description,
            String paymentMethod) {

        RecurringTransaction recurring = getRecurringTransactionById(recurringId);
        
        log.info("Updating recurring transaction: {}", recurringId);

        if (amount != null) {
            recurring.setAmount(amount);
        }
        if (frequency != null) {
            recurring.setFrequency(frequency);
        }
        if (startDate != null) {
            recurring.setStartDate(startDate);
            recurring.setNextOccurrence(startDate);
        }
        if (endDate != null) {
            recurring.setEndDate(endDate);
        }
        if (dayOfMonth != null) {
            recurring.setDayOfMonth(dayOfMonth);
        }
        if (description != null) {
            recurring.setDescription(description);
        }
        if (paymentMethod != null) {
            recurring.setPaymentMethod(com.financetracker.model.enums.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        }

        return recurringTransactionRepository.save(recurring);
    }

    /**
     * Deactivate recurring transaction
     */
    public void deactivateRecurringTransaction(Long recurringId) {
        RecurringTransaction recurring = getRecurringTransactionById(recurringId);
        recurring.setIsActive(false);
        recurringTransactionRepository.save(recurring);
        log.info("Recurring transaction deactivated: {}", recurringId);
    }

    /**
     * Delete recurring transaction
     */
    public void deleteRecurringTransaction(Long recurringId) {
        RecurringTransaction recurring = getRecurringTransactionById(recurringId);
        log.info("Deleting recurring transaction: {}", recurringId);
        recurringTransactionRepository.delete(recurring);
    }

    /**
     * Verify user owns the recurring transaction
     */
    @Transactional(readOnly = true)
    public boolean userOwnsRecurringTransaction(User user, Long recurringId) {
        RecurringTransaction recurring = getRecurringTransactionById(recurringId);
        return recurring.getUser().getId().equals(user.getId());
    }
}
