package ui;

import core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;


public class LoginFrame extends JFrame {
    private RoundedTextField usernameField;
    private RoundedPasswordField passwordField;
    private AccountManager accountManager;
    private JPanel rightPanel;
    private CardLayout cardLayout;


    private JToggleButton customerToggleButton, staffToggleButton, loanManagerToggleButton;
    private ButtonGroup roleGroup;

    public LoginFrame(AccountManager accountManager) {
        this.accountManager = accountManager;

        setTitle("Login - Bank Management System");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // Add the logo panel on the left
        JPanel logoPanel = createLogoPanel();
        add(logoPanel, BorderLayout.WEST);

        // Add the login panel on the right
        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);
        rightPanel.add(createLoginPanel(), "login");
        rightPanel.add(createRegisterPanel(), "register");
        add(rightPanel, BorderLayout.CENTER);

    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0, 51, 102));
        logoPanel.setPreferredSize(new Dimension(250, getHeight()));
        logoPanel.setLayout(new BorderLayout());

        // Load image safely
        URL imageUrl = getClass().getResource("/resource/Picsart_25-05-21_09-26-07-657 (1)-1.png");
        if (imageUrl == null) {
            System.out.println("Logo image not found!");
            return logoPanel;
        }

        ImageIcon logoIcon = new ImageIcon(imageUrl);
        Image img = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        logoPanel.add(logoLabel, BorderLayout.CENTER);

        return logoPanel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Welcome to Bank Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(0, 51, 102));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(usernameLabel, constraints);

        usernameField = new RoundedTextField(20);
        constraints.gridx = 1;
        formPanel.add(usernameField, constraints);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        constraints.gridy = 1;
        constraints.gridx = 0;
        formPanel.add(passwordLabel, constraints);

        passwordField = new RoundedPasswordField(20);
        constraints.gridx = 1;
        formPanel.add(passwordField, constraints);

        // Role selection
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(labelFont);
        constraints.gridy = 2;
        constraints.gridx = 0;
        formPanel.add(roleLabel, constraints);

        customerToggleButton = createRoundedToggleButton("Customer");
        staffToggleButton = createRoundedToggleButton("Staff");
        loanManagerToggleButton = createRoundedToggleButton("Loan Manager");

        roleGroup = new ButtonGroup();
        roleGroup.add(customerToggleButton);
        roleGroup.add(staffToggleButton);
        roleGroup.add(loanManagerToggleButton);

        // **No default selection**

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setBackground(Color.WHITE);
        rolePanel.add(customerToggleButton);
        rolePanel.add(staffToggleButton);
        rolePanel.add(loanManagerToggleButton);

        constraints.gridx = 1;
        formPanel.add(rolePanel, constraints);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 95));

        JButton loginButton = new RoundedButton("Login", 20);
        loginButton.addActionListener(this::handleLogin);
        buttonPanel.add(loginButton);

        JButton registerButton = new RoundedButton("Register", 20);
        registerButton.addActionListener(this::handleRegister);
        buttonPanel.add(registerButton);


        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        formPanel.add(buttonPanel, constraints);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JToggleButton createRoundedToggleButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        // Default background grey, text white
        btn.setBackground(new Color(128, 128, 128)); // grey background default
        btn.setForeground(Color.WHITE);              // white text default
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setContentAreaFilled(false);
        btn.setUI(new RoundedToggleButtonUI());

        // MouseListener for double-click deselect
        btn.addMouseListener(new MouseAdapter() {
            private long lastClickTime = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
                long clickTime = System.currentTimeMillis();
                if (btn.isSelected() && (clickTime - lastClickTime) < 400) {
                    btn.setSelected(false);
                    roleGroup.clearSelection();
                }
                lastClickTime = clickTime;
            }
        });

        btn.addChangeListener(e -> updateButtonColors(btn));
        updateButtonColors(btn);
        return btn;
    }

    private void updateButtonColors(JToggleButton btn) {
        if (btn.isSelected()) {
            // Selected: blue background, white text
            btn.setBackground(new Color(0, 51, 102)); // blue
            btn.setForeground(Color.WHITE);
        } else {
            // Not selected: grey background, white text
            btn.setBackground(new Color(169, 169, 169)); // grey
            btn.setForeground(Color.WHITE);
        }
        btn.repaint();
    }


    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        Role role = getSelectedRole();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        if (role == null) {
            JOptionPane.showMessageDialog(this, "Please select a role before logging in.");
            return;
        }

        User user = UserManager.authenticate(username, password, role);
        if (user != null) {
            Account account = accountManager.getAccountForUser(user);
            if (account == null) {
                JOptionPane.showMessageDialog(this, "Account not found!");
                return;
            }

            switch (role) {
                case CUSTOMER -> {
                    TransactionManager transactionManager = new TransactionManager();
                    CustomerPanel customerPanel = new CustomerPanel(account, transactionManager, accountManager);
                    customerPanel.setVisible(true);
                }
                case STAFF -> {
                    StaffPanel staffPanel = new StaffPanel(account, accountManager);
                    staffPanel.setVisible(true);
                }

                case LOAN_MANAGER -> {
                    LoanManagerPanel loanManagerPanel = new LoanManagerPanel(accountManager);
                    loanManagerPanel.setVisible(true); // ✅ This is enough
                }

            }

            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }
    private void handleRegister(ActionEvent e) {
        cardLayout.show(rightPanel, "register");
    }


    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JLabel titleLabel = new JLabel("Register New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        RoundedTextField regUsernameField = new RoundedTextField(20);
        formPanel.add(regUsernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        RoundedPasswordField regPasswordField = new RoundedPasswordField(20);
        formPanel.add(regPasswordField, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy++;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        RoundedTextField regNameField = new RoundedTextField(20);
        formPanel.add(regNameField, gbc);

        // Role dropdown
        gbc.gridx = 0; gbc.gridy++;
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(labelFont);
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1;
        String[] roles = {"Customer", "Staff", "Loan Manager"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        formPanel.add(roleBox, gbc);

        // Buttons panel
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton submitButton = new RoundedButton("Submit", 20);
        submitButton.addActionListener(ae -> {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword()).trim();
            String name = regNameField.getText().trim();
            String selectedRole = (String) roleBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            Role role = switch (selectedRole) {
                case "Staff" -> Role.STAFF;
                case "Loan Manager" -> Role.LOAN_MANAGER;
                default -> Role.CUSTOMER;
            };

            boolean success = UserManager.register(username, password, role, name);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful! Awaiting staff approval.");
                cardLayout.show(rightPanel, "login");
            } else {
                JOptionPane.showMessageDialog(this, "Username already taken. Try another.");
            }
        });

        JButton backButton = new RoundedButton("Back to Login", 20);
        backButton.addActionListener(e -> cardLayout.show(rightPanel, "login"));

        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        formPanel.add(buttonPanel, gbc);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }




    private Role getSelectedRole() {
        if (customerToggleButton.isSelected()) return Role.CUSTOMER;
        if (staffToggleButton.isSelected()) return Role.STAFF;
        if (loanManagerToggleButton.isSelected()) return Role.LOAN_MANAGER;
        return null;
    }
}
