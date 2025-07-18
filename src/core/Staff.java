package core;

public class Staff extends User {

    public Staff(String username, String password, String accountId, String name) {
        super(username, password, Role.STAFF, accountId, name);  // Pass name to User constructor
    }


    @Override
    public void performRoleSpecificTask() {
        System.out.println("Staff member is performing role-specific tasks.");
    }
}
