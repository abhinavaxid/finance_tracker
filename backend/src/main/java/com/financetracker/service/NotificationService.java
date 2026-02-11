package com.financetracker.service;

import com.financetracker.model.Notification;
import com.financetracker.model.User;
import com.financetracker.model.enums.NotificationType;
import com.financetracker.model.enums.Priority;
import com.financetracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Create a new notification
     */
    public Notification createNotification(
            User user,
            String type,
            String title,
            String message,
            String priority,
            boolean sendEmail) {

        log.info("Creating notification for user: {}", user.getId());

        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.valueOf(type.toUpperCase()))
                .title(title)
                .message(message)
                .priority(Priority.valueOf(priority.toUpperCase()))
                .isRead(false)
                .isEmailSent(false)
                .build();

        notification = notificationRepository.save(notification);

        // Send email if requested
        if (sendEmail) {
            sendNotificationEmail(notification);
        }

        return notification;
    }

    /**
     * Get unread notifications for user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Get notifications by type (paginated)
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(User user, String type) {
        NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
        // Get all notifications of type for user
        List<Notification> all = getUnreadNotifications(user);
        return all.stream()
                .filter(n -> n.getType() == notificationType)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get all notifications for user
     */
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(User user) {
        return getUnreadNotifications(user);
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }

    /**
     * Mark multiple notifications as read
     */
    public void markMultipleAsRead(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            markAsRead(notificationId);
        }
    }

    /**
     * Mark all notifications as read for user
     */
    public void markAllAsRead(User user) {
        List<Notification> unread = getUnreadNotifications(user);
        for (Notification notification : unread) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unread);
        log.info("All notifications marked as read for user: {}", user.getId());
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        log.info("Deleting notification: {}", notificationId);
        notificationRepository.delete(notification);
    }

    /**
     * Delete all notifications for user
     */
    public void deleteAllNotifications(User user) {
        List<Notification> notifications = getAllNotifications(user);
        notificationRepository.deleteAll(notifications);
        log.info("All notifications deleted for user: {}", user.getId());
    }

    /**
     * Get count of unread notifications
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Send notification email
     */
    private void sendNotificationEmail(Notification notification) {
        try {
            String recipientEmail = notification.getUser().getEmail();
            String subject = notification.getTitle();
            String body = String.format(
                    "Priority: %s\n\n%s\n\nPlease log in to view more details.",
                    notification.getPriority().name(),
                    notification.getMessage()
            );

            emailService.sendEmail(recipientEmail, subject, body);
            
            notification.setIsEmailSent(true);
            notificationRepository.save(notification);
            
            log.info("Notification email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Error sending notification email", e);
        }
    }

    /**
     * Create and send budget alert notification
     */
    public void sendBudgetAlertNotification(User user, String categoryName, java.math.BigDecimal spent, java.math.BigDecimal budget) {
        String title = "Budget Alert";
        String message = String.format(
                "Your %s category has reached %.2f%% of your budget (%.2f / %.2f)",
                categoryName,
                (spent.doubleValue() / budget.doubleValue()) * 100,
                spent,
                budget
        );

        createNotification(
                user,
                NotificationType.BUDGET_ALERT.name(),
                title,
                message,
                Priority.HIGH.name(),
                true
        );
    }

    /**
     * Create and send overspending notification
     */
    public void sendOverspendingNotification(User user, String categoryName, java.math.BigDecimal overage) {
        String title = "Budget Exceeded";
        String message = String.format(
                "You have exceeded your %s budget by %.2f",
                categoryName,
                overage
        );

        createNotification(
                user,
                NotificationType.BUDGET_ALERT.name(),
                title,
                message,
                Priority.URGENT.name(),
                true
        );
    }

    /**
     * Verify user owns the notification
     */
    @Transactional(readOnly = true)
    public boolean userOwnsNotification(User user, Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        return notification.getUser().getId().equals(user.getId());
    }
}
