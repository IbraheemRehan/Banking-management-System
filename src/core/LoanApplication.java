package core;

import java.time.LocalDate;
import java.util.UUID;

public class LoanApplication {
    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    private String applicationId;    // Removed 'final' so it can be set when rehydrating
    private final String accountId;
    private final double loanAmount;
    private final LocalDate applicationDate;
    private LoanStatus status;
    private final String reason;
    private LocalDate decisionDate;

    public LoanApplication(String accountId,
                           double loanAmount,
                           LocalDate applicationDate,
                           String reason) {
        if (loanAmount <= 0) throw new IllegalArgumentException("Loan amount must be positive");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Reason required");

        this.applicationId   = UUID.randomUUID().toString();
        this.accountId       = accountId;
        this.loanAmount      = loanAmount;
        this.applicationDate = applicationDate;
        this.reason          = reason;
        this.status          = LoanStatus.PENDING;
        this.decisionDate    = null;
    }

    // Called by DAO when rehydrating from the DB - now it works
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public String getReason() {
        return reason;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    /** Mark this application APPROVED, record decision date. */
    public void approve() {
        if (status != LoanStatus.PENDING) {
            throw new IllegalStateException("Cannot approve when status is " + status);
        }
        this.status       = LoanStatus.APPROVED;
        this.decisionDate = LocalDate.now();
    }

    /** Mark this application REJECTED, record decision date. */
    public void reject() {
        if (status != LoanStatus.PENDING) {
            throw new IllegalStateException("Cannot reject when status is " + status);
        }
        this.status       = LoanStatus.REJECTED;
        this.decisionDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "LoanApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", loanAmount=" + loanAmount +
                ", applicationDate=" + applicationDate +
                ", status=" + status +
                (decisionDate != null ? ", decisionDate=" + decisionDate : "") +
                ", reason='" + reason + '\'' +
                '}';
    }
}
