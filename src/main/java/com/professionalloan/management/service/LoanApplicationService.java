package com.professionalloan.management.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.professionalloan.management.exception.DuplicateLoanApplicationException;
import com.professionalloan.management.exception.LoanApplicationNotFoundException;
import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.model.ApplicationStatus;
import com.professionalloan.management.model.LoanApplication;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.LoanApplicationRepository;
import com.professionalloan.management.repository.UserRepository;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private EmailService emailService;

    private static final int MINIMUM_CREDIT_SCORE = 600;

    @Transactional
    public LoanApplication submitApplicationWithFiles(
            String name,
            String profession,
            String purpose,
            BigDecimal loanAmount,
            String panCard,
            Integer tenureInMonths,
            Long userId,
            MultipartFile pfAccountPdf,
            MultipartFile salarySlip
    ) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<LoanApplication> existingApps = loanRepo.findByUser_Id(userId);

        for (LoanApplication app : existingApps) {
            if (app.getStatus() == ApplicationStatus.APPROVED || app.getStatus() == ApplicationStatus.DISBURSED) {
                throw new DuplicateLoanApplicationException("You already have an active loan. Cannot apply again until it is closed.");
            }
            if (app.getPanCard().equalsIgnoreCase(panCard)) {
                throw new DuplicateLoanApplicationException("PAN card already exists in another application.");
            }
        }

        int creditScore = fetchCreditScoreByPan(panCard);

        LoanApplication application = new LoanApplication();
        application.setApplicationId(UUID.randomUUID().toString());
        application.setName(name);
        application.setProfession(profession);
        application.setPurpose(purpose);
        application.setLoanAmount(loanAmount);
        application.setCreditScore(creditScore);
        application.setPanCard(panCard);
        application.setTenureInMonths(tenureInMonths);
        application.setUser(user);
        application.setPfAccountPdf(pfAccountPdf != null && !pfAccountPdf.isEmpty() ? pfAccountPdf.getBytes() : null);
        application.setSalarySlip(salarySlip != null && !salarySlip.isEmpty() ? salarySlip.getBytes() : null);

        if (creditScore < MINIMUM_CREDIT_SCORE) {
            application.setStatus(ApplicationStatus.REJECTED);
            notificationService.notifyLoanStatus(userId, application.getApplicationId(), ApplicationStatus.REJECTED, null);

            emailService.sendLoanStatusEmail(
                    user.getEmail(),
                    user.getName(),
                    "REJECTED",
                    application.getApplicationId(),
                    creditScore,
                    "Reason: Credit score is below the required threshold (" + MINIMUM_CREDIT_SCORE + ")."
            );
        } else {
            application.setStatus(ApplicationStatus.PENDING);
            notificationService.createNotification(userId,
                    "Your loan application has been submitted successfully!", "APPLICATION_SUBMITTED");

            emailService.sendLoanStatusEmail(
                    user.getEmail(),
                    user.getName(),
                    "PENDING",
                    application.getApplicationId(),
                    creditScore,
                    "Your application has been received and is under review."
            );
        }

        LoanApplication savedApplication = loanRepo.save(application);

        if (pfAccountPdf != null && !pfAccountPdf.isEmpty()) {
            documentService.saveDocument(pfAccountPdf, userId, "PF_ACCOUNT_PDF");
        }
        if (salarySlip != null && !salarySlip.isEmpty()) {
            documentService.saveDocument(salarySlip, userId, "SALARY_SLIP");
        }

        return savedApplication;
    }

    // ✅ Uses original PAN card value for deterministic score 
    private int fetchCreditScoreByPan(String panCard) {
        int hash = Math.abs(panCard.hashCode());
        return 550 + (hash % 301);  // Range: 550–850
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> getApplicationsByUserId(Long userId) {
        return userId == null ? List.of() : loanRepo.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> getAllApplications() {
        return loanRepo.findAll();
    }

    @Transactional
    public LoanApplication updateLoanStatusWithComment(String applicationId, ApplicationStatus status, String comment) {
        LoanApplication application = loanRepo.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Application not found with ID: " + applicationId));

        application.setStatus(status);
        application.setStatusComment(comment);
        notificationService.notifyLoanStatus(application.getUser().getId(), applicationId, status, comment);
        return loanRepo.save(application);
    }

    @Transactional(readOnly = true)
    public LoanApplication getApplicationById(String applicationId) {
        return loanRepo.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Application not found with ID: " + applicationId));
    }
}
