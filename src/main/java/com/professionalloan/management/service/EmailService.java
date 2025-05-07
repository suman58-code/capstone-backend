package com.professionalloan.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "avijit.dam9@gmail.com";

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Password Reset");
        message.setText("Your OTP is: " + otp + ". It is valid for 10 minutes.");
        message.setFrom(FROM_EMAIL);
        mailSender.send(message);
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(FROM_EMAIL);
        mailSender.send(message);
    }

    public void sendLoanStatusEmail(String to, String name, String status, String applicationId, int creditScore, String extraInfo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(FROM_EMAIL);
        message.setSubject("Loan Application Status Update");

        StringBuilder text = new StringBuilder();
        text.append("Dear ").append(name).append(",\n\n");
        text.append("Your loan application (ID: ").append(applicationId).append(") status: ").append(status).append(".\n");
        text.append("Credit Score: ").append(creditScore).append("\n");

        if (extraInfo != null && !extraInfo.isEmpty()) {
            text.append(extraInfo).append("\n");
        }

        text.append("\nRegards,\nProfessional Loan Management Team");

        message.setText(text.toString());
        mailSender.send(message);
    }

    // ✅ New: Send email after registration
    public void sendRegistrationSuccessEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(FROM_EMAIL);
        message.setSubject("Registration Successful");

        String text = "Dear " + name + ",\n\n"
                + "Welcome to the Professional Loan Management System!\n"
                + "Your registration was successful. You can now log in and apply for loans.\n\n"
                + "Regards,\nProfessional Loan Management Team";

        message.setText(text);
        mailSender.send(message);
    }
}
