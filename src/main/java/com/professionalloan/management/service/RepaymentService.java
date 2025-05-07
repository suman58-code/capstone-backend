package com.professionalloan.management.service;

import com.professionalloan.management.model.ApplicationStatus;
import com.professionalloan.management.model.LoanApplication;
import com.professionalloan.management.model.Repayment;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.LoanApplicationRepository;
import com.professionalloan.management.repository.RepaymentRepository;
import com.professionalloan.management.dto.RepaymentDTO;
import com.professionalloan.management.exception.LoanApplicationNotFoundException;
import com.professionalloan.management.exception.RepaymentNotFoundException;
import com.professionalloan.management.exception.EMIAlreadyPaidException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepaymentService {

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    public Repayment getRepaymentById(Long repaymentId) {
        return repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RepaymentNotFoundException("Repayment not found with ID: " + repaymentId));
    }

    public BigDecimal calculateEMI(BigDecimal principal, int tenureInMonths, double interestRate) {
        double monthlyRate = (interestRate / 12.0) / 100.0;
        double emi = principal.doubleValue() * monthlyRate * Math.pow(1 + monthlyRate, tenureInMonths) /
                (Math.pow(1 + monthlyRate, tenureInMonths) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Repayment> generateEMISchedule(String applicationId, int tenureInMonths) {
        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found with ID: " + applicationId));

        BigDecimal emiAmount = calculateEMI(loan.getLoanAmount(), tenureInMonths, 12.0);
        List<Repayment> emiSchedule = new ArrayList<>();
        LocalDate startDate = LocalDate.now();

        for (int i = 1; i <= tenureInMonths; i++) {
            Repayment repayment = new Repayment();
            repayment.setLoanApplication(loan);
            repayment.setEmiAmount(emiAmount);
            repayment.setEmiNumber(i);
            repayment.setDueDate(startDate.plusMonths(i));
            repayment.setStatus("PENDING");
            repayment.setPaidDate(null);
            emiSchedule.add(repayment);
        }

        return repaymentRepository.saveAll(emiSchedule);
    }

    @Transactional
    public Repayment makePayment(Long repaymentId) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RepaymentNotFoundException("Repayment not found with ID: " + repaymentId));

        if ("PAID".equalsIgnoreCase(repayment.getStatus())) {
            throw new EMIAlreadyPaidException("This EMI is already paid");
        }

        repayment.setStatus("PAID");
        repayment.setPaidDate(LocalDate.now());
        repaymentRepository.save(repayment);

        LoanApplication loan = repayment.getLoanApplication();
        List<Repayment> pendingEmis = repaymentRepository
                .findByLoanApplication_ApplicationIdAndStatus(loan.getApplicationId(), "PENDING");

        if (pendingEmis.isEmpty()) {
            loan.setStatus(ApplicationStatus.CLOSED);
            loanApplicationRepository.save(loan);
        }

        sendPaymentEmail(repayment);

        notificationService.createNotification(
                loan.getUser().getId(),
                "Your EMI payment of ₹" + repayment.getEmiAmount() + " for Loan " + loan.getApplicationId() + " is successful.",
                "EMI_PAYMENT"
        );

        return repayment;
    }

    @Transactional
    public Repayment simulatePayment(Long repaymentId, String method) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RepaymentNotFoundException("Repayment not found with ID: " + repaymentId));

        if ("PAID".equalsIgnoreCase(repayment.getStatus())) {
            throw new EMIAlreadyPaidException("This EMI is already paid");
        }

        if (Math.random() < 0.1) {
            repayment.setFailureReason("Simulated failure: Payment gateway error");
            repayment.setStatus("FAILED");
            repaymentRepository.save(repayment);
            throw new RuntimeException("Simulated payment failure");
        }

        repayment.setStatus("PAID");
        repayment.setPaidDate(LocalDate.now());
        repayment.setPaymentMethod(method);
        repayment.setTransactionId("TXN" + System.currentTimeMillis());
        repayment.setFailureReason(null);
        repaymentRepository.save(repayment);

        LoanApplication loan = repayment.getLoanApplication();
        List<Repayment> pendingEmis = repaymentRepository
                .findByLoanApplication_ApplicationIdAndStatus(loan.getApplicationId(), "PENDING");

        if (pendingEmis.isEmpty()) {
            loan.setStatus(ApplicationStatus.CLOSED);
            loanApplicationRepository.save(loan);
        }

        sendPaymentEmail(repayment);

        notificationService.createNotification(
                loan.getUser().getId(),
                "Your EMI #" + repayment.getEmiNumber() + " payment of ₹" + repayment.getEmiAmount() +
                        " for Loan " + loan.getApplicationId() + " is successful.",
                "EMI_PAYMENT"
        );

        return repayment;
    }

    private void sendPaymentEmail(Repayment repayment) {
        LoanApplication loan = repayment.getLoanApplication();
        User user = loan.getUser();

        String subject = "✅ EMI Payment Successful - Confirmation for Loan ID " + loan.getApplicationId();

        String message = String.format(
                "Hello %s,\n\n" +
                        "We are pleased to inform you that your EMI payment has been successfully received.\n\n" +
                        "📌 Payment Details:\n" +
                        "   • Amount Paid: ₹%s\n" +
                        "   • Loan ID: %s\n" +
                        "   • Payment Date: %s\n\n" +
                        "Thank you for your prompt payment. If you have any questions, feel free to contact our support team.\n\n" +
                        "Best regards,\n" +
                        "Professional Loan Management System (PLMS) Team",
                user.getName(),
                repayment.getEmiAmount(),
                loan.getApplicationId(),
                repayment.getPaidDate()
        );

        emailService.sendSimpleMessage(user.getEmail(), subject, message);
    }

    public List<Repayment> getLoanEMIs(String applicationId) {
        return repaymentRepository.findByLoanApplication_ApplicationId(applicationId);
    }

    public List<Repayment> getPendingEMIs(String applicationId) {
        return repaymentRepository.findByLoanApplication_ApplicationIdAndStatus(applicationId, "PENDING");
    }

    public RepaymentDTO toDTO(Repayment repayment) {
        RepaymentDTO dto = new RepaymentDTO();
        dto.setId(repayment.getId());
        dto.setEmiAmount(repayment.getEmiAmount());
        dto.setDueDate(repayment.getDueDate());
        dto.setPaidDate(repayment.getPaidDate());
        dto.setStatus(repayment.getStatus());
        dto.setEmiNumber(repayment.getEmiNumber());
        dto.setApplicationId(repayment.getLoanApplication() != null ? repayment.getLoanApplication().getApplicationId() : null);
        return dto;
    }

    public List<Repayment> getRepaymentsByUserId(Long userId) {
        return repaymentRepository.findByLoanApplication_User_Id(userId);
    }
}
