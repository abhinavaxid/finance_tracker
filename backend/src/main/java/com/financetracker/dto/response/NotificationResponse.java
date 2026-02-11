package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    
    private String type;
    
    private String title;
    
    private String message;
    
    private String priority;
    
    @JsonProperty("is_read")
    private Boolean isRead;
    
    @JsonProperty("is_email_sent")
    private Boolean isEmailSent;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("read_at")
    private LocalDateTime readAt;
}
