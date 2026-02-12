package com.financetracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * DTO for user preference update request
 * Validates user preference settings for theme, currency, timezone, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceRequest {

    @Pattern(regexp = "^[A-Z]{3}$|^$", message = "Currency must be a valid ISO 4217 code (e.g., USD, EUR, GBP)")
    private String currency;

    @Pattern(regexp = "MM/DD/YYYY|DD/MM/YYYY|YYYY-MM-DD|^$", message = "Date format must be MM/DD/YYYY, DD/MM/YYYY, or YYYY-MM-DD")
    @JsonProperty("date_format")
    private String dateFormat;

    @Size(max = 50, message = "Timezone must be a valid IANA timezone")
    private String timezone;

    private Boolean emailNotifications = true;
    
    @JsonProperty("budget_alerts")
    private Boolean budgetAlerts = true;
    
    @JsonProperty("monthly_summary")
    private Boolean monthlySummary = true;

    @Pattern(regexp = "LIGHT|DARK|AUTO|^$", message = "Theme must be LIGHT, DARK, or AUTO")
    private String theme;

    @Pattern(regexp = "^[a-z]{2}$|^$", message = "Language must be a valid ISO 639-1 code (e.g., en, es, fr)")
    private String language;
}
