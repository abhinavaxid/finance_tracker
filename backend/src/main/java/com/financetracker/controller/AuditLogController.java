package com.financetracker.controller;

import com.financetracker.model.AuditLog;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.AuditLogService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for audit log management
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    /**
     * Get all audit logs for current user
     * GET /api/audit-logs
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getUserAuditLogs() {
        log.info("Fetching audit logs");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<AuditLog> logs = auditLogService.getUserAuditLogs(user);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get failed login attempts
     * GET /api/audit-logs/security/failed-logins
     */
    @GetMapping("/security/failed-logins")
    public ResponseEntity<List<AuditLog>> getFailedLoginAttempts() {
        log.info("Fetching failed login attempts");
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<AuditLog> logs = auditLogService.getFailedLoginAttempts(user);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs for specific entity
     * GET /api/audit-logs/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        log.info("Fetching audit logs for entity: {} with ID: {}", entityType, entityId);
        List<AuditLog> logs = auditLogService.getEntityAuditLogs(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Export audit logs
     * GET /api/audit-logs/export
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Exporting audit logs for date range: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String csvData = auditLogService.exportAuditLogs(user, startDate, endDate);
        return ResponseEntity.ok(csvData);
    }
}
