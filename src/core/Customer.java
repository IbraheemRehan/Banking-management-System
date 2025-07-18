package core;

public class Customer extends User {
    private Account account;

    public Customer(String username, String password, Role role, String accountId, String name) {
        super(username, password, role, accountId, name); // Pass name to User constructor
        this.account = new Account(accountId, 0.0, this, "defaultPin"); // Initialize account for Customer
    }

    @Override
    public void performRoleSpecificTask() {
        System.out.println("Customer is accessing their account.");
    }

    public Account getAccount() {
        return account;
    }
}
