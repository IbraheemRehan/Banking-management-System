package core.dao;

import core.Account;
import core.DBConnection;
import core.User;
import core.UserManager;
import core.dao.UserDAO;
import core.dao.AccountDAO;
import java.sql.SQLException;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    /** Insert a brand-new account row. */
    public static void add(Account a) throws SQLException {
        String sql = "INSERT INTO accounts(account_id,balance,username,pin,loan_amount) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, a.getAccountId());
            p.setDouble(2, a.getBalance());
            p.setString(3, a.getUser().getUsername());
            p.setString(4, a.getPin());
            p.setDouble(5, a.getLoanAmount());
            p.executeUpdate();
        }
    }

    /** Overloaded update method to use existing connection, for transaction control. */
    public static void update(Account a, Connection c) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, pin = ?, loan_amount = ? WHERE account_id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setDouble(1, a.getBalance());
            p.setString(2, a.getPin());
            p.setDouble(3, a.getLoanAmount());
            p.setString(4, a.getAccountId());
            p.executeUpdate();
        }
    }
    // in core/dao/AccountDAO.java

    /** Convenience update using its own connection */
    public static void update(Account a) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            update(a, c);
        }
    }
    // in core/dao/AccountDAO.java, alongside your existing methods:

    /**
     * Check whether an account with the given ID exists, using the provided Connection.
     */
    public static boolean accountExists(String accountId, Connection c) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE account_id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, accountId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }


    /** Delete all accounts for a given username (used when deleting a user). */
    public static void deleteByUsername(String username) throws SQLException {
        String sql = "DELETE FROM accounts WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.executeUpdate();
        }
    }

    public static int getNextAccountId() throws SQLException {
        String sql = "SELECT NVL(MAX(account_id), 0) + 1 FROM accounts";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to get next account ID.");
    }
    public static double getBalance(String accountId, Connection c) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, accountId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    throw new SQLException("Account ID not found: " + accountId);
                }
            }
        }
    }


    /** Look up one account by its ID. */
    public static Account findById(String accountId) throws SQLException {
        String sql = "SELECT account_id, balance, username, pin, loan_amount FROM accounts WHERE account_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, accountId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    User u = UserManager.getUser(rs.getString("username"));
                    Account a = new Account(
                            rs.getString("account_id"),
                            rs.getDouble("balance"),
                            u,
                            rs.getString("pin")
                    );
                    a.setLoanAmount(rs.getDouble("loan_amount"));
                    return a;
                }
            }
        }
        return null;
    }

    /**
     * Load every account from the DB, linking each to the User cache in UserManager.
     * Accounts whose username isn’t in UserManager are skipped with a warning.
     */
    public static List<Account> findAll() throws SQLException {
        List<Account> out = new ArrayList<>();
        String sql = "SELECT account_id, balance, username, pin, loan_amount FROM accounts";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String accountId  = rs.getString("account_id");
                double balance    = rs.getDouble("balance");
                String username   = rs.getString("username");
                String pin        = rs.getString("pin");
                double loanAmount = rs.getDouble("loan_amount");

                User user = UserManager.getUser(username);
                if (user == null) {
                    System.err.println("⚠️  Skipping account " + accountId +
                            ": no such user '" + username + "'");
                    continue;
                }

                Account acct = new Account(accountId, balance, user, pin);
                acct.setLoanAmount(loanAmount);
                out.add(acct);
            }
        }
        return out;
    }
}
