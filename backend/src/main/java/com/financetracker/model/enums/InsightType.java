package com.financetracker.model.enums;

/**
 * Enum representing insight types
 */
public enum InsightType {
    OVERSPENDING,      // Spending more than usual
    TREND_UP,          // Spending increasing
    TREND_DOWN,        // Spending decreasing
    LOW_SAVINGS,       // Savings below threshold
    HIGH_SPENDING,     // High spending detected
    UNUSUAL_ACTIVITY   // Unusual transaction patterns
}
