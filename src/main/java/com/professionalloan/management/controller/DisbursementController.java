package com.professionalloan.management.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.professionalloan.management.model.Disbursement;
import com.professionalloan.management.model.Repayment;
import com.professionalloan.management.service.DisbursementService;
import com.professionalloan.management.service.RepaymentService;

@RestController
@RequestMapping("/api/disbursements")
@CrossOrigin(origins = "http://localhost:5173")
public class DisbursementController {

    @Autowired
    private DisbursementService disbursementService;

    @Autowired
    private RepaymentService repaymentService;

    @PostMapping("/disburse/{applicationId}")
    public ResponseEntity<?> disburseLoan(
            @PathVariable String applicationId,
            @RequestParam BigDecimal amount) {
        Disbursement disbursement = disbursementService.disburseLoan(applicationId, amount);
        return ResponseEntity.ok(disbursement);
    }
    
    
    
    
    
    
    
    //  DTO for response
    public static class DisbursementWithEmisResponse {
        private final Disbursement disbursement;
        private final List<Repayment> emis;

        public DisbursementWithEmisResponse(Disbursement disbursement, List<Repayment> emis) {
            this.disbursement = disbursement;
            this.emis = emis;
        }

        public Disbursement getDisbursement() {
            return disbursement;
        }

        public List<Repayment> getEmis() {
            return emis;
        }
    }
}