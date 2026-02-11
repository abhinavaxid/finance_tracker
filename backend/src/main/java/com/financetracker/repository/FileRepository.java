package com.financetracker.repository;

import com.financetracker.model.File;
import com.financetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for File entity operations
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * Find files for a user (paginated)
     */
    Page<File> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);

    /**
     * Find files by transaction
     */
    List<File> findByTransactionIdOrderByUploadedAtDesc(Long transactionId);

    /**
     * Find files for user by file type
     */
    Page<File> findByUserAndFileTypeOrderByUploadedAtDesc(User user, String fileType, Pageable pageable);

    /**
     * Find files by file name (search)
     */
    @Query("SELECT f FROM File f WHERE f.user = :user AND LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY f.uploadedAt DESC")
    Page<File> searchByFileName(
        @Param("user") User user,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    /**
     * Find large files
     */
    @Query("SELECT f FROM File f WHERE f.user = :user AND f.fileSize > :minSize ORDER BY f.fileSize DESC")
    List<File> findLargeFiles(
        @Param("user") User user,
        @Param("minSize") Long minSize
    );

    /**
     * Count files for user
     */
    long countByUser(User user);

    /**
     * Get total file size for user
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.user = :user")
    Long getTotalFileSizeForUser(@Param("user") User user);
}
