package com.financetracker.controller;

import com.financetracker.model.File;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.FileService;
import com.financetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for file upload/download management
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Upload a file (receipt/invoice)
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<File> uploadFile(
            @RequestParam Long transactionId,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        log.info("Uploading file for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        File uploadedFile = fileService.uploadFile(file, transaction, user);
        return ResponseEntity.ok(uploadedFile);
    }

    /**
     * Download a file
     * GET /api/files/download/{fileId}
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) throws java.io.IOException {
        log.info("Downloading file: {}", fileId);
        
        byte[] fileBytes = fileService.downloadFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"file\"")
                .body(fileBytes);
    }

    /**
     * Get all files for a transaction
     * GET /api/files/transaction/{transactionId}
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<File>> getTransactionFiles(@PathVariable Long transactionId) {
        log.info("Fetching files for transaction: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        List<File> files = fileService.getTransactionFiles(transaction);
        return ResponseEntity.ok(files);
    }

    /**
     * Delete a file
     * DELETE /api/files/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) throws java.io.IOException {
        log.info("Deleting file: {}", fileId);
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validate file before upload
     * POST /api/files/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateFile(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        log.info("Validating file: {}", file.getOriginalFilename());
        boolean isValid = fileService.validateFile(file);
        return ResponseEntity.ok(isValid);
    }
}
