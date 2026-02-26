package com.financetracker.controller;

import com.financetracker.dto.request.BudgetRequest;
import com.financetracker.dto.response.BudgetResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.BudgetService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for budget management
 */
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new budget
     * POST /api/budgets
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request) {
        log.info("Creating budget");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        BudgetResponse budget = budgetService.createBudget(
                user, category, request.getAmount(),
                request.getMonth(), request.getYear(), 
                request.getAlertThreshold(), request.getNotes()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    /**
     * Get budget by ID
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudget(@PathVariable Long id) {
        log.info("Fetching budget: {}", id);
        Budget budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    /**
     * Get all budgets for current user
     * GET /api/budgets
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        log.info("Fetching all budgets");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<BudgetResponse> budgets = budgetService.getAllBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budgets for specific month
     * GET /api/budgets/month/{month}/year/{year}
     */
    @GetMapping("/month/{month}/year/{year}")
    public ResponseEntity<List<BudgetResponse>> getBudgetsForMonth(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        log.info("Fetching budgets for month: {}, year: {}", month, year);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<BudgetResponse> budgets = budgetService.getBudgetsForMonth(user, month, year);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budgets approaching limit (warning status)
     * GET /api/budgets/warning
     */
    @GetMapping("/warning")
    public ResponseEntity<List<BudgetResponse>> getWarningBudgets() {
        log.info("Fetching warning budgets");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<BudgetResponse> budgets = budgetService.getWarningBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budgets that have exceeded limit
     * GET /api/budgets/exceeded
     */
    @GetMapping("/exceeded")
    public ResponseEntity<List<BudgetResponse>> getExceededBudgets() {
        log.info("Fetching exceeded budgets");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<BudgetResponse> budgets = budgetService.getExceededBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Update budget
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        log.info("Updating budget: {}", id);
        
        BudgetResponse budget = budgetService.updateBudget(
                id, request.getAmount(), request.getAlertThreshold(), request.getNotes()
        );
        
        return ResponseEntity.ok(budget);
    }

    /**
     * Delete budget
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        log.info("Deleting budget: {}", id);
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
