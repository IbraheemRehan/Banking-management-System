package core.dao;

import core.DBConnection;
import core.User;
import core.Role;
import core.UserFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static void add(User u) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role, account_id, name) VALUES(?,?,?,?,?)";
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
    // in core/dao/UserDAO.java

    /** Persist updates to a User’s name (or other fields) */
    /** Persist name/password/role changes */
    public static void update(User u) throws SQLException {
        String sql = "UPDATE users SET name = ?, password_hash = ?, role = ? WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, u.getName());
            p.setString(2, u.getPassword());
            p.setString(3, u.getRole().name());
            p.setString(4, u.getUsername());
            p.executeUpdate();
        }
    }



    public static void delete(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.executeUpdate();
        }
    }

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash, role, account_id, name FROM users WHERE username = ?";
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

    public static List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT username, password_hash, role, account_id, name FROM users";
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
}
