package com.professionalloan.management;

import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.model.Notification;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.NotificationRepository;
import com.professionalloan.management.repository.UserRepository;
import com.professionalloan.management.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createNotification_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.createNotification(1L, "msg", "type"));
    }

    @Test
    void createNotification_success() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Notification n = new Notification();
        when(notificationRepository.save(any())).thenReturn(n);
        assertNotNull(service.createNotification(1L, "msg", "type"));
    }
}