package com.financetracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * DTO for user preference update request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceRequest {

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (e.g., USD)")
    private String currency;

    @Pattern(regexp = "MM_DD_YYYY|DD_MM_YYYY|YYYY_MM_DD", message = "Invalid date format")
    private String dateFormat;

    private String timezone;

    private Boolean emailNotifications;
    private Boolean budgetAlerts;
    private Boolean monthlySummary;

    @Pattern(regexp = "LIGHT|DARK", message = "Theme must be LIGHT or DARK")
    private String theme;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (e.g., en)")
    private String language;
}
