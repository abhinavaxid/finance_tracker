package com.financetracker.controller;

import com.financetracker.model.User;
import com.financetracker.model.UserPreference;
import com.financetracker.model.enums.DateFormat;
import com.financetracker.model.enums.Theme;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.UserPreferenceService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user preferences and settings
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Slf4j
public class PreferenceController {

    private final UserPreferenceService userPreferenceService;
    private final UserRepository userRepository;

    /**
     * Get user preferences
     * GET /api/preferences
     */
    @GetMapping
    public ResponseEntity<UserPreference> getPreferences() {
        log.info("Fetching user preferences");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreference response = userPreferenceService.getOrCreateUserPreference(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user preferences
     * PUT /api/preferences
     */
    @PutMapping
    public ResponseEntity<UserPreference> updatePreferences(
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String timezone,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean budgetAlerts,
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean monthlySummary) {
        log.info("Updating user preferences");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreference prefs = userPreferenceService.getOrCreateUserPreference(user);
        if (currency != null) prefs.setCurrency(currency);
        if (timezone != null) prefs.setTimezone(timezone);
        if (dateFormat != null) prefs.setDateFormat(DateFormat.valueOf(dateFormat.toUpperCase()));
        if (theme != null) prefs.setTheme(Theme.valueOf(theme.toUpperCase()));
        if (language != null) prefs.setLanguage(language);
        
        return ResponseEntity.ok(prefs);
    }

    /**
     * Set currency preference
     * PATCH /api/preferences/currency
     */
    @PatchMapping("/currency")
    public ResponseEntity<UserPreference> setCurrency(@RequestParam String currency) {
        log.info("Setting currency preference: {}", currency);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserPreference prefs = userPreferenceService.getOrCreateUserPreference(user);
        prefs.setCurrency(currency);
        userPreferenceService.updatePreferences(user, currency, null, null, null, null, null, null, null);
        
        return ResponseEntity.ok(prefs);
    }

    /**
     * Set timezone preference
     * PATCH /api/preferences/timezone
     */
    @PatchMapping("/timezone")
    public ResponseEntity<UserPreference> setTimezone(@RequestParam String timezone) {
        log.info("Setting timezone preference: {}", timezone);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserPreference prefs = userPreferenceService.getOrCreateUserPreference(user);
        
        return ResponseEntity.ok(prefs);
    }

    /**
     * Set theme preference
     * PATCH /api/preferences/theme
     */
    @PatchMapping("/theme")
    public ResponseEntity<UserPreference> setTheme(@RequestParam String theme) {
        log.info("Setting theme preference: {}", theme);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserPreference prefs = userPreferenceService.getOrCreateUserPreference(user);
        
        return ResponseEntity.ok(prefs);
    }

    /**
     * Set language preference
     * PATCH /api/preferences/language
     */
    @PatchMapping("/language")
    public ResponseEntity<UserPreference> setLanguage(@RequestParam String language) {
        log.info("Setting language preference: {}", language);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserPreference prefs = userPreferenceService.getOrCreateUserPreference(user);
        
        return ResponseEntity.ok(prefs);
    }
}
