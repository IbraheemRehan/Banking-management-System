package core;


public class UserFactory {
    public static User create(String username, String password, Role role, String accountId, String name) {
        switch (role) {
            case CUSTOMER:
                return new Customer(username, password, Role.CUSTOMER, accountId, name);
            case STAFF:
                return new Staff(username, password, accountId, name);
            case LOAN_MANAGER:
                return new LoanManager(username, password, accountId, name);
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
