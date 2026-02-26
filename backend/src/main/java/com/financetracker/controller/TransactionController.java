package com.financetracker.controller;

import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.dto.mcp.MCPTransactionRequest;
import com.financetracker.dto.mcp.MCPResponse;
import com.financetracker.mcp.MCPException;
import com.financetracker.mcp.MCPTransactionService;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.TransactionService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for transaction management
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final MCPTransactionService mcpTransactionService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Create a new transaction
     * POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Creating transaction for amount: {}", request.getAmount());
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        TransactionResponse response = transactionService.createTransaction(
                user, category, request.getAmount(), request.getType(),
                request.getDescription(), request.getTransactionDate(),
                request.getPaymentMethod(), null, null
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get transaction by ID
     * GET /api/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.info("Fetching transaction: {}", id);
        Transaction response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for current user
     * GET /api/transactions
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching transactions - page: {}, size: {}", page, size);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.getUserTransactions(user, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get transactions by date range
     * GET /api/transactions/search/date-range
     */
    @GetMapping("/search/date-range")
    public ResponseEntity<Page<TransactionResponse>> getByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching transactions for date range: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.getTransactionsByDateRange(
                user, startDate, endDate, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Search transactions by description
     * GET /api/transactions/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(
            @RequestParam String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching transactions with description: {}", description);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionResponse> responses = transactionService.searchTransactions(
                user, description, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get total income for period
     * GET /api/transactions/analytics/income
     */
    @GetMapping("/analytics/income")
    public ResponseEntity<BigDecimal> getTotalIncome(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching total income for period: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal totalIncome = transactionService.getTotalIncome(user, startDate, endDate);
        return ResponseEntity.ok(totalIncome);
    }

    /**
     * Get total expense for period
     * GET /api/transactions/analytics/expense
     */
    @GetMapping("/analytics/expense")
    public ResponseEntity<BigDecimal> getTotalExpense(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching total expense for period: {} to {}", startDate, endDate);
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal totalExpense = transactionService.getTotalExpense(user, startDate, endDate);
        return ResponseEntity.ok(totalExpense);
    }

    /**
     * Update transaction
     * PUT /api/transactions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        log.info("Updating transaction: {}", id);
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        TransactionResponse response = transactionService.updateTransaction(
                id, category, request.getAmount(), request.getType(),
                request.getDescription(), request.getTransactionDate(),
                request.getPaymentMethod(), null, null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete transaction
     * DELETE /api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("Deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Process transaction request from MCP (Model Context Protocol)
     * Handles natural language input parsed by GLM-5/Claude
     * POST /api/transactions/mcp/process
     */
    @PostMapping("/mcp/process")
    public ResponseEntity<MCPResponse> processMCPTransaction(
            @Valid @RequestBody MCPTransactionRequest mcpRequest) {
        log.info("Processing MCP transaction request - action: {}, originalInput: {}", 
                mcpRequest.getAction(), mcpRequest.getOriginalInput());

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String action = mcpRequest.getAction() != null ? mcpRequest.getAction().toUpperCase() : "CREATE";

            switch (action) {
                case "CREATE":
                    return handleMCPCreate(user, mcpRequest);
                case "READ":
                    return handleMCPRead(user, mcpRequest);
                case "UPDATE":
                    return handleMCPUpdate(user, mcpRequest);
                case "DELETE":
                    return handleMCPDelete(user, mcpRequest);
                default:
                    return ResponseEntity.badRequest()
                            .body(MCPResponse.error("INVALID_ACTION", "Unknown action: " + action));
            }
        } catch (MCPException e) {
            log.error("MCP processing error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(MCPResponse.error(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing MCP request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MCPResponse.error("INTERNAL_ERROR", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Handle MCP CREATE action
     */
    private ResponseEntity<MCPResponse> handleMCPCreate(User user, MCPTransactionRequest mcpRequest) throws MCPException {
        if (mcpRequest.getAmount() == null || mcpRequest.getCategoryHint() == null) {
            throw new MCPException("Amount and category are required for transaction creation");
        }

        TransactionResponse transaction = mcpTransactionService.createTransactionFromMCP(
                user,
                mcpRequest.getAmount(),
                mcpRequest.getCategoryHint(),
                mcpRequest.getType(),
                mcpRequest.getDescription(),
                mcpRequest.getTransactionDate(),
                mcpRequest.getPaymentMethod()
        );

        String confirmationMessage = formatConfirmationMessage(transaction, mcpRequest);
        MCPResponse response = MCPResponse.success(confirmationMessage, transaction);
        response.setAction("CREATE");
        response.setConfidence(mcpRequest.getConfidence());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Handle MCP READ action
     */
    private ResponseEntity<MCPResponse> handleMCPRead(User user, MCPTransactionRequest mcpRequest) throws MCPException {
        if (mcpRequest.getTransactionId() == null) {
            throw new MCPException("Transaction ID is required for read operation");
        }

        TransactionResponse transaction = mcpTransactionService.getTransaction(
                mcpRequest.getTransactionId(), user);

        MCPResponse response = MCPResponse.success("Transaction retrieved successfully", transaction);
        response.setAction("READ");

        return ResponseEntity.ok(response);
    }

    /**
     * Handle MCP UPDATE action (placeholder - can be extended)
     */
    private ResponseEntity<MCPResponse> handleMCPUpdate(User user, MCPTransactionRequest mcpRequest) throws MCPException {
        // Future implementation: Update transaction details
        throw new MCPException("UPDATE action is not yet implemented", "NOT_IMPLEMENTED");
    }

    /**
     * Handle MCP DELETE action
     */
    private ResponseEntity<MCPResponse> handleMCPDelete(User user, MCPTransactionRequest mcpRequest) throws MCPException {
        if (mcpRequest.getTransactionId() == null) {
            throw new MCPException("Transaction ID is required for delete operation");
        }

        mcpTransactionService.deleteTransaction(mcpRequest.getTransactionId(), user);

        MCPResponse response = MCPResponse.success("Transaction deleted successfully", null);
        response.setAction("DELETE");

        return ResponseEntity.ok(response);
    }

    /**
     * Format user-friendly confirmation message
     */
    private String formatConfirmationMessage(TransactionResponse transaction, MCPTransactionRequest mcpRequest) {
        String typeSymbol = "INCOME".equalsIgnoreCase(transaction.getType()) ? "+" : "−";
        String formattedAmount = String.format("%.2f", transaction.getAmount());
        
        return String.format("✓ %s ₹%s added to %s on %s",
                typeSymbol,
                formattedAmount,
                transaction.getCategoryName(),
                transaction.getTransactionDate());
    }
}
