package com.financetracker.controller;

import com.financetracker.dto.request.CategoryRequest;
import com.financetracker.dto.response.CategoryResponse;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.CategoryService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for category management
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    /**
     * Create a new category
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CategoryResponse response = categoryService.createCategory(
                user, request.getName(), request.getType(), request.getColor(), request.getIcon()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        log.info("Fetching category: {}", id);
        Category response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all categories for current user by type
     * GET /api/categories?type=INCOME
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam String type) {
        log.info("Fetching all categories for user with type: {}", type);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<CategoryResponse> responses = categoryService.getAllCategoriesForUser(user, type);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get only system default categories
     * GET /api/categories/system/defaults
     */
    @GetMapping("/system/defaults")
    public ResponseEntity<List<CategoryResponse>> getDefaultCategories() {
        log.info("Fetching system default categories");
        // Default categories fetched during getAllCategories
        List<CategoryResponse> responses = new java.util.ArrayList<>();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Updating category: {}", id);
        
        CategoryResponse response = categoryService.updateCategory(
                id, request.getName(), request.getColor(), request.getIcon()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete category
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
