package core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanManager extends User {
    private List<LoanApplication> loanApplications;

    public LoanManager(String username, String password, String accountId, String name) {
        super(username, password, Role.LOAN_MANAGER, accountId, name);  // Pass name to User constructor
        this.loanApplications = new ArrayList<>();
    }

    @Override
    public void performRoleSpecificTask() {
        System.out.println("Loan Manager is processing loan applications.");
    }

    // Retrieve a loan application by its ID
    public LoanApplication findLoanApplicationById(String applicationId) {
        for (LoanApplication application : loanApplications) {
            // Check if the applicationId matches
            if (application.getApplicationId().equals(applicationId)) {
                return application;
            }
        }
        return null;
    }

    public void applyForLoan(String accountId, double amount, String reason) {
        // Retrieve the account using the accountId
        Account account = AccountManager.getAccountById(accountId);  // Use the correct method name
// Ensure this works if AccountManager has getAccount method
        if (account == null) {
            System.out.println("Account not found!");
            return;
        }

        // Get the customerId
        String customerId = account.getUser().getUsername();

        // Get the current date
        LocalDate applicationDate = LocalDate.now();
        LoanApplication application = new LoanApplication(accountId, amount, applicationDate, reason);
        loanApplications.add(application); // Add the application to the list
    }

    // Reject a loan application by ID
    public boolean rejectLoanById(String applicationId) {
        LoanApplication application = findLoanApplicationById(applicationId);
        if (application != null && application.getStatus() == LoanApplication.LoanStatus.PENDING) {
            application.setStatus(LoanApplication.LoanStatus.REJECTED); // Use enum for status
            return true;
        }
        return false;
    }

    // Retrieve all loan applications
    public List<LoanApplication> getAllLoanApplications() {
        return loanApplications;
    }

    // Retrieve pending loan applications only
    public List<LoanApplication> getPendingLoans() {
        List<LoanApplication> pendingLoans = new ArrayList<>();
        for (LoanApplication application : loanApplications) {
            if (application.getStatus() == LoanApplication.LoanStatus.PENDING) { // Use enum
                pendingLoans.add(application);
            }
        }
        return pendingLoans;
    }

    // Process loan approval
    public void processLoanApproval(LoanApplication application) {
        System.out.println("Processing loan approval for application ID: " + application.getApplicationId());
    }
}
