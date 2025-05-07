package com.professionalloan.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.professionalloan.management.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // ✅ Optional: Add this for a cleaner login method
    Optional<User> findByEmailAndPassword(String email, String password);
}