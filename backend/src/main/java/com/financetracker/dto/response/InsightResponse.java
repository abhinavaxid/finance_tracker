package com.financetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for financial insights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    
    private Long id;
    
    @JsonProperty("insight_type")
    private String insightType;
    
    private String description;
    
    private String severity;
    
    @JsonProperty("is_dismissed")
    private Boolean isDismissed;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("dismissed_at")
    private LocalDateTime dismissedAt;
}
