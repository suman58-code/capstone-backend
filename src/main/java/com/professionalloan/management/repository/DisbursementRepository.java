package com.professionalloan.management.repository;

import com.professionalloan.management.model.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {
}
