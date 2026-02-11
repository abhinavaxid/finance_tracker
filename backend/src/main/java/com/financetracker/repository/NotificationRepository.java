package com.financetracker.repository;

import com.financetracker.model.Notification;
import com.financetracker.model.User;
import com.financetracker.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification entity operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user (paginated)
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find unread notifications for user
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Count unread notifications for user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Find notifications by type
     */
    Page<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type, Pageable pageable);

    /**
     * Find notifications not sent via email
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isEmailSent = false ORDER BY n.createdAt DESC")
    List<Notification> findUnsentEmailNotifications(@Param("user") User user);

    /**
     * Find all pending email notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.isEmailSent = false ORDER BY n.createdAt")
    List<Notification> findAllPendingEmailNotifications();

    /**
     * Find recent notifications for user
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC LIMIT 5")
    List<Notification> findRecentNotifications(@Param("user") User user);
}
