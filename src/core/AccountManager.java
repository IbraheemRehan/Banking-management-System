package core;

import core.dao.AccountDAO;
import core.dao.LoanApplicationDAO;
import core.dao.TransactionDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages all Account → LoanApplication state,
 * loading and persisting via DAO so that the database is always the source of truth.
 */
public class AccountManager {
    private static final Map<String, Account> accounts = new HashMap<>();
    private static final Map<String, LoanApplication> loanApplications = new HashMap<>();

    static {
        try {
            for (Account a : AccountDAO.findAll()) {
                accounts.put(a.getAccountId(), a);
            }
            for (LoanApplication la : LoanApplicationDAO.findAll()) {
                loanApplications.put(la.getApplicationId(), la);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load initial data.");
        }
    }

    public static Account getAccountById(String accountId) {
        return accounts.get(accountId);
    }

    public static List<LoanApplication> getLoanApplications() {
        return new ArrayList<>(loanApplications.values());
    }

    public static void addLoanApplication(LoanApplication la) {
        try {
            LoanApplicationDAO.add(la);
            loanApplications.put(la.getApplicationId(), la);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Lookup the Account belonging to a given User. */
    public static Account getAccountForUser(User user) {
        return accounts.values().stream()
                .filter(a -> a.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }

    /** Returns all accounts whose user role is CUSTOMER. */
    public static List<Account> getAllCustomerAccounts() {
        List<Account> out = new ArrayList<>();
        for (Account a : accounts.values()) {
            if (a.getUser() != null && a.getUser().getRole() == Role.CUSTOMER) {
                out.add(a);
            }
        }
        return out;
    }

    /** Add a new account (both in-memory and persist to DB). */
    public static void addAccount(Account account) {
        if (accounts.containsKey(account.getAccountId())) {
            throw new IllegalArgumentException("Account ID already exists: " + account.getAccountId());
        }
        try {
            AccountDAO.add(account);
            accounts.put(account.getAccountId(), account);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to persist new account: " + e.getMessage(), e);
        }
    }

    /** Remove an account (e.g., when deleting a user). */
    public static void removeAccount(String accountId) {
        try {
            Account account = accounts.get(accountId);
            if (account != null) {
                AccountDAO.deleteByUsername(account.getUser().getUsername());
                accounts.remove(accountId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }

    //
    // ----------- Loan Application Logic ------------
    //

    /** Get loan application by ID */
    public static LoanApplication getLoanApplicationById(String id) {
        return loanApplications.get(id);
    }

    /** Apply for a loan for any account. Adds to DB and in-memory map. */
    public static void applyLoanToAccount(String accountId, double amt, String reason) {
        if (amt <= 0) throw new IllegalArgumentException("Loan amount must be > 0");
        Account acct = getAccountById(accountId);
        if (acct == null) throw new IllegalArgumentException("No such account: " + accountId);
        LoanApplication la = new LoanApplication(accountId, amt, LocalDate.now(), reason);
        addLoanApplication(la);
    }

    /** Approve loan for any loan application by its ID */
    public static boolean approveLoan(String applicationId) {
        LoanApplication la = loanApplications.get(applicationId);
        if (la == null || la.getStatus() != LoanApplication.LoanStatus.PENDING) {
            return false;
        }
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            la.approve();
            LoanApplicationDAO.update(la, c);

            Account acct = accounts.get(la.getAccountId());
            if (acct == null) {
                c.rollback();
                return false;
            }
            acct.applyLoan(la.getLoanAmount());
            AccountDAO.update(acct, c);

            Transaction tx = new Transaction(
                    "T" + UUID.randomUUID(),
                    "LEDGER",
                    acct.getAccountId(),
                    la.getLoanAmount(),
                    LocalDateTime.now(),
                    "Loan Approved"
            );
            TransactionDAO.add(tx, c);

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Reject loan (update status + decision date in DB and memory) */
    public static boolean rejectLoan(String applicationId) {
        LoanApplication la = loanApplications.get(applicationId);
        if (la == null || la.getStatus() != LoanApplication.LoanStatus.PENDING) return false;

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            la.reject();
            LoanApplicationDAO.update(la, c);

            c.commit();

            // Update in-memory state
            loanApplications.put(applicationId, la);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Make a payment towards a loan */
    public static boolean payLoan(String accountId, double payment) {
        Account acct = accounts.get(accountId);
        if (acct == null) return false;
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            acct.payLoan(payment);
            AccountDAO.update(acct, c);

            Transaction tx = new Transaction(
                    "T" + UUID.randomUUID(),
                    acct.getAccountId(),
                    "LEDGER",
                    payment,
                    LocalDateTime.now(),
                    "Loan Payment"
            );
            TransactionDAO.add(tx, c);

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
