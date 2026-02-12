package com.financetracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO for category creation/update request
 * Validates category name, type, color, and icon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Category type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #FF5733)")
    private String color;

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    @Pattern(regexp = "^[a-z_-]+$|^$", message = "Icon must be lowercase with underscores or hyphens")
    private String icon;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
