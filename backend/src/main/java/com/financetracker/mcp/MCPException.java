package com.financetracker.mcp;

/**
 * Custom exception for MCP-related errors
 */
public class MCPException extends Exception {

    private String errorCode;
    private Object errorDetails;

    public MCPException(String message) {
        super(message);
        this.errorCode = "MCP_ERROR";
    }

    public MCPException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MCPException(String message, String errorCode, Object errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    public MCPException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MCP_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getErrorDetails() {
        return errorDetails;
    }
}
