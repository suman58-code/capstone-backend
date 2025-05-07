package com.professionalloan.management.repository;

import com.professionalloan.management.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser_Id(Long userId);
    List<Document> findByUser_IdAndFileType(Long userId, String fileType);
} 