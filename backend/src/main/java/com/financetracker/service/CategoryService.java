package com.financetracker.service;

import com.financetracker.dto.response.CategoryResponse;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.model.enums.TransactionType;
import com.financetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for category management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories for user (including defaults)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForUser(User user, String type) {
        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
        
        List<Category> categories = categoryRepository.findAllForUser(user, transactionType);
        
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get default categories for a type
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getDefaultCategories(String type) {
        List<Category> defaults = categoryRepository.findByIsDefaultTrueAndUserIsNullAndIsActiveTrue();
        
        return defaults.stream()
                .filter(c -> c.getType().name().equals(type.toUpperCase()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create custom category for user
     */
    public CategoryResponse createCategory(User user, String name, String type, String color, String icon) {
        log.info("Creating category for user {}: {} ({})", user.getId(), name, type);

        if (categoryRepository.existsByUserAndName(user, name)) {
            log.warn("Category already exists: {} for user {}", name, user.getId());
            throw new IllegalArgumentException("Category already exists for this user");
        }

        TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());

        Category category = Category.builder()
                .user(user)
                .name(name)
                .type(transactionType)
                .color(color != null ? color : "#000000")
                .icon(icon)
                .isDefault(false)
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created successfully: {}", category.getId());

        return mapToResponse(category);
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    /**
     * Update category
     */
    public CategoryResponse updateCategory(Long categoryId, String name, String color, String icon) {
        Category category = getCategoryById(categoryId);
        
        log.info("Updating category: {}", categoryId);

        if (name != null && !name.isEmpty()) {
            category.setName(name);
        }
        if (color != null && !color.isEmpty()) {
            category.setColor(color);
        }
        if (icon != null && !icon.isEmpty()) {
            category.setIcon(icon);
        }

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    /**
     * Delete category (soft delete)
     */
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        
        // Cannot delete default categories
        if (category.isSystemDefault()) {
            throw new IllegalArgumentException("Cannot delete default categories");
        }

        log.info("Deleting category: {}", categoryId);
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    /**
     * Verify user owns the category
     */
    @Transactional(readOnly = true)
    public boolean userOwnsCategory(User user, Long categoryId) {
        Category category = getCategoryById(categoryId);
        return category.getUser() == null || category.getUser().getId().equals(user.getId());
    }

    /**
     * Map Category entity to response DTO
     */
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType().name())
                .color(category.getColor())
                .icon(category.getIcon())
                .isDefault(category.getIsDefault())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
