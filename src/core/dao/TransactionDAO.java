// src/core/dao/TransactionDAO.java
package core.dao;

import core.DBConnection;
import core.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDAO {

    public static void add(Transaction tx) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            add(tx, c);
            c.commit();
        }
    }

    public static void add(Transaction tx, Connection c) throws SQLException {
        // 0) sanity‐check that both sides exist as real accounts
        if (!AccountDAO.accountExists(tx.getFromAccount(), c)) {
            throw new SQLException("Unknown from_account: " + tx.getFromAccount());
        }
        if (!AccountDAO.accountExists(tx.getToAccount(), c)) {
            throw new SQLException("Unknown to_account: " + tx.getToAccount());
        }

        // 1) record the transaction
        String sqlTx = """
            INSERT INTO transactions(
              transaction_id, from_account, to_account, amount, timestamp, description
            ) VALUES(?,?,?,?,?,?)
            """;
        try (PreparedStatement p = c.prepareStatement(sqlTx)) {
            String txId = tx.getTransactionId();
            if (txId.length() > 64) txId = txId.substring(0, 64);
            p.setString(1, txId);
            p.setString(2, tx.getFromAccount());
            p.setString(3, tx.getToAccount());
            p.setDouble(4, tx.getAmount());
            p.setTimestamp(5, Timestamp.valueOf(tx.getTimestamp()));
            p.setString(6, tx.getDescription());
            if (p.executeUpdate() != 1) {
                throw new SQLException("Failed to insert into transactions.");
            }
        }

        // 2) ledger debit (FROM)
        String sqlLed = """
            INSERT INTO ledger(
              entry_id, account_id, entry_date, type, amount, balance_after, description
            ) VALUES(?,?,?,?,?,?,?)
            """;
        try (PreparedStatement p = c.prepareStatement(sqlLed)) {
            String entryId = UUID.randomUUID().toString();
            p.setString(1, entryId);
            p.setString(2, tx.getFromAccount());
            p.setTimestamp(3, Timestamp.valueOf(tx.getTimestamp()));
            p.setString(4, deriveType(tx.getFromAccount(), tx.getToAccount(), tx.getDescription(), true));
            p.setDouble(5, -tx.getAmount());
            double fromBal = AccountDAO.getBalance(tx.getFromAccount(), c) - tx.getAmount();
            p.setDouble(6, fromBal);
            p.setString(7, tx.getDescription());
            if (p.executeUpdate() != 1) {
                throw new SQLException("Failed to insert debit ledger entry.");
            }
        }

        // 3) ledger credit (TO)
        try (PreparedStatement p = c.prepareStatement(sqlLed)) {
            String entryId = UUID.randomUUID().toString();
            p.setString(1, entryId);
            p.setString(2, tx.getToAccount());
            p.setTimestamp(3, Timestamp.valueOf(tx.getTimestamp()));
            p.setString(4, deriveType(tx.getFromAccount(), tx.getToAccount(), tx.getDescription(), false));
            p.setDouble(5, tx.getAmount());
            double toBal = AccountDAO.getBalance(tx.getToAccount(), c) + tx.getAmount();
            p.setDouble(6, toBal);
            p.setString(7, tx.getDescription());
            if (p.executeUpdate() != 1) {
                throw new SQLException("Failed to insert credit ledger entry.");
            }
        }
    }

    private static String deriveType(String from, String to, String desc, boolean isDebit) {
        if (from.equals("LEDGER")) {
            return isDebit ? "LOAN_DISBURSE" : "LOAN_CREDIT";
        } else if (to.equals("LEDGER")) {
            return "LOAN_PAYMENT";
        } else {
            return isDebit ? "TRANSFER_OUT" : "TRANSFER_IN";
        }
    }

    public static java.util.List<Transaction> findByAccount(String accountId) throws SQLException {
        String sql = """
            SELECT transaction_id, from_account, to_account, amount, timestamp, description
              FROM transactions
             WHERE from_account = ? OR to_account = ?
             ORDER BY timestamp
            """;
        var out = new java.util.ArrayList<Transaction>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, accountId);
            p.setString(2, accountId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    out.add(new Transaction(
                            rs.getString("transaction_id"),
                            rs.getString("from_account"),
                            rs.getString("to_account"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getString("description")
                    ));
                }
            }
        }
        return out;
    }
}
