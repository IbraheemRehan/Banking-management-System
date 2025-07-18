// === File: core/dao/LedgerDAO.java ===
package core.dao;

import core.DBConnection;
import core.LedgerEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LedgerDAO {

    /** Inserts one ledger entry. */
    public static void add(LedgerEntry entry) throws SQLException {
        String sql = "INSERT INTO ledger(" +
                " entry_id, account_id, entry_date, type, amount, balance_after, description" +
                ") VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, entry.getEntryId());
            p.setString(2, entry.getAccountId());
            p.setTimestamp(3, Timestamp.valueOf(entry.getEntryDate()));
            p.setString(4, entry.getType());
            p.setDouble(5, entry.getAmount());
            p.setDouble(6, entry.getBalanceAfter());
            p.setString(7, entry.getDescription());
            p.executeUpdate();
        }
    }


    /** Reads all ledger entries, ordered by timestamp. */
    public static List<LedgerEntry> findAll() throws SQLException {
        String sql = "SELECT * FROM ledger ORDER BY entry_date";
        List<LedgerEntry> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(new LedgerEntry(
                        rs.getString("entry_id"),
                        rs.getString("account_id"),
                        rs.getTimestamp("entry_date").toLocalDateTime(),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getString("description")
                ));
            }
        }
        return list;
    }
}
