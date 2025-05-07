package com.professionalloan.management.repository;

import com.professionalloan.management.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {
    List<LoanApplication> findByUser_Id(Long userId);
}