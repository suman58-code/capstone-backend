package com.professionalloan.management.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;  // e.g., PF_ACCOUNT, SALARY_SLIP

    @Column(nullable = false)
    private String filePath;  // Path to file on the server filesystem

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column
    private Long fileSize;

    @Column
    private String mimeType;

    @Column(nullable = false)
    private boolean isVerified = false;

    // ✅ ADD THIS DEFAULT CONSTRUCTOR
    public Document() {
        // Default no-argument constructor required by JPA
    }

    // Optional: parameterized constructor for convenience
    public Document(User user, String fileName, String fileType, String filePath,
                    LocalDateTime uploadDate, Long fileSize, String mimeType) {
        this.user = user;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.isVerified = false;
    }

    // Getters and Setters

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }
}
