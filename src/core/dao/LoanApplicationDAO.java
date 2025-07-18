// === File: core/dao/LoanApplicationDAO.java ===
package core.dao;

import core.LoanApplication;
import core.DBConnection;



import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanApplicationDAO {

    /** Insert a brand-new loan application (status=PENDING). */
    public static void add(LoanApplication la) throws SQLException {
        String sql = """
            INSERT INTO loan_applications
              (application_id, account_id, loan_amount, application_date, reason, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, la.getApplicationId());
            ps.setString(2, la.getAccountId());
            ps.setDouble(3, la.getLoanAmount());
            ps.setDate(4, Date.valueOf(la.getApplicationDate()));
            ps.setString(5, la.getReason());
            ps.setString(6, la.getStatus().name()); // PENDING
            ps.executeUpdate();
        }
    }

    /** Fetch only PENDING applications in date order. */
    public static List<LoanApplication> findPendingApplications() throws SQLException {
        String sql = """
            SELECT * FROM loan_applications
             WHERE status = 'PENDING'
             ORDER BY application_date
            """;
        List<LoanApplication> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LoanApplication la = new LoanApplication(
                        rs.getString("account_id"),
                        rs.getDouble("loan_amount"),
                        rs.getDate("application_date").toLocalDate(),
                        rs.getString("reason")
                );
                la.setApplicationId(rs.getString("application_id"));
                la.setStatus(LoanApplication.LoanStatus.valueOf(rs.getString("status")));
                Date dd = rs.getDate("decision_date");
                if (dd != null) la.setDecisionDate(dd.toLocalDate());
                out.add(la);
            }
        }
        return out;
    }
    public static List<LoanApplication> findAll() throws SQLException {
        String sql = "SELECT * FROM loan_applications ORDER BY application_date";
        List<LoanApplication> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // build from every row
                LoanApplication la = new LoanApplication(
                        rs.getString("account_id"),
                        rs.getDouble("loan_amount"),
                        rs.getDate("application_date").toLocalDate(),
                        rs.getString("reason")
                );
                la.setApplicationId(rs.getString("application_id"));
                la.setStatus(LoanApplication.LoanStatus.valueOf(rs.getString("status")));
                Date dd = rs.getDate("decision_date");
                if (dd != null) la.setDecisionDate(dd.toLocalDate());
                out.add(la);
            }
        }
        return out;
    }

    /**
     * Update status + decision_date of an application.
     * Caller must manage the Connection/transaction.
     */
    public static void update(LoanApplication la, Connection conn) throws SQLException {
        String sql = """
        UPDATE loan_applications
           SET status = ?,
               application_date = ?,
               reason = ?
         WHERE application_id = ?
        """;
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, la.getStatus().name());
            p.setDate(2, Date.valueOf(la.getApplicationDate()));
            p.setString(3, la.getReason());
            p.setString(4, la.getApplicationId());
            p.executeUpdate();
        }
    }


    /** Find one application by its ID. */
    public static LoanApplication findById(String applicationId) throws SQLException {
        String sql = "SELECT * FROM loan_applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                LoanApplication la = new LoanApplication(
                        rs.getString("account_id"),
                        rs.getDouble("loan_amount"),
                        rs.getDate("application_date").toLocalDate(),
                        rs.getString("reason")
                );
                la.setApplicationId(rs.getString("application_id"));
                la.setStatus(LoanApplication.LoanStatus.valueOf(rs.getString("status")));
                Date dd = rs.getDate("decision_date");
                if (dd != null) la.setDecisionDate(dd.toLocalDate());
                return la;
            }
        }
    }
}
