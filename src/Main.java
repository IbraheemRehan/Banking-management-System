import core.DBSetup;
import ui.LoginFrame;
import core.AccountManager;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Create an instance of AccountManager
        AccountManager accountManager = new AccountManager();
        DBSetup.createTables();

        // Start the application by showing the login frame with the accountManager
        SwingUtilities.invokeLater(() -> new LoginFrame(accountManager).setVisible(true));
    }
}
