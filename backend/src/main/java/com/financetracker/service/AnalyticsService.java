package com.financetracker.service;

import com.financetracker.dto.response.AnalyticsSummaryResponse;
import com.financetracker.model.Insight;
import com.financetracker.model.Transaction;import com.financetracker.model.Transaction;import com.financetracker.model.User;
import com.financetracker.model.enums.InsightType;
import com.financetracker.model.enums.Severity;
import com.financetracker.repository.InsightRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Service for analytics and insight generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final InsightRepository insightRepository;

    /**
     * Get monthly analytics summary
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getMonthAnalytics(User user, Integer month, Integer year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.getMonth().length(year % 4 == 0));

        BigDecimal totalIncome = transactionRepository.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.getTotalExpense(user, startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        BigDecimal savingsPercentage = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netSavings.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return AnalyticsSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .savingsPercentage(savingsPercentage)
                .period(month + "/" + year)
                .build();
    }

    /**
     * Get year-to-date analytics
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getYearToDateAnalytics(User user, Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.now();

        BigDecimal totalIncome = transactionRepository.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.getTotalExpense(user, startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        BigDecimal savingsPercentage = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netSavings.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return AnalyticsSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .savingsPercentage(savingsPercentage)
                .period("YTD " + year)
                .build();
    }

    /**
     * Get custom period analytics
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getCustomPeriodAnalytics(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.getTotalExpense(user, startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        BigDecimal savingsPercentage = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netSavings.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return AnalyticsSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .savingsPercentage(savingsPercentage)
                .period(startDate + " to " + endDate)
                .build();
    }

    /**
     * Generate insights - analyze spending patterns
     */
    @Transactional
    public void generateInsights(User user) {
        log.info("Generating insights for user: {}", user.getId());

        // Check for overspending
        checkForOverspending(user);

        // Check for spending trends
        checkSpendingTrends(user);

        // Check for unusual transactions
        checkUnusualTransactions(user);

        // Check for savings opportunity
        checkSavingsOpportunity(user);
    }

    /**
     * Check for overspending patterns
     */
    private void checkForOverspending(User user) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate now = LocalDate.now();

        BigDecimal last30DaysExpense = transactionRepository.getTotalExpense(user, thirtyDaysAgo, now);
        BigDecimal last30DaysIncome = transactionRepository.getTotalIncome(user, thirtyDaysAgo, now);

        if (last30DaysExpense != null && last30DaysIncome != null &&
                last30DaysExpense.compareTo(last30DaysIncome) > 0) {

            Insight insight = Insight.builder()
                    .user(user)
                    .insightType(InsightType.OVERSPENDING)
                    .title("Overspending Detected")
                    .description(String.format(
                            "In the last 30 days, you've spent $%.2f while earning $%.2f. Consider reducing expenses.",
                            last30DaysExpense,
                            last30DaysIncome
                    ))
                    .severity(Severity.CRITICAL)
                    .isDismissed(false)
                    .build();

            insightRepository.save(insight);
            log.info("Overspending insight created for user: {}", user.getId());
        }
    }

    /**
     * Check for spending trends
     */
    private void checkSpendingTrends(User user) {
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate now = LocalDate.now();

        BigDecimal last30Days = transactionRepository.getTotalExpense(user, thirtyDaysAgo, now);
        BigDecimal previous30Days = transactionRepository.getTotalExpense(user, sixtyDaysAgo, thirtyDaysAgo);

        if (last30Days != null && previous30Days != null && previous30Days.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentageChange = last30Days.subtract(previous30Days)
                    .divide(previous30Days, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            if (percentageChange.compareTo(new BigDecimal("20")) > 0) {
                Insight insight = Insight.builder()
                        .user(user)
                        .insightType(InsightType.TREND_UP)
                        .title("Spending Increase")
                        .description(String.format(
                                "Your spending has increased by %.2f%% compared to the previous month.",
                                percentageChange
                        ))
                        .severity(Severity.WARNING)
                        .isDismissed(false)
                        .build();

                insightRepository.save(insight);
            }
        }
    }

    /**
     * Check for unusual transactions
     */
    private void checkUnusualTransactions(User user) {
        // Get average transaction amount for last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate now = LocalDate.now();
        
        List<Transaction> recentTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()) &&
                        t.getTransactionDate().isAfter(thirtyDaysAgo.minusDays(1)))
                .collect(java.util.stream.Collectors.toList());

        if (recentTransactions.size() > 10) {
            BigDecimal total = recentTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal average = total.divide(new BigDecimal(recentTransactions.size()), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal threshold = average.multiply(new BigDecimal("2.5")); // 2.5x average

            for (Transaction transaction : recentTransactions) {
                if (transaction.getAmount().compareTo(threshold) > 0) {
                    Insight insight = Insight.builder()
                            .user(user)
                            .insightType(InsightType.UNUSUAL_ACTIVITY)
                            .title("Unusual Transaction Detected")
                            .description(String.format(
                                    "Transaction of $%.2f in %s category is significantly higher than your average (%.2f).",
                                    transaction.getAmount(),
                                    transaction.getCategory().getName(),
                                    average
                            ))
                            .severity(Severity.INFO)
                            .isDismissed(false)
                            .build();

                    insightRepository.save(insight);
                    break; // Only create one unusual transaction insight per cycle
                }
            }
        }
    }

    /**
     * Check for savings opportunity
     */
    private void checkSavingsOpportunity(User user) {
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
        LocalDate now = LocalDate.now();

        BigDecimal expense = transactionRepository.getTotalExpense(user, sixtyDaysAgo, now);

        if (expense != null && expense.compareTo(BigDecimal.ZERO) > 0) {
            Insight insight = Insight.builder()
                    .user(user)
                    .insightType(InsightType.LOW_SAVINGS)
                    .title("Savings Opportunity")
                    .description(String.format(
                            "You've spent $%.2f in the last 60 days. Set budget limits to help you save more.",
                            expense
                    ))
                    .severity(Severity.INFO)
                    .isDismissed(false)
                    .build();

            insightRepository.save(insight);
        }
    }

    /**
     * Get active insights for user
     */
    @Transactional(readOnly = true)
    public List<Insight> getActiveInsights(User user) {
        return insightRepository.findAll().stream()
                .filter(i -> i.getUser().getId().equals(user.getId()) && !i.getIsDismissed())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Dismiss insight
     */
    public void dismissInsight(Long insightId) {
        Insight insight = insightRepository.findById(insightId)
                .orElseThrow(() -> new IllegalArgumentException("Insight not found"));
        insight.setIsDismissed(true);
        insightRepository.save(insight);
        log.info("Insight dismissed: {}", insightId);
    }

    /**
     * Generate analytics report
     */
    public String generateReport(User user, LocalDate startDate, LocalDate endDate) {
        log.info("Generating report for user: {} from {} to {}", user.getId(), startDate, endDate);
        
        AnalyticsSummaryResponse analytics = getCustomPeriodAnalytics(user, startDate, endDate);
        
        StringBuilder report = new StringBuilder();
        report.append("FINANCIAL REPORT\n");
        report.append("================\n");
        report.append("Period: ").append(analytics.getPeriod()).append("\n");
        report.append("Total Income: $").append(analytics.getTotalIncome()).append("\n");
        report.append("Total Expense: $").append(analytics.getTotalExpense()).append("\n");
        report.append("Net Savings: $").append(analytics.getNetSavings()).append("\n");
        report.append("Savings Rate: ").append(analytics.getSavingsPercentage()).append("%\n");
        
        return report.toString();
    }
}
