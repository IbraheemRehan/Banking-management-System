package core;

import java.sql.*;

public class DBSetup {

    public static void createTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1) USERS
            try {
                stmt.executeUpdate(
                        "CREATE TABLE users (\n" +
                                "  username       VARCHAR2(50) PRIMARY KEY,\n" +
                                "  password_hash  VARCHAR2(100) NOT NULL,\n" +
                                "  role           VARCHAR2(20)  NOT NULL,\n" +
                                "  account_id     VARCHAR2(36)  NOT NULL,\n" +
                                "  name           VARCHAR2(100) NOT NULL\n" +
                                ")"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            // 2) PENDING_USERS
            try {
                stmt.executeUpdate(
                        "CREATE TABLE pending_users (\n" +
                                "  username       VARCHAR2(50) PRIMARY KEY,\n" +
                                "  password_hash  VARCHAR2(100) NOT NULL,\n" +
                                "  role           VARCHAR2(20)  NOT NULL,\n" +
                                "  account_id     VARCHAR2(36)  NOT NULL\n" +
                                ")"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            // 3) ACCOUNTS
            try {
                stmt.executeUpdate(
                        "CREATE TABLE accounts (\n" +
                                "  account_id   VARCHAR2(36) PRIMARY KEY,\n" +
                                "  balance      NUMBER        NOT NULL,\n" +
                                "  username     VARCHAR2(50)  NOT NULL,\n" +
                                "  pin          VARCHAR2(10)  NOT NULL,\n" +
                                "  loan_amount  NUMBER DEFAULT 0\n" +  // <-- no comma here
                                ")"
                );

            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            // 4) TRANSACTIONS
            try {
                stmt.executeUpdate(
                        "CREATE TABLE transactions (\n" +
                                "  transaction_id VARCHAR2(64) PRIMARY KEY,\n" +
                                "  from_account   VARCHAR2(36) NOT NULL,\n" +
                                "  to_account     VARCHAR2(36) NOT NULL,\n" +
                                "  amount         NUMBER       NOT NULL,\n" +
                                "  timestamp      TIMESTAMP    NOT NULL,\n" +
                                "  description    VARCHAR2(255),\n" +
                                "  CONSTRAINT fk_from_account FOREIGN KEY(from_account) REFERENCES accounts(account_id),\n" +
                                "  CONSTRAINT fk_to_account   FOREIGN KEY(to_account)   REFERENCES accounts(account_id)\n" +
                                ")"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            // 5) LOAN_APPLICATIONS
            try {
                stmt.executeUpdate(
                        "CREATE TABLE loan_applications (\n" +
                                "  application_id   VARCHAR2(36) PRIMARY KEY,\n" +
                                "  account_id       VARCHAR2(36) NOT NULL,\n" +
                                "  loan_amount      NUMBER       NOT NULL,\n" +
                                "  application_date DATE         NOT NULL,\n" +
                                "  status           VARCHAR2(20) NOT NULL,\n" +
                                "  reason           VARCHAR2(255),\n" +
                                "  CONSTRAINT fk_account_id FOREIGN KEY(account_id) REFERENCES accounts(account_id)\n" +
                                ")"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            // 6) LEDGER
            try {
                stmt.executeUpdate(
                        "CREATE TABLE ledger (\n" +
                                "  entry_id      VARCHAR2(36) PRIMARY KEY,\n" +
                                "  account_id    VARCHAR2(36) NOT NULL,\n" +
                                "  entry_date    TIMESTAMP    NOT NULL,\n" +
                                "  type          VARCHAR2(20) NOT NULL,\n" +
                                "  amount        NUMBER       NOT NULL,\n" +
                                "  balance_after NUMBER       NOT NULL,\n" +
                                "  description   VARCHAR2(255),\n" +
                                "  CONSTRAINT fk_led_account FOREIGN KEY(account_id) REFERENCES accounts(account_id)\n" +
                                ")"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) throw e;
            }

            System.out.println("All tables ensured.");

            // … after creating tables …

// seed system user with a real default password "0000"
            // 1) Seed the LEDGER account first
            stmt.executeUpdate(
                    "MERGE INTO accounts a " +
                            "USING (SELECT 'LEDGER' AS account_id FROM dual) src " +
                            "ON (a.account_id = src.account_id) " +
                            "WHEN NOT MATCHED THEN " +
                            "  INSERT (account_id, balance, username, pin, loan_amount) " +
                            "  VALUES ('LEDGER', 0, 'system', '0000', 0)"
            );

// 2) Seed the system user
            stmt.executeUpdate(
                    "MERGE INTO users u " +
                            "USING (SELECT 'system' AS username FROM dual) src " +
                            "ON (u.username = src.username) " +
                            "WHEN NOT MATCHED THEN " +
                            "  INSERT (username, password_hash, role, account_id, name) " +
                            "    VALUES ('system','0000','STAFF','LEDGER','System Ledger')"
            );


// …

            // Demo users
            if (!initialUsersExist(conn)) {
                insertUser(conn, "customer1", "pass123", "CUSTOMER", "AC123001", "John Doe");
                insertAccount(conn, "AC123001", 5000.0, "customer1");

                insertUser(conn, "staff1", "staffpass", "STAFF", "AC123003", "Jane Smith");
                insertAccount(conn, "AC123003", 0.0, "staff1");

                insertUser(conn, "loanManager1", "loanpass", "LOAN_MANAGER", "AC123004", "Bob Johnson");
                insertAccount(conn, "AC123004", 0.0, "loanManager1");
            }

        } catch (SQLException e) {
            System.err.println("Error in DBSetup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean initialUsersExist(Connection conn) throws SQLException {
        try (PreparedStatement ps =
                     conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username IN ('customer1','staff1','loanManager1')")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static void insertUser(Connection conn,
                                   String username,
                                   String rawPassword,
                                   String role,
                                   String accountId,
                                   String fullName) throws SQLException {
        String hashedPassword = PasswordUtils.hash(rawPassword);

        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new RuntimeException("Password hash generation failed for user: " + username);
        }

        String sql = "INSERT INTO users(username, password_hash, role, account_id, name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.setString(4, accountId);
            pstmt.setString(5, fullName);
            pstmt.executeUpdate();
        }
    }

    private static void insertAccount(Connection conn,
                                      String accountId,
                                      double balance,
                                      String username) throws SQLException {
        String sql = "INSERT INTO accounts(account_id, balance, username, pin, loan_amount) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setDouble(2, balance);
            pstmt.setString(3, username);
            pstmt.setString(4, "0000");
            pstmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}
