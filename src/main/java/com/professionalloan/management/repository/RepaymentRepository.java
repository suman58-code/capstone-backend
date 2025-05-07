package com.professionalloan.management.repository;

import com.professionalloan.management.model.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanApplication_ApplicationId(String applicationId);
    List<Repayment> findByLoanApplication_ApplicationIdAndStatus(String applicationId, String status);
    List<Repayment> findByStatus(String status);
    List<Repayment> findByLoanApplication_User_Id(Long userId);
}