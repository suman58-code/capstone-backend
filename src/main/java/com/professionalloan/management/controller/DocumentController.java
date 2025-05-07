package com.professionalloan.management.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.professionalloan.management.model.Document;
import com.professionalloan.management.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("documentType") String documentType
    ) {
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File must be under 5MB");
        }
        
        Document savedDocument = documentService.saveDocument(file, userId, documentType);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document uploaded successfully");
        response.put("documentId", savedDocument.getDocumentId());
        response.put("fileName", savedDocument.getFileName());
        response.put("fileType", savedDocument.getFileType());
        response.put("uploadDate", savedDocument.getUploadDate());
        response.put("fileSize", savedDocument.getFileSize());
        response.put("mimeType", savedDocument.getMimeType());
        response.put("isVerified", savedDocument.isVerified());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable Long userId) {
        List<Document> documents = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> downloadDocument(@PathVariable Long documentId) {
        Resource file = documentService.loadDocument(documentId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
            .body(file);
    }
    
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok().build();
    }
    
    
}