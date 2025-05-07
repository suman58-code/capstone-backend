package com.professionalloan.management;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.professionalloan.management.model.*;
import com.professionalloan.management.repository.*;
import com.professionalloan.management.service.DisbursementService;
import com.professionalloan.management.service.NotificationService;
import com.professionalloan.management.service.RepaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class DisbursementServiceTest {

    @InjectMocks
    private DisbursementService disbursementService;

    @Mock
    private DisbursementRepository disbursementRepo;

    @Mock
    private LoanApplicationRepository loanRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RepaymentService repaymentService;

    private LoanApplication loanApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loanApplication = new LoanApplication();
        loanApplication.setApplicationId("APP001");
        loanApplication.setStatus(ApplicationStatus.APPROVED);
        loanApplication.setLoanAmount(new BigDecimal("50000"));
        loanApplication.setTenureInMonths(12);
        User user = new User();
        user.setId(1L);
        loanApplication.setUser(user);

        when(disbursementRepo.save(any(Disbursement.class))).thenAnswer(i -> i.getArgument(0));
        when(loanRepo.save(any(LoanApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(loanRepo.findById("APP001")).thenReturn(Optional.of(loanApplication));

        
        when(repaymentService.generateEMISchedule(anyString(), anyInt())).thenReturn(java.util.Collections.emptyList());

        doNothing().when(notificationService).notifyDisbursement(anyLong(), anyString());
        doNothing().when(notificationService).sendDisbursementEmail(any(User.class), anyString(), any(BigDecimal.class));
    }

    @Test
    void testDisburseLoanSuccess() {
        Disbursement result = disbursementService.disburseLoan("APP001", new BigDecimal("50000"));

        assertNotNull(result);
        assertEquals(DisbursementStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("50000"), result.getDisbursedAmount());
        assertEquals(loanApplication, result.getLoanApplication());
        verify(disbursementRepo, times(2)).save(any(Disbursement.class));
        verify(loanRepo, times(1)).save(any(LoanApplication.class));
        verify(repaymentService).generateEMISchedule(eq("APP001"), eq(12));
        verify(notificationService).notifyDisbursement(eq(1L), eq("APP001"));
        verify(notificationService).sendDisbursementEmail(any(User.class), eq("APP001"), eq(new BigDecimal("50000")));
    }

    @Test
    void testDisburseLoanThrowsIfNotApproved() {
        loanApplication.setStatus(ApplicationStatus.PENDING);
        when(loanRepo.findById("APP001")).thenReturn(Optional.of(loanApplication));
        Exception ex = assertThrows(RuntimeException.class, () ->
                disbursementService.disburseLoan("APP001", new BigDecimal("50000"))
        );
        assertTrue(ex.getMessage().contains("must be APPROVED"));
    }

    @Test
    void testDisburseLoanThrowsIfAmountMismatch() {
        Exception ex = assertThrows(RuntimeException.class, () ->
                disbursementService.disburseLoan("APP001", new BigDecimal("99999"))
        );
        assertTrue(ex.getMessage().contains("must match approved loan amount"));
    }

    @Test
    void testDisburseLoanThrowsIfLoanNotFound() {
        when(loanRepo.findById("APP002")).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () ->
                disbursementService.disburseLoan("APP002", new BigDecimal("50000"))
        );
        assertTrue(ex.getMessage().contains("not found"));
    }
}