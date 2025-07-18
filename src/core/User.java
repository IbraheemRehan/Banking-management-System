package core;

import java.util.Objects;

public abstract class User {
    private String username;
    private String password;
    private Role role;
    private String accountId;
    private String name; //
    // Add a name field


    public void setName(String name) {
        this.name = name;
    }


    public User(String username, String password, Role role, String accountId, String name) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountId = accountId;
        this.name = name; // Initialize the name field
    }

    public Role getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() { // Add getter for name
        return name;
    }

    // Abstract method to be implemented by subclasses
    public abstract void performRoleSpecificTask();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
