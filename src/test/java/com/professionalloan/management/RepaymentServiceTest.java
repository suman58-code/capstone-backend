package com.professionalloan.management;

import com.professionalloan.management.exception.EMIAlreadyPaidException;
import com.professionalloan.management.exception.LoanApplicationNotFoundException;
import com.professionalloan.management.exception.RepaymentNotFoundException;
import com.professionalloan.management.model.*;
import com.professionalloan.management.repository.LoanApplicationRepository;
import com.professionalloan.management.repository.RepaymentRepository;
import com.professionalloan.management.service.RepaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepaymentServiceTest {

    @InjectMocks
    private RepaymentService service;

    @Mock
    private RepaymentRepository repaymentRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRepaymentById_notFound() {
        when(repaymentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RepaymentNotFoundException.class, () -> service.getRepaymentById(1L));
    }

    @Test
    void generateEMISchedule_loanNotFound() {
        when(loanApplicationRepository.findById("id")).thenReturn(Optional.empty());
        assertThrows(LoanApplicationNotFoundException.class, () -> service.generateEMISchedule("id", 12));
    }

    @Test
    void makePayment_alreadyPaid() {
        Repayment emi = new Repayment();
        emi.setStatus("PAID");
        when(repaymentRepository.findById(1L)).thenReturn(Optional.of(emi));
        assertThrows(EMIAlreadyPaidException.class, () -> service.makePayment(1L));
    }
}