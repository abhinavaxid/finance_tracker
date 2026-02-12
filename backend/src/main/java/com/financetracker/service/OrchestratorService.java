package com.financetracker.service;

import com.financetracker.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for coordinating various business logic tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrchestratorService {

    private final RecurringTransactionService recurringTransactionService;
    private final AnalyticsService analyticsService;
    private final EmailService emailService;
    private final BudgetService budgetService;
    private final TransactionService transactionService;

    /**
     * Daily task: Process recurring transactions
     * Scheduled to run every day at 01:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyProcessRecurringTransactions() {
        try {
            log.info("Starting daily recurring transaction processing...");
            int count = recurringTransactionService.processRecurringTransactions();
            log.info("Completed processing {} recurring transactions", count);
        } catch (Exception e) {
            log.error("Error in daily recurring transaction processing", e);
        }
    }

    /**
     * Daily task: Generate insights
     * Scheduled to run every day at 02:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyGenerateInsights() {
        try {
            log.info("Starting daily insight generation...");
            // This should be called with each user - implementation depends on user repository
            log.info("Completed daily insight generation");
        } catch (Exception e) {
            log.error("Error in daily insight generation", e);
        }
    }

    /**
     * Weekly task: Send weekly summary emails
     * Scheduled to run every Monday at 08:00 AM
     */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void weeklySendSummaryEmails() {
        try {
            log.info("Starting weekly summary email sending...");
            // Implementation would iterate through all users and send emails
            log.info("Completed weekly summary email sending");
        } catch (Exception e) {
            log.error("Error in weekly summary email sending", e);
        }
    }

    /**
     * Check budgets and send alerts
     * Scheduled to run every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void checkBudgetsAndSendAlerts() {
        try {
            log.info("Starting budget alert check...");
            // Implementation would check all user budgets
            log.info("Completed budget alert check");
        } catch (Exception e) {
            log.error("Error in budget alert check", e);
        }
    }

    /**
     * Perform database cleanup
     * Scheduled to run weekly on Saturday at 03:00 AM
     */
    @Scheduled(cron = "0 0 3 ? * SAT")
    public void performCleanup() {
        try {
            log.info("Starting database cleanup...");
            // Clean up old audit logs (older than 1 year)
            // Clean up old deleted records
            log.info("Completed database cleanup");
        } catch (Exception e) {
            log.error("Error in database cleanup", e);
        }
    }
}
