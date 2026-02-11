package com.financetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Service for sending emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send simple email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@financetracker.com");

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
        }
    }

    /**
     * Send email to multiple recipients
     */
    public void sendEmailToMultiple(String[] to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@financetracker.com");

            mailSender.send(message);
            log.info("Email sent to: {}", Arrays.toString(to));
        } catch (Exception e) {
            log.error("Error sending email to multiple recipients", e);
        }
    }

    /**
     * Send registration confirmation email
     */
    public void sendRegistrationEmail(String to, String firstName, String confirmationLink) {
        String subject = "Welcome to Finance Tracker!";
        String body = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with Finance Tracker. Please confirm your email address:\n" +
                "%s\n\n" +
                "Best regards,\n" +
                "Finance Tracker Team",
                firstName,
                confirmationLink
        );

        sendEmail(to, subject, body);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String firstName, String resetLink) {
        String subject = "Reset Your Finance Tracker Password";
        String body = String.format(
                "Hello %s,\n\n" +
                "We received a request to reset your password. Click the link below:\n" +
                "%s\n\n" +
                "This link expires in 24 hours.\n\n" +
                "Best regards,\n" +
                "Finance Tracker Team",
                firstName,
                resetLink
        );

        sendEmail(to, subject, body);
    }

    /**
     * Send budget alert email
     */
    public void sendBudgetAlertEmail(String to, String categoryName, double percentageUsed, double spent, double budget) {
        String subject = "Budget Alert: " + categoryName;
        String body = String.format(
                "Your %s category budget alert has been triggered.\n\n" +
                "Percentage Used: %.2f%%\n" +
                "Amount Spent: $%.2f\n" +
                "Budget Limit: $%.2f\n\n" +
                "Please review your spending in the Finance Tracker app.",
                categoryName,
                percentageUsed,
                spent,
                budget
        );

        sendEmail(to, subject, body);
    }

    /**
     * Send budget exceeded email
     */
    public void sendBudgetExceededEmail(String to, String categoryName, double overage, double budget) {
        String subject = "Budget Exceeded: " + categoryName;
        String body = String.format(
                "WARNING: You have exceeded your %s budget!\n\n" +
                "Amount Over Budget: $%.2f\n" +
                "Budget Limit: $%.2f\n\n" +
                "Please take action to control your spending.",
                categoryName,
                overage,
                budget
        );

        sendEmail(to, subject, body);
    }

    /**
     * Send monthly summary email
     */
    public void sendMonthlySummaryEmail(
            String to,
            String firstName,
            double totalIncome,
            double totalExpense,
            double netSavings,
            int transactionCount) {

        String subject = "Your Monthly Finance Summary";
        String body = String.format(
                "Hello %s,\n\n" +
                "Here's your financial summary:\n\n" +
                "Total Income: $%.2f\n" +
                "Total Expenses: $%.2f\n" +
                "Net Savings: $%.2f\n" +
                "Transactions: %d\n\n" +
                "Log in to the Finance Tracker app for detailed insights.",
                firstName,
                totalIncome,
                totalExpense,
                netSavings,
                transactionCount
        );

        sendEmail(to, subject, body);
    }

    /**
     * Send insight email
     */
    public void sendInsightEmail(String to, String firstName, String insightTitle, String insightMessage) {
        String subject = "Finance Insight: " + insightTitle;
        String body = String.format(
                "Hello %s,\n\n" +
                "We've discovered a new financial insight for you:\n\n" +
                "%s\n\n" +
                "Log in to the Finance Tracker app to view more details and recommendations.",
                firstName,
                insightMessage
        );

        sendEmail(to, subject, body);
    }
}
