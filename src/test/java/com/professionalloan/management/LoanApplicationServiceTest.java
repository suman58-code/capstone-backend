package com.professionalloan.management;

import com.professionalloan.management.exception.DuplicateLoanApplicationException;
import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.model.*;
import com.professionalloan.management.repository.LoanApplicationRepository;
import com.professionalloan.management.repository.UserRepository;
import com.professionalloan.management.service.DocumentService;
import com.professionalloan.management.service.LoanApplicationService;
import com.professionalloan.management.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanApplicationServiceTest {

    @InjectMocks
    private LoanApplicationService service;

    @Mock
    private LoanApplicationRepository loanRepo;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitApplicationWithFiles_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.submitApplicationWithFiles(
                "name", "profession", "purpose", BigDecimal.TEN, "PAN", 12, 1L, null, null
        ));
    }

    @Test
    void submitApplicationWithFiles_duplicateLoan() {
        User user = new User();
        LoanApplication app = new LoanApplication();
        app.setStatus(ApplicationStatus.APPROVED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepo.findByUser_Id(1L)).thenReturn(List.of(app));
        assertThrows(DuplicateLoanApplicationException.class, () -> service.submitApplicationWithFiles(
                "name", "profession", "purpose", BigDecimal.TEN, "PAN", 12, 1L, null, null
        ));
    }

    @Test
    void getApplicationsByUserId_returnsEmptyIfNull() {
        assertTrue(service.getApplicationsByUserId(null).isEmpty());
    }

    @Test
    void updateLoanStatusWithComment_updatesStatusAndComment() {
        LoanApplication app = new LoanApplication();
        app.setUser(new User());
        when(loanRepo.findById("id")).thenReturn(Optional.of(app));
        when(loanRepo.save(app)).thenReturn(app);

        LoanApplication result = service.updateLoanStatusWithComment("id", ApplicationStatus.APPROVED, "ok");
        assertEquals(ApplicationStatus.APPROVED, result.getStatus());
        assertEquals("ok", result.getStatusComment());
    }
}