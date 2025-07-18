package core;

import java.time.LocalDate;

public class LoanPayment {
    private String paymentId;
    private LoanApplication loanApplication;
    private double paymentAmount;
    private LocalDate paymentDate;

    public void processLoan(LoanApplication loanApplication) {
        if (loanApplication.getLoanAmount() > 0) {
            loanApplication.approve(); // Use approve() instead of approveLoan()
            System.out.println("Loan approved: " + loanApplication.getApplicationId());
        } else {
            loanApplication.reject(); // Use reject() instead of rejectLoan()
            System.out.println("Loan rejected: " + loanApplication.getApplicationId());
        }
    }
    public LoanPayment(String paymentId, LoanApplication loanApplication, double paymentAmount) {
        this.paymentId = paymentId;
        this.loanApplication = loanApplication;
        this.paymentAmount = paymentAmount;
        this.paymentDate = LocalDate.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void applyPayment() {
        double remainingAmount = loanApplication.getLoanAmount() - paymentAmount;
        if (remainingAmount <= 0) {
            loanApplication.approve(); // Use approve() instead of approveLoan()
        } else {
            loanApplication.reject(); // Use reject() instead of rejectLoan()
        }
    }
}
