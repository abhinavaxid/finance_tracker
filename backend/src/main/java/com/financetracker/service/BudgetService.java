package com.financetracker.service;

import com.financetracker.dto.response.BudgetResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for budget management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;

    /**
     * Create a new budget
     */
    public BudgetResponse createBudget(
            User user,
            Category category,
            BigDecimal amount,
            Integer month,
            Integer year,
            BigDecimal alertThreshold,
            String notes) {

        log.info("Creating budget for user: {} in {}/{}", user.getId(), month, year);

        // Check if budget already exists
        if (budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, category.getId(), month, year).isPresent()) {
            throw new IllegalArgumentException("Budget already exists for this category and period");
        }

        // Validate month and year
        validateMonthYear(month, year);

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amount(amount)
                .month(month)
                .year(year)
                .spentAmount(BigDecimal.ZERO)
                .alertThreshold(alertThreshold != null ? alertThreshold : new BigDecimal("80.00"))
                .alertSent(false)
                .exceededAlertSent(false)
                .notes(notes)
                .build();

        budget = budgetRepository.save(budget);
        log.info("Budget created successfully: {}", budget.getId());

        return mapToResponse(budget);
    }

    /**
     * Get budget by ID
     */
    @Transactional(readOnly = true)
    public Budget getBudgetById(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
    }

    /**
     * Get budgets for specific month/year
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsForMonth(User user, Integer month, Integer year) {
        validateMonthYear(month, year);
        
        return budgetRepository.findByUserAndMonthAndYearOrderByCategory(user, month, year)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all budgets for user
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(User user) {
        return budgetRepository.findByUserOrderByMonthDescYearDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get budgets with warning status
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getWarningBudgets(User user) {
        return budgetRepository.findUserBudgetsWithWarning(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get exceeded budgets
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getExceededBudgets(User user) {
        return budgetRepository.findUserExceededBudgets(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update budget
     */
    public BudgetResponse updateBudget(
            Long budgetId,
            BigDecimal amount,
            BigDecimal alertThreshold,
            String notes) {

        Budget budget = getBudgetById(budgetId);

        log.info("Updating budget: {}", budgetId);

        if (amount != null) {
            budget.setAmount(amount);
        }
        if (alertThreshold != null) {
            budget.setAlertThreshold(alertThreshold);
        }
        if (notes != null) {
            budget.setNotes(notes);
        }

        // Reset alert flags if budget is updated
        budget.setAlertSent(false);
        budget.setExceededAlertSent(false);

        budget = budgetRepository.save(budget);
        return mapToResponse(budget);
    }

    /**
     * Mark alert as sent
     */
    public void markAlertSent(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        budget.setAlertSent(true);
        budgetRepository.save(budget);
    }

    /**
     * Mark exceeded alert as sent
     */
    public void markExceededAlertSent(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        budget.setExceededAlertSent(true);
        budgetRepository.save(budget);
    }

    /**
     * Delete budget
     */
    public void deleteBudget(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        log.info("Deleting budget: {}", budgetId);
        budgetRepository.delete(budget);
    }

    /**
     * Verify user owns the budget
     */
    @Transactional(readOnly = true)
    public boolean userOwnsBudget(User user, Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        return budget.getUser().getId().equals(user.getId());
    }

    /**
     * Validate month and year
     */
    private void validateMonthYear(Integer month, Integer year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }
    }

    /**
     * Map Budget entity to response DTO
     */
    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .spentAmount(budget.getSpentAmount())
                .remainingAmount(budget.getRemainingAmount())
                .percentageUsed(budget.getPercentageUsed())
                .alertThreshold(budget.getAlertThreshold())
                .status(budget.getStatus())
                .alertSent(budget.getAlertSent())
                .exceededAlertSent(budget.getExceededAlertSent())
                .notes(budget.getNotes())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
