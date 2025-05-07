package com.professionalloan.management.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.professionalloan.management.model.Disbursement;
import com.professionalloan.management.model.LoanApplication;
import com.professionalloan.management.model.DisbursementStatus;
import com.professionalloan.management.model.ApplicationStatus;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.DisbursementRepository;
import com.professionalloan.management.repository.LoanApplicationRepository;

@Service
public class DisbursementService {

    @Autowired
    private DisbursementRepository disbursementRepo;

    @Autowired
    private LoanApplicationRepository loanRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RepaymentService repaymentService;

    //  Full atomic disbursement method
    @Transactional
    public Disbursement disburseLoan(String applicationId, BigDecimal amount) {
        LoanApplication application = loanRepo.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Loan application not found"));

        // Validate loan status before disbursement
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("Loan application must be APPROVED before disbursement.");
        }

        //  Strict amount validation (optional)
        if (amount.compareTo(application.getLoanAmount()) != 0) {
            throw new RuntimeException("Disbursement amount must match approved loan amount.");
        }

        // Create new disbursement record
        Disbursement disbursement = new Disbursement();
        disbursement.setLoanApplication(application);
        disbursement.setDisbursedAmount(amount);
        disbursement.setDisbursementDate(LocalDate.now());
        disbursement.setStatus(DisbursementStatus.PROCESSING);

        disbursementRepo.save(disbursement);

        //  Simulate Bank Transfer (sleep 1 sec)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Disbursement process interrupted.");
        }

        // Update disbursement + loan status
        disbursement.setStatus(DisbursementStatus.COMPLETED);
        disbursementRepo.save(disbursement);

        application.setStatus(ApplicationStatus.DISBURSED);
        loanRepo.save(application);

        // Generate EMI schedule immediately
        repaymentService.generateEMISchedule(applicationId, application.getTenureInMonths());

        
        // Notify user after disbursement
        User user = application.getUser();
        notificationService.notifyDisbursement(user.getId(), applicationId);
        notificationService.sendDisbursementEmail(user, applicationId, amount);

        return disbursement;
    }
}

