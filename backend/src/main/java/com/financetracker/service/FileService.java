package com.financetracker.service;

import com.financetracker.model.File;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for file management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileService {

    private final FileRepository fileRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:5242880}")
    private long maxFileSize;

    /**
     * Upload file for transaction
     */
    public File uploadFile(MultipartFile multipartFile, Transaction transaction, User user) throws IOException {
        log.info("Uploading file for transaction: {}", transaction);

        // Validate file size
        if (multipartFile.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String fileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save file to disk
        Files.write(filePath, multipartFile.getBytes());

        // Create file record
        File file = File.builder()
                .transaction(transaction)
                .fileName(multipartFile.getOriginalFilename())
                .filePath(filePath.toString())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .user(user)
                .build();

        file = fileRepository.save(file);
        log.info("File uploaded successfully: {}", file);

        return file;
    }

    /**
     * Get file by ID
     */
    @Transactional(readOnly = true)
    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    /**
     * Get files for transaction
     */
    @Transactional(readOnly = true)
    public List<File> getTransactionFiles(Transaction transaction) {
        return fileRepository.findAll().stream()
                .filter(f -> f.getTransaction() != null && f.getTransaction().equals(transaction))
                .sorted((a, b) -> b.getUploadedAt().compareTo(a.getUploadedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Download file
     */
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long fileId) throws IOException {
        File file = getFileById(fileId);
        Path filePath = Paths.get(file.getFilePath());

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on disk");
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Delete file
     */
    public void deleteFile(Long fileId) throws IOException {
        File file = getFileById(fileId);
        Path filePath = Paths.get(file.getFilePath());

        // Delete from disk
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete from database
        fileRepository.delete(file);
        log.info("File deleted: {}", fileId);
    }

    /**
     * Get total size of all files for transaction
     */
    @Transactional(readOnly = true)
    public BigDecimal getTransactionFilesSize(Transaction transaction) {
        List<File> files = getTransactionFiles(transaction);
        return files.stream()
                .map(f -> BigDecimal.valueOf(f.getFileSize()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validate file
     */
    public boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > maxFileSize) {
            return false;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return true;
    }
}
