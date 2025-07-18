package core.dao;

import core.DBConnection;
import core.User;
import core.Role;
import core.UserFactory;
import core.dao.UserDAO;
import core.dao.AccountDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PendingUserDAO {
    public static void addPending(User u) throws SQLException {
        String sql = "INSERT INTO pending_users(username, password_hash, role, account_id, name) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, u.getUsername());
            p.setString(2, u.getPassword());
            p.setString(3, u.getRole().name());
            p.setString(4, u.getAccountId());
            p.setString(5, u.getName());
            p.executeUpdate();
        }
    }

    public static List<User> getAllPending() throws SQLException {
        String sql = "SELECT username, password_hash, role, account_id, name FROM pending_users";
        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(UserFactory.create(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")),
                        rs.getString("account_id"),
                        rs.getString("name")
                ));
            }
        }
        return list;
    }
    public static boolean exists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pending_users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }


    public static void remove(String username) throws SQLException {
        String sql = "DELETE FROM pending_users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.executeUpdate();
        }
    }

    public static User getByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash, role, account_id, name FROM pending_users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return UserFactory.create(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role")),
                            rs.getString("account_id"),
                            rs.getString("name")
                    );
                }
            }
        }
        return null;
    }


}
