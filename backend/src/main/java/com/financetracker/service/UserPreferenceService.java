package com.financetracker.service;

import com.financetracker.dto.response.UserPreferenceResponse;
import com.financetracker.model.User;
import com.financetracker.model.UserPreference;
import com.financetracker.model.enums.DateFormat;
import com.financetracker.model.enums.Theme;
import com.financetracker.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user preferences
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * Get or create user preferences
     */
    @Transactional
    public UserPreference getOrCreateUserPreference(User user) {
        return userPreferenceRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreference(user));
    }

    /**
     * Create default preferences for new user
     */
    private UserPreference createDefaultPreference(User user) {
        log.info("Creating default preferences for user: {}", user.getId());

        UserPreference preference = UserPreference.builder()
                .user(user)
                .currency("USD")
                .timezone("UTC")
                .dateFormat(DateFormat.MM_DD_YYYY)
                .theme(Theme.LIGHT)
                .language("en")
                .budgetAlerts(true)
                .emailNotifications(true)
                .monthlySummary(true)
                .build();

        return userPreferenceRepository.save(preference);
    }

    /**
     * Update user preferences
     */
    public UserPreferenceResponse updatePreferences(
            User user,
            String currency,
            String timezone,
            String dateFormat,
            String theme,
            String language,
            Boolean budgetAlerts,
            Boolean emailNotifications,
            Boolean monthlySummary) {

        log.info("Updating preferences for user: {}", user.getId());

        UserPreference preference = getOrCreateUserPreference(user);

        if (currency != null && !currency.isEmpty()) {
            preference.setCurrency(currency);
        }
        if (timezone != null && !timezone.isEmpty()) {
            preference.setTimezone(timezone);
        }
        if (dateFormat != null && !dateFormat.isEmpty()) {
            preference.setDateFormat(DateFormat.valueOf(dateFormat.toUpperCase()));
        }
        if (theme != null && !theme.isEmpty()) {
            preference.setTheme(Theme.valueOf(theme.toUpperCase()));
        }
        if (language != null && !language.isEmpty()) {
            preference.setLanguage(language);
        }
        if (budgetAlerts != null) {
            preference.setBudgetAlerts(budgetAlerts);
        }
        if (emailNotifications != null) {
            preference.setEmailNotifications(emailNotifications);
        }
        if (monthlySummary != null) {
            preference.setMonthlySummary(monthlySummary);
        }

        preference = userPreferenceRepository.save(preference);
        return mapToResponse(preference);
    }

    /**
     * Get user preferences
     */
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreferences(User user) {
        UserPreference preference = getOrCreateUserPreference(user);
        return mapToResponse(preference);
    }

    /**
     * Check if budget alerts are enabled
     */
    @Transactional(readOnly = true)
    public boolean isBudgetAlertsEnabled(User user) {
        UserPreference preference = getOrCreateUserPreference(user);
        return preference.getBudgetAlerts();
    }

    /**
     * Check if email notifications are enabled
     */
    @Transactional(readOnly = true)
    public boolean isEmailNotificationsEnabled(User user) {
        UserPreference preference = getOrCreateUserPreference(user);
        return preference.getEmailNotifications();
    }

    /**
     * Map UserPreference entity to response DTO
     */
    private UserPreferenceResponse mapToResponse(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .currency(preference.getCurrency())
                .timezone(preference.getTimezone())
                .dateFormat(preference.getDateFormat().name())
                .theme(preference.getTheme().name())
                .language(preference.getLanguage())
                .budgetAlerts(preference.getBudgetAlerts())
                .emailNotifications(preference.getEmailNotifications())
                .monthlySummary(preference.getMonthlySummary())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
