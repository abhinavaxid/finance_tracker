package com.financetracker.service;

import com.financetracker.model.AuditLog;
import com.financetracker.model.User;
import com.financetracker.model.enums.AuditAction;
import com.financetracker.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an action
     */
    public void logAction(
            User user,
            String action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue) {

        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(AuditAction.valueOf(action.toUpperCase()))
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Error logging audit trail", e);
        }
    }

    /**
     * Get audit logs for a user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(User user) {
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getUser() != null && auditLog.getUser().getId().equals(user.getId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get audit logs by action
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getAction() == action)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get audit logs by entity type
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getEntityType().equals(entityType))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get audit logs for entity
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getEntityAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getEntityType().equals(entityType) && auditLog.getEntityId().equals(entityId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get failed login attempts
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getFailedLoginAttempts(User user) {
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getUser() != null && auditLog.getUser().getId().equals(user.getId()) &&
                        auditLog.getAction() == AuditAction.FAILED_LOGIN)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String clientIp = request.getHeader("X-Forwarded-For");
                
                if (clientIp == null || clientIp.isEmpty()) {
                    clientIp = request.getRemoteAddr();
                } else {
                    clientIp = clientIp.split(",")[0];
                }
                
                return clientIp;
            }
        } catch (Exception e) {
            log.debug("Unable to get client IP address", e);
        }
        
        return "UNKNOWN";
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Unable to get user agent", e);
        }
        
        return "UNKNOWN";
    }

}
