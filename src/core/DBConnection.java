package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XE";  // Oracle XE service name
    private static final String USER = "system";      // your Oracle username
    private static final String PASSWORD = "123";  // your Oracle password

    // Optional: load Oracle JDBC driver explicitly (helps sometimes)
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        // This is where DriverManager.getConnection is called
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
