package core;

public class Account {
    private final String accountId;
    private double balance;
    private final User user;
    private final String pin;
    private double loanAmount;

    private static final double MAX_LOAN_LIMIT = 50000.0;
    private static final double INTEREST_RATE  = 0.05;

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Account(String accountId, double balance, User user, String pin) {
        if (user == null) throw new IllegalArgumentException("Account must have a non-null User");
        this.accountId = accountId;
        this.balance = balance;
        this.user = user;
        this.pin = pin;
        this.loanAmount = 0.0;
    }

    public String getAccountId() { return accountId; }
    public double getBalance()   { return balance; }
    public User getUser()        { return user; }
    public String getPin()       { return pin; }
    public double getLoanAmount(){ return loanAmount; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit > 0");
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) return false;
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public void applyLoan(double amt) {
        if (amt <= 0) throw new IllegalArgumentException("Loan must be > 0");
        if (loanAmount + amt > MAX_LOAN_LIMIT) {
            throw new IllegalArgumentException("Exceeds max loan limit");
        }
        loanAmount += amt;
        balance += amt;
    }

    public void payLoan(double amt) {
        if (amt <= 0 || amt > loanAmount) {
            throw new IllegalArgumentException("Invalid loan payment");
        }
        if (balance < amt) {
            throw new IllegalArgumentException("Insufficient balance to pay loan");
        }
        loanAmount -= amt;
        balance -= amt;
    }

    public void applyInterest() {
        if (loanAmount <= 0) return;
        double interest = loanAmount * INTEREST_RATE;
        loanAmount += interest;
        balance += interest;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    @Override
    public String toString() {
        return "Account[" + accountId +
                ", bal=" + balance +
                ", user=" + user.getUsername() +
                ", loan=" + loanAmount + "]";
    }
}
