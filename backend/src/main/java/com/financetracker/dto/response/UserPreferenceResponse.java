package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user preference response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPreferenceResponse {

    private Long id;
    private String currency;
    private String dateFormat;
    private String timezone;
    private Boolean emailNotifications;
    private Boolean budgetAlerts;
    private Boolean monthlySummary;
    private String theme;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
