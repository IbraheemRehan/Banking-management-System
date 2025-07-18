package core;

import core.dao.TransactionDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionManager {
    private static int counter = 1;

    /**
     * Sends money and persists the transaction.
     */
    public String sendMoney(Account from, Account to, double amount, String description) {
        if (from.getAccountId().equals(to.getAccountId())) {
            return "Transaction failed: cannot send to yourself.";
        }
        if (!UserManager.isValidCustomerId(to.getAccountId())) {
            return "Transaction failed: recipient not found.";
        }
        if (from.getBalance() < amount) {
            return "Transaction failed: insufficient funds.";
        }

        // 1) Update balances in-memory (ideally persist via AccountDAO.update, not shown here)
        from.withdraw(amount);
        to.deposit(amount);

        // 2) Create and record the transaction
        String txId = "T" + (counter++);
        LocalDateTime ts = LocalDateTime.now();
        Transaction tx = new Transaction(txId, from.getAccountId(), to.getAccountId(), amount, ts, description);

        try {
            TransactionDAO.add(tx);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Transaction failed: unable to record in database.";
        }

        return "Transaction successful!";
    }

    /**
     * Returns the full history for that account, pulling from DB.
     */
    public List<Transaction> getTransactionHistory(String accountId) {
        try {
            return TransactionDAO.findByAccount(accountId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return List.of();
        }
    }

    /** For CLI or debug; prints history to stdout. */
    public void printTransactions(String accountId) {
        getTransactionHistory(accountId)
                .forEach(System.out::println);
    }
}
