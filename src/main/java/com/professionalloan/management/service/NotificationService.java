package com.professionalloan.management.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.model.ApplicationStatus;
import com.professionalloan.management.model.Notification;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.NotificationRepository;
import com.professionalloan.management.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    // --- In-App Notification Methods ---

    // Create a new notification
    public Notification createNotification(Long userId, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    // Get user's notifications
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    // Mark notification as read
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // Create loan status notification (in-app)
    // Create loan status notification (in-app) with optional comment
public void notifyLoanStatus(Long userId, String applicationId, ApplicationStatus status, String comment) {
    String message;
    if (comment != null && !comment.trim().isEmpty()) {
        message = String.format("Your loan application %s has been %s. %s", applicationId, status.name().toLowerCase(), comment);
    } else {
        message = String.format("Your loan application %s has been %s", applicationId, status.name().toLowerCase());
    }
    createNotification(userId, message, "STATUS_UPDATE");
}
    // Create EMI due notification (in-app)
    public void notifyEMIDue(Long userId, String applicationId, int emiNumber) {
        String message = String.format("EMI #%d for loan %s is due soon", emiNumber, applicationId);
        createNotification(userId, message, "EMI_DUE");
    }

    // Create EMI overdue notification (in-app)
    public void notifyEMIOverdue(Long userId, String applicationId, int emiNumber) {
        String message = String.format("EMI #%d for loan %s is overdue", emiNumber, applicationId);
        createNotification(userId, message, "EMI_OVERDUE");
    }

    // Create disbursement notification (in-app)
    public void notifyDisbursement(Long userId, String applicationId) {
        String message = String.format("Loan %s has been disbursed! EMI schedule is available.", applicationId);
        createNotification(userId, message, "DISBURSEMENT");
    }

    // --- Email Notification Methods ---

    // Send email after disbursement
    public void sendDisbursementEmail(User user, String applicationId, BigDecimal amount) {
        String to = user.getEmail();
        String subject = "Loan Disbursed!";
        String text = "Dear " + user.getName() + ",\n\n" +
                "Congratulations! Your loan (ID: " + applicationId + ") of ₹" + amount + " has been disbursed.\n" +
                "You can now view your EMI schedule in your dashboard.\n\n" +
                "Best regards,\nProfessional Loan Management Team";
        sendEmail(to, subject, text);
    }

    // Send EMI reminder email
    public void sendEmiReminderEmail(User user, String applicationId, int emiNumber, BigDecimal emiAmount, String dueDate) {
        String to = user.getEmail();
        String subject = "EMI Repayment Reminder";
        String text = "Dear " + user.getName() + ",\n\n" +
                "This is a reminder that your EMI #" + emiNumber +
                " for loan " + applicationId + " of amount ₹" + emiAmount +
                " is due on " + dueDate + ".\nPlease ensure timely payment.\n\n" +
                "Best regards,\nProfessional Loan Management Team";
        sendEmail(to, subject, text);
    }

    // --- Helper method to send email ---
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(text);
        mailSender.send(mail);
    }
}