package com.professionalloan.management.controller;

import com.professionalloan.management.model.User;
import com.professionalloan.management.service.UserService;
import com.professionalloan.management.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private static final Random RANDOM = new Random();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    // --- Registration ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        boolean success = userService.registerUser(user);
        return success
                ? ResponseEntity.ok("Registration successful!")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed!");
    }

    // --- Login ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User loggedInUser = userService.findByEmailAndPassword(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (loggedInUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", loggedInUser.getId());
            response.put("name", loggedInUser.getName());
            response.put("email", loggedInUser.getEmail());
            response.put("role", loggedInUser.getRole());
            response.put("isAdmin", "ADMIN".equals(loggedInUser.getRole()));
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    // --- Forgot Password (Generate OTP) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String otp = String.format("%0" + OTP_LENGTH + "d", RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH)));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userService.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
        return ResponseEntity.ok("OTP sent to your email.");
    }

    // --- Verify OTP ---
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (user.getOtp() != null && user.getOtp().equals(otp)
                && user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            return ResponseEntity.ok("OTP is valid.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
    }

    // --- Reset Password using OTP ---
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String otp,
                                           @RequestParam String newPassword) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (user.getOtp() != null && user.getOtp().equals(otp)
                && user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            user.setPassword(newPassword);
            user.setOtp(null);
            user.setOtpExpiry(null);
            userService.save(user);
            return ResponseEntity.ok("Password reset successful.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
    }

    // --- Get User by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        return userOpt
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    // --- Update User Profile ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        user.setName(updatedUser.getName());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            user.setPassword(updatedUser.getPassword());
        }

        userService.save(user);
        return ResponseEntity.ok(user);
    }
}
