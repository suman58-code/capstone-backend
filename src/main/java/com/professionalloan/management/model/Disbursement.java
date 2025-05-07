package com.professionalloan.management.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "disbursements")
public class Disbursement {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private BigDecimal disbursedAmount;
	    @Column(name = "disbursed_date")
	    private LocalDate disbursementDate;

	    @Enumerated(EnumType.STRING)
	    @Column(name = "status")
	    private DisbursementStatus status = DisbursementStatus.PENDING;

	    @OneToOne
	    @JoinColumn(name = "application_id")
	    private LoanApplication loanApplication;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public BigDecimal getDisbursedAmount() {
			return disbursedAmount;
		}

		public void setDisbursedAmount(BigDecimal disbursedAmount) {
			this.disbursedAmount = disbursedAmount;
		}

		public LocalDate getDisbursementDate() {
			return disbursementDate;
		}

		public void setDisbursementDate(LocalDate disbursementDate) {
			this.disbursementDate = disbursementDate;
		}

		public LoanApplication getLoanApplication() {
			return loanApplication;
		}

		public void setLoanApplication(LoanApplication loanApplication) {
			this.loanApplication = loanApplication;
		}

		public DisbursementStatus getStatus() {
			return status;
		}

		public void setStatus(DisbursementStatus status) {
			this.status = status;
		}
}
