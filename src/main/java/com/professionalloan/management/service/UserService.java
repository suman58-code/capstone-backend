package com.professionalloan.management.service;

import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.UserRepository;
import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.exception.DuplicateLoanApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z ]+$"); // Only letters and spaces

    // Register user (fails if email is already registered or invalid input)
    public boolean registerUser(User user) {
        // Validate name
        if (user.getName() == null || user.getName().trim().length() < 3) {
            throw new IllegalArgumentException("Name must be at least 3 characters");
        }
        if (!NAME_PATTERN.matcher(user.getName()).matches()) {
            throw new IllegalArgumentException("Name must contain only letters and spaces");
        }

        // Validate email
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // Validate password
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Check for duplicate email
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new DuplicateLoanApplicationException("Email already registered!");
        }

        // Assign role if not set
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        userRepository.save(user);

        // Send registration success email
        emailService.sendRegistrationSuccessEmail(user.getEmail(), user.getName());

        return true;
    }

    // Admin-only login with hardcoded credentials
    public User findByEmailAndPassword(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        // Admin login
        if ("admin@gmail.com".equals(email) && "admin".equals(password)) {
            User admin = new User();
            admin.setId(0L);
            admin.setEmail("admin@gmail.com");
            admin.setPassword("admin");
            admin.setName("Admin");
            admin.setRole("ADMIN");
            return admin;
        }

        // Prevent login for anyone else using admin email
        if ("admin@gmail.com".equals(email)) {
            throw new UserNotFoundException("Invalid admin credentials!");
        }

        // Normal user login from DB
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                if (user.getRole() == null) {
                    user.setRole("USER");
                }
                return user;
            } else {
                throw new UserNotFoundException("Invalid password!");
            }
        }

        throw new UserNotFoundException("User not found with email: " + email);
    }

    // Used for OTP verification and forgot password
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    // Used for profile update
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Save updated user details
    public void save(User user) {
        userRepository.save(user);
    }
}
