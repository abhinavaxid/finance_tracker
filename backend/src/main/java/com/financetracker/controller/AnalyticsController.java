package com.financetracker.controller;

import com.financetracker.dto.response.AnalyticsSummaryResponse;
import com.financetracker.model.Insight;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.AnalyticsService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for analytics and insights
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    /**
     * Get analytics for specific month
     * GET /api/analytics/month/{month}/year/{year}
     */
    @GetMapping("/month/{month}/year/{year}")
    public ResponseEntity<AnalyticsSummaryResponse> getMonthAnalytics(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        log.info("Fetching analytics for month: {}, year: {}", month, year);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        AnalyticsSummaryResponse response = analyticsService.getMonthAnalytics(user, month, year);
        return ResponseEntity.ok(response);
    }

    /**
     * Get AI-generated insights
     * GET /api/analytics/insights
     */
    @GetMapping("/insights")
    public ResponseEntity<Void> getInsights() {
        log.info("Fetching financial insights");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        analyticsService.generateInsights(user);
        return ResponseEntity.ok().build();
    }

    /**
     * Get active (non-dismissed) insights
     * GET /api/analytics/insights/active
     */
    @GetMapping("/insights/active")
    public ResponseEntity<List<Insight>> getActiveInsights() {
        log.info("Fetching active insights");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Insight> insights = analyticsService.getActiveInsights(user);
        return ResponseEntity.ok(insights);
    }

    /**
     * Dismiss an insight (mark as read)
     * PATCH /api/analytics/insights/{id}/dismiss
     */
    @PatchMapping("/insights/{id}/dismiss")
    public ResponseEntity<Void> dismissInsight(@PathVariable Long id) {
        log.info("Dismissing insight: {}", id);
        analyticsService.dismissInsight(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Export analytics report
     * GET /api/analytics/export
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportAnalyticsReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Exporting analytics report for date range: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String report = analyticsService.generateReport(user, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
