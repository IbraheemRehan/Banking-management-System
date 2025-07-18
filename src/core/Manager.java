package core;

public class Manager {
    private String managerId;
    private User user;

    public Manager(String managerId, User user) {
        this.managerId = managerId;
        this.user = user;
    }

    public String getManagerId() {
        return managerId;
    }

    public User getUser() {
        return user;
    }

    public void approveLoan(LoanApplication loanApplication) {
        // Adjust this if the method name is different
        loanApplication.approve(); // Assuming LoanApplication has an approve() method
    }

    public void rejectLoan(LoanApplication loanApplication) {
        // Adjust this if the method name is different
        loanApplication.reject(); // Assuming LoanApplication has a reject() method
    }
}
