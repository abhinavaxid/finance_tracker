package com.financetracker.model.enums;

/**
 * Enum representing date format preferences
 */
public enum DateFormat {
    MM_DD_YYYY("MM/DD/YYYY"),
    DD_MM_YYYY("DD/MM/YYYY"),
    YYYY_MM_DD("YYYY-MM-DD");

    private final String format;

    DateFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
