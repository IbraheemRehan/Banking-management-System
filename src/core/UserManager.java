package core;

import core.dao.AccountDAO;
import core.dao.PendingUserDAO;
import core.dao.UserDAO;

import java.sql.SQLException;
import java.util.*;

/**
 * Central place for all user-related operations.
 * Everything goes through the DAOs so the DB is always kept in sync.
 */
public class UserManager {
    // In-memory cache of active users and accounts
    private static final Map<String, Account> accounts = new HashMap<>();
    private static final Map<String, User> users = new HashMap<>();

    // Load users from the database into cache
    static {
        try {
            for (User u : UserDAO.findAll()) {
                users.put(u.getUsername(), u);
                // Optionally, preload accounts cache if needed:
                // accounts.put(u.getAccountId(), AccountDAO.findById(u.getAccountId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Lookup user from cache. */
    public static User getUser(String username) {
        return users.get(username);
    }

    /** Adds an account to the DB and memory cache. */
    public static void addAccount(Account account) {
        if (accounts.containsKey(account.getAccountId())) {
            throw new IllegalArgumentException("Account ID already exists: " + account.getAccountId());
        }
        try {
            AccountDAO.add(account);            // write to DB
            accounts.put(account.getAccountId(), account);  // cache
        } catch (SQLException e) {
            throw new RuntimeException("Failed to persist new account: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticate a user with username, password and role.
     * On success, caches the user and returns it; otherwise returns null.
     */
    public static User authenticate(String username, String password, Role role) {
        try {
            User u = UserDAO.findByUsername(username);
            if (u != null && u.getPassword().equals(password) && u.getRole() == role) {
                users.put(username, u);
                return u;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Register a new user by adding to pending_users table.
     * Generates a unique-ish accountId.
     */
    public static boolean register(String username, String password, Role role, String name) {
        // short random numeric suffix, always < 20 chars
        String accountId = "AC" + String.format("%06d", new Random().nextInt(1_000_000));
        User u = UserFactory.create(username, password, role, accountId, name);
        try {
            PendingUserDAO.addPending(u);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Approve a pending user:
     *  - moves from pending_users to users
     *  - creates their account if needed
     *  - updates in-memory caches
     */
    public static boolean approve(String username) {
        try {
            // 1) Load the pending user
            User u = PendingUserDAO.getByUsername(username);
            if (u == null) return false;

            // 2) Insert into users if not present
            if (UserDAO.findByUsername(username) == null) {
                UserDAO.add(u);
            }

            // 3) Create Account object for this user
            Account acct = new Account(u.getAccountId(), 0.0, u, "0000");

            // 4) Insert account if missing
            if (AccountDAO.findById(u.getAccountId()) == null) {
                AccountDAO.add(acct);
            }

            // 5) Remove from pending_users
            PendingUserDAO.remove(username);

            // 6) Update caches
            users.put(username, u);
            accounts.put(acct.getAccountId(), acct);

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Reject a pending user by removing from pending_users. */
    public static boolean reject(String username) {
        try {
            PendingUserDAO.remove(username);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Delete an active user and their account from DB and cache. */
    public static boolean delete(String username) {
        User u = users.get(username);
        if (u == null) return false;
        try {
            AccountDAO.deleteByUsername(username);
            UserDAO.delete(username);
            users.remove(username);
            // Also remove account from cache
            accounts.values().removeIf(a -> a.getUser().getUsername().equals(username));
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** List all pending registrations. */
    public static List<User> getPendingUsers() {
        try {
            return PendingUserDAO.getAllPending();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** List active users filtered by role. */
    public static List<User> getUsersByRole(Role role) {
        List<User> out = new ArrayList<>();
        for (User u : users.values()) {
            if (u.getRole() == role) {
                out.add(u);
            }
        }
        return out;
    }

    /** Check if a customer exists by account ID. */
    public static boolean isValidCustomerId(String accountId) {
        return users.values().stream()
                .filter(u -> u instanceof Customer)
                .anyMatch(u -> u.getAccountId().equals(accountId));
    }

    /** Get a customer by account ID. */
    public static Customer getCustomerById(String accountId) {
        for (User u : users.values()) {
            if (u instanceof Customer && u.getAccountId().equals(accountId)) {
                return (Customer) u;
            }
        }
        return null;
    }
}
