package com.professionalloan.management.service;

import com.professionalloan.management.model.Document;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.DocumentRepository;
import com.professionalloan.management.repository.UserRepository;
import com.professionalloan.management.exception.DocumentNotFoundException;
import com.professionalloan.management.exception.DocumentUploadException;
import com.professionalloan.management.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public Document saveDocument(MultipartFile file, Long userId, String documentType) {
        // Validate inputs
        if (file == null || file.isEmpty()) {
            logger.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (userId == null) {
            logger.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (documentType == null || documentType.trim().isEmpty()) {
            logger.error("Document type is null or empty");
            throw new IllegalArgumentException("Document type cannot be null or empty");
        }

        // Get user
        logger.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found with ID: {}", userId);
                return new UserNotFoundException("User not found with ID: " + userId);
            });

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, String.valueOf(userId));
        logger.info("Creating upload directory if not exists: {}", uploadPath);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", uploadPath, e);
            throw new DocumentUploadException("Could not create upload directory", e);
        }

        // Verify directory is writable
        if (!Files.isWritable(uploadPath)) {
            logger.error("Upload directory is not writable: {}", uploadPath);
            throw new DocumentUploadException("Upload directory is not writable");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            logger.error("Original filename is null or empty");
            throw new IllegalArgumentException("Invalid file name");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to filesystem
        Path filePath = uploadPath.resolve(newFilename);
        logger.info("Saving file to: {}", filePath);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to save file to filesystem: {}", filePath, e);
            throw new DocumentUploadException("Could not store file", e);
        }

        // Create document record
        logger.info("Creating document record for file: {}", originalFilename);
        Document document = new Document(
            user,
            originalFilename,
            documentType,
            filePath.toString(),
            LocalDateTime.now(),
            file.getSize(),
            file.getContentType()
        );

        // Save document to database
        Document savedDocument = documentRepository.save(document);
        logger.info("Document saved successfully with ID: {}", savedDocument.getDocumentId());
        return savedDocument;
    }

    public List<Document> getUserDocuments(Long userId) {
        logger.info("Fetching documents for user ID: {}", userId);
        return documentRepository.findByUser_Id(userId);
    }

    public Resource loadDocument(Long documentId) {
        logger.info("Loading document with ID: {}", documentId);
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> {
                logger.error("Document not found with ID: {}", documentId);
                return new DocumentNotFoundException("Document not found with ID: " + documentId);
            });

        Path filePath = Paths.get(document.getFilePath());
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            logger.error("Invalid file path: {}", filePath, e);
            throw new DocumentNotFoundException("Invalid file path", e);
        }

        if (resource.exists() && resource.isReadable()) {
            logger.info("Document loaded successfully: {}", filePath);
            return resource;
        } else {
            logger.error("Could not read file: {}", filePath);
            throw new DocumentNotFoundException("Could not read file: " + filePath);
        }
    }

    public void deleteDocument(Long documentId) {
        logger.info("Deleting document with ID: {}", documentId);
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> {
                logger.error("Document not found with ID: {}", documentId);
                return new DocumentNotFoundException("Document not found with ID: " + documentId);
            });

        // Delete file from filesystem
        Path filePath = Paths.get(document.getFilePath());
        try {
            Files.deleteIfExists(filePath);
            logger.info("File deleted from filesystem: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to delete file from filesystem: {}", filePath, e);
            throw new DocumentUploadException("Failed to delete file from filesystem", e);
        }

        // Delete database record
        documentRepository.delete(document);
        logger.info("Document deleted successfully: {}", documentId);
    }

    public List<Document> getUserDocumentsByType(Long userId, String documentType) {
        logger.info("Fetching documents for user ID: {} and type: {}", userId, documentType);
        return documentRepository.findByUser_IdAndFileType(userId, documentType);
    }

    public void verifyDocument(Long documentId) {
        logger.info("Verifying document with ID: {}", documentId);
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> {
                logger.error("Document not found with ID: {}", documentId);
                return new DocumentNotFoundException("Document not found with ID: " + documentId);
            });
        document.setVerified(true);
        documentRepository.save(document);
        logger.info("Document verified successfully: {}", documentId);
    }
}