package core.service;

import core.dao.PendingUserDAO;
import core.dao.UserDAO;
import core.dao.AccountDAO;
import core.User;
import core.Account;
import core.Customer;

import java.sql.SQLException;
import java.util.Map;

public class StaffService {
    private Map<String, User> users;  // Your in-memory user map

    public StaffService(Map<String, User> users) {
        this.users = users;
    }

    /**
     * Approves a pending user by moving them to active users.
     * Returns true if successful, false otherwise.
     */
    public boolean approveUser(String username) {
        try {
            User u = PendingUserDAO.getByUsername(username);
            if (u == null) {
                System.out.println("No pending user found with username: " + username);
                return false;
            }

            // Add user to active users storage
            UserDAO.add(u);

            // Add account for the user
            Account a = (u instanceof Customer) ? ((Customer) u).getAccount()
                    : new Account(u.getAccountId(), 0.0, u, "0000");
            AccountDAO.add(a);

            // Remove user from pending list
            PendingUserDAO.remove(username);

            // Update in-memory user map
            users.put(username, u);

            System.out.println("User approved successfully: " + username);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
