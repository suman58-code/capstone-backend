package com.professionalloan.management.dto;

public class SimulatedPaymentRequest {
    private Long repaymentId;
    private String method;     // e.g., GPay, Card, NetBanking
    private String txnId;      // Fake transaction ID

    // --- Getters and Setters ---
    public Long getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(Long repaymentId) {
        this.repaymentId = repaymentId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}
