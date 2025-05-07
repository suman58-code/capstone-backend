package com.professionalloan.management.controller;

import com.professionalloan.management.model.LoanApplication;
import com.professionalloan.management.model.ApplicationStatus;
import com.professionalloan.management.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanApplicationController {

    private static final String DOWNLOAD_URL_PREFIX = "/api/loans/documents/download/";
    private static final String PF_PREFIX = "pf_account_";
    private static final String SALARY_PREFIX = "salary_slip_";
    private static final String PF_DISPLAY_NAME = "PF Account Statement";
    private static final String SALARY_DISPLAY_NAME = "Salary Slip";

    @Autowired
    private LoanApplicationService loanService;

    @PostMapping(value = "/apply", consumes = "multipart/form-data")
    public ResponseEntity<?> submitApplication(
            @RequestParam("name") String name,
            @RequestParam("profession") String profession,
            @RequestParam("purpose") String purpose,
            @RequestParam("loanAmount") BigDecimal loanAmount,
            @RequestParam("panCard") String panCard,
            @RequestParam("tenureInMonths") Integer tenureInMonths,
            @RequestParam("userId") Long userId,
            @RequestParam("pfAccountPdf") MultipartFile pfAccountPdf,
            @RequestParam("salarySlip") MultipartFile salarySlip
    ) throws IOException {
        if (pfAccountPdf.getSize() > 5 * 1024 * 1024 || salarySlip.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("PDF files must be under 5MB");
        }
        LoanApplication savedApplication = loanService.submitApplicationWithFiles(
                name, profession, purpose, loanAmount, panCard,
                tenureInMonths, userId, pfAccountPdf, salarySlip
        );
        return ResponseEntity.ok(savedApplication);
    }

    @PutMapping("/update-status/{applicationId}")
    public ResponseEntity<?> updateLoanStatusWithComment(
            @PathVariable String applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        LoanApplication updated = loanService.updateLoanStatusWithComment(
                applicationId, ApplicationStatus.valueOf(status.toUpperCase()), comment
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        return ResponseEntity.ok(loanService.getAllApplications());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanApplication>> getUserApplications(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getApplicationsByUserId(userId));
    }

    @GetMapping("/documents/user/{userId}")
    public ResponseEntity<List<DocumentInfo>> getUserDocuments(@PathVariable Long userId) {
        List<LoanApplication> applications = loanService.getApplicationsByUserId(userId);
        List<DocumentInfo> docs = new ArrayList<>();
        for (LoanApplication app : applications) {
            if (app.getPfAccountPdf() != null) {
                docs.add(new DocumentInfo(
                        app.getApplicationId(),
                        PF_DISPLAY_NAME,
                        PF_PREFIX + app.getApplicationId() + ".pdf",
                        DOWNLOAD_URL_PREFIX + app.getApplicationId() + "/pf"
                ));
            }
            if (app.getSalarySlip() != null) {
                docs.add(new DocumentInfo(
                        app.getApplicationId(),
                        SALARY_DISPLAY_NAME,
                        SALARY_PREFIX + app.getApplicationId() + ".pdf",
                        DOWNLOAD_URL_PREFIX + app.getApplicationId() + "/salary"
                ));
            }
        }
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/documents/application/{applicationId}")
    public ResponseEntity<List<DocumentInfo>> getApplicationDocuments(@PathVariable String applicationId) {
        LoanApplication app = loanService.getApplicationById(applicationId);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        List<DocumentInfo> docs = new ArrayList<>();
        if (app.getPfAccountPdf() != null) {
            docs.add(new DocumentInfo(
                    app.getApplicationId(),
                    PF_DISPLAY_NAME,
                    PF_PREFIX + app.getApplicationId() + ".pdf",
                    DOWNLOAD_URL_PREFIX + app.getApplicationId() + "/pf"
            ));
        }
        if (app.getSalarySlip() != null) {
            docs.add(new DocumentInfo(
                    app.getApplicationId(),
                    SALARY_DISPLAY_NAME,
                    SALARY_PREFIX + app.getApplicationId() + ".pdf",
                    DOWNLOAD_URL_PREFIX + app.getApplicationId() + "/salary"
            ));
        }
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/documents/download/{applicationId}/{type}")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable String applicationId,
            @PathVariable String type) {
        LoanApplication app = loanService.getApplicationById(applicationId);
        byte[] fileData;
        String fileName;

        if ("pf".equals(type)) {
            fileData = app.getPfAccountPdf();
            fileName = PF_PREFIX + applicationId + ".pdf";
        } else if ("salary".equals(type)) {
            fileData = app.getSalarySlip();
            fileName = SALARY_PREFIX + applicationId + ".pdf";
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (fileData == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(fileData);
    }

    // Inner class to structure document metadata
    public static class DocumentInfo {
        private String applicationId;
        private String documentType;
        private String fileName;
        private String downloadUrl;

        public DocumentInfo(String applicationId, String documentType, String fileName, String downloadUrl) {
            this.applicationId = applicationId;
            this.documentType = documentType;
            this.fileName = fileName;
            this.downloadUrl = downloadUrl;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getDocumentType() {
            return documentType;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }
    }
}
