package ui;

import core.*;
import core.dao.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.table.JTableHeader;


public class StaffPanel extends JFrame {
    private final Account account;
    private final AccountManager accountManager;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private static final String LEDGER_PASSWORD = "admin123";

    public StaffPanel(Account account, AccountManager accountManager) {
        this.account = account;
        this.accountManager = accountManager;

        setTitle("Staff Panel - " + account.getUser().getName());
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar (left)
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 10, 10));
        sidebar.setBackground(new Color(21, 30, 47));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Content panel (right)
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(32, 40, 60));

        // Split pane layout like CustomerPanel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);
        splitPane.setBackground(new Color(21, 30, 47));
        add(splitPane, BorderLayout.CENTER);

        // Header Label
        JLabel titleLabel = new JLabel("Account ID: " + account.getAccountId(), SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sidebar.add(titleLabel);

        // Add sidebar buttons
        addNavButton(sidebar, "Approve Applications", this::showPendingPanel);
        addNavButton(sidebar, "View Customers", this::showCustomerPanel);
        addNavButton(sidebar, "View Staff", this::showStaffPanel);
        addNavButton(sidebar, "View Loan Managers", this::showLoanManagerPanel);
        addNavButton(sidebar, "Physical Transactions", this::showPhysicalTransactionPanel);
        addNavButton(sidebar, "Ledger", this::showLedgerPanel);
        addNavButton(sidebar, "Logout", this::logout);


        getContentPane().setBackground(new Color(21, 30, 47));
    }
    private JButton makeSidebarButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(57, 62, 70));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16)); // ✅ Apply to the button
        // ✅ This is fine
        // Rounded corner radius

        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 173, 181));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(57, 62, 70));
            }
        });

        return btn;
    }



    private void addNavButton(JPanel panel, String text, Runnable action) {
        RoundedButton btn = new RoundedButton(text, 30); // use your custom class
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 173, 181)); // hover color
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 51, 102)); // default color
            }
        });

        panel.add(btn);
    }



    private boolean verifyPassword() {
        JPasswordField pf = new JPasswordField();
        int opt = JOptionPane.showConfirmDialog(this, pf, "Enter PIN", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            if (LEDGER_PASSWORD.equals(new String(pf.getPassword()))) {
                return true;
            }
            JOptionPane.showMessageDialog(this, "Incorrect PIN", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void showPendingPanel() {
        if (!verifyPassword()) return;

        List<User> pending = UserManager.getPendingUsers();
        if (pending.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending applications.");
            return;
        }

        String[] cols = {"Username", "Name", "Role", "Account ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (User u : pending) {
            model.addRow(new Object[]{u.getUsername(), u.getName(), u.getRole(), u.getAccountId()});
        }

        JTable table = new JTable(model);
        table.setBackground(new Color(33, 47, 61));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(44, 62, 80));
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 30, 47));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(21, 30, 47));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttons.setBackground(new Color(21, 30, 47));

        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");

        for (JButton btn : new JButton[]{approve, reject}) {
            btn.setBackground(new Color(52, 73, 94));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            buttons.add(btn);
        }

        panel.add(buttons, BorderLayout.SOUTH);

        approve.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                if (!verifyPassword()) return;
                String user = (String) model.getValueAt(row, 0);
                if (UserManager.approve(user)) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Approved " + user);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to approve.");
                }
            }
        });

        reject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String user = (String) model.getValueAt(row, 0);
                if (UserManager.reject(user)) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Rejected " + user);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reject.");
                }
            }
        });

        contentPanel.add(panel, "Pending");
        cardLayout.show(contentPanel, "Pending");
    }


    private void showCustomerPanel() {
        var custs = accountManager.getAllCustomerAccounts();
        if (custs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers found.");
            return;
        }

        String[] cols = {"Account ID", "Username", "Name", "Balance"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3; // editable: Name & Balance
            }

            @Override public void setValueAt(Object val, int row, int col) {
                super.setValueAt(val, row, col);
                String acctId = (String) getValueAt(row, 0);
                Account acct = accountManager.getAccountById(acctId);
                try {
                    if (col == 2) {
                        acct.getUser().setName(val.toString());
                        UserDAO.update(acct.getUser());
                    } else if (col == 3) {
                        double b = Double.parseDouble(val.toString());
                        acct.setBalance(b);
                        AccountDAO.update(acct);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        for (Account a : custs) {
            model.addRow(new Object[]{
                    a.getAccountId(), a.getUser().getUsername(), a.getUser().getName(), a.getBalance()
            });
        }

        // Panel setup
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(21, 30, 47));

        JLabel title = new JLabel("Customer Accounts", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setBackground(new Color(33, 47, 61));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(60, 120, 180));
        table.setGridColor(new Color(44, 62, 80));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 30, 47));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Show in panel container or fallback to dialog
        if (contentPanel != null && cardLayout != null) {
            contentPanel.add(panel, "Customers");
            cardLayout.show(contentPanel, "Customers");
        } else {
            JOptionPane.showMessageDialog(this, panel, "Customer Accounts", JOptionPane.PLAIN_MESSAGE);
        }
    }


    private void showStaffPanel() {
        if (!verifyPassword()) return;

        List<User> staffList = UserManager.getUsersByRole(Role.STAFF);
        if (staffList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No staff members found.");
            return;
        }

        String[] cols = {"Username", "Name", "Role", "Account ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 1; // Only allow editing Name
            }

            @Override public void setValueAt(Object val, int row, int col) {
                super.setValueAt(val, row, col);
                String username = (String) getValueAt(row, 0);
                User user = UserManager.getUser(username);
                try {
                    user.setName(val.toString());
                    UserDAO.update(user);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        staffList.forEach(u -> model.addRow(new Object[] {
                u.getUsername(), u.getName(), u.getRole(), u.getAccountId()
        }));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(21, 30, 47));

        JLabel title = new JLabel("Staff Accounts", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setBackground(new Color(33, 47, 61));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(60, 120, 180));
        table.setGridColor(new Color(44, 62, 80));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 30, 47));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setBackground(new Color(200, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            String username = (String) model.getValueAt(row, 0);
            User user = UserManager.getUser(username);

            try {
                AccountDAO.deleteByUsername(username);
                UserDAO.delete(username);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "DB delete failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            accountManager.removeAccount(user.getAccountId());
            UserManager.delete(username);
            model.removeRow(row);

            JOptionPane.showMessageDialog(this,
                    "Deleted staff account: " + username);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(21, 30, 47));
        bottomPanel.add(deleteBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Show panel or fallback dialog
        if (contentPanel != null && cardLayout != null) {
            contentPanel.add(panel, "Staff");
            cardLayout.show(contentPanel, "Staff");
        } else {
            JOptionPane.showMessageDialog(this, panel, "Staff Accounts", JOptionPane.PLAIN_MESSAGE);
        }
    }


    private void showLoanManagerPanel() {
        if (!verifyPassword()) return;

        List<User> loans = UserManager.getUsersByRole(Role.LOAN_MANAGER);
        if (loans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No loan managers found.");
            return;
        }

        String[] cols = {"Username", "Name", "Role", "Account ID"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 1; // Only Name is editable
            }

            @Override public void setValueAt(Object val, int row, int col) {
                super.setValueAt(val, row, col);
                String username = (String) getValueAt(row, 0);
                User u = UserManager.getUser(username);
                try {
                    u.setName(val.toString());
                    UserDAO.update(u);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Update failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        for (User u : loans) {
            model.addRow(new Object[]{
                    u.getUsername(), u.getName(), u.getRole(), u.getAccountId()
            });
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(21, 30, 47)); // Matches LedgerPanel

        JLabel title = new JLabel("Loan Manager Accounts", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setBackground(new Color(33, 47, 61)); // Matches LedgerPanel table
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(60, 120, 180));
        table.setGridColor(Color.GRAY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 30, 47)); // Same as panel background
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setBackground(new Color(200, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;

            String username = (String) model.getValueAt(row, 0);
            User u = UserManager.getUser(username);

            try {
                AccountDAO.deleteByUsername(username);
                UserDAO.delete(username);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "DB delete failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            accountManager.removeAccount(u.getAccountId());
            UserManager.delete(username);
            model.removeRow(row);

            JOptionPane.showMessageDialog(this, "Deleted loan manager: " + username);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(21, 30, 47)); // Dark background for bottom
        bottomPanel.add(deleteBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        if (contentPanel != null && cardLayout != null) {
            contentPanel.add(panel, "LoanManagers");
            cardLayout.show(contentPanel, "LoanManagers");
        } else {
            JOptionPane.showMessageDialog(this, panel, "Loan Manager Accounts", JOptionPane.PLAIN_MESSAGE);
        }
    }


    public class PhysicalTransactionPanel extends JPanel {
        public PhysicalTransactionPanel(AccountManager accountManager) {
            setLayout(new GridBagLayout());
            setBackground(new Color(21, 30, 47));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel idLabel = new JLabel("Enter Account ID:");
            idLabel.setForeground(Color.WHITE);
            idLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(idLabel, gbc);

            JTextField idField = new JTextField(20);
            idField.setBackground(new Color(33, 47, 61));
            idField.setForeground(Color.WHITE);
            idField.setCaretColor(Color.WHITE);
            idField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            idField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            gbc.gridx = 1;
            add(idField, gbc);

            JButton loadBtn = new JButton("Load Account");
            loadBtn.setBackground(new Color(52, 73, 94));
            loadBtn.setForeground(Color.WHITE);
            loadBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            loadBtn.setFocusPainted(false);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            add(loadBtn, gbc);

            JPanel actionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            actionPanel.setBackground(new Color(21, 30, 47));
            gbc.gridy = 2;
            add(actionPanel, gbc);

            JButton depositBtn = new JButton("Deposit (>100k)");
            JButton withdrawBtn = new JButton("Withdraw (>100k)");

            for (JButton btn : new JButton[]{depositBtn, withdrawBtn}) {
                btn.setBackground(new Color(52, 73, 94));
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btn.setFocusPainted(false);
                actionPanel.add(btn);
                btn.setEnabled(false); // disabled until valid account
            }

            loadBtn.addActionListener(e -> {
                String id = idField.getText().trim();
                if (id.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Account ID is empty.");
                    return;
                }

                Account acct = accountManager.getAccountById(id);
                if (acct == null) {
                    JOptionPane.showMessageDialog(this, "Invalid Account ID.");
                    return;
                }

                depositBtn.setEnabled(true);
                withdrawBtn.setEnabled(true);

                depositBtn.addActionListener(ev -> {
                    String input = JOptionPane.showInputDialog(this, "Enter deposit amount (>100k):");
                    try {
                        double amt = Double.parseDouble(input);
                        if (amt > 100000) {
                            acct.deposit(amt);
                            AccountDAO.update(acct);
                            JOptionPane.showMessageDialog(this, "Deposited successfully.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Amount must be greater than 100,000.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid number.");
                    }
                });

                withdrawBtn.addActionListener(ev -> {
                    String input = JOptionPane.showInputDialog(this, "Enter withdrawal amount (>100k):");
                    try {
                        double amt = Double.parseDouble(input);
                        if (amt > 100000 && acct.withdraw(amt)) {
                            AccountDAO.update(acct);
                            JOptionPane.showMessageDialog(this, "Withdrawn successfully.");
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    amt <= 100000 ? "Amount must be >100,000." : "Insufficient balance.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid number.");
                    }
                });
            });
        }
    }
    private void showPhysicalTransactionPanel() {
        contentPanel.removeAll();
        contentPanel.add(new PhysicalTransactionPanel(accountManager), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void showLedgerPanel() {
        if (!verifyPassword()) return;

        // Custom LedgerPanel assumed to be properly themed inside
        LedgerPanel ledgerPanel = new LedgerPanel(account.getAccountId(), true);



        if (contentPanel != null && cardLayout != null) {
            contentPanel.add(ledgerPanel, "Ledger");
            cardLayout.show(contentPanel, "Ledger");
        } else {
            // Fallback dialog
            JOptionPane.showMessageDialog(this, ledgerPanel, "Ledger", JOptionPane.PLAIN_MESSAGE);
        }
    }



    private void logout() {
        dispose();
        new LoginFrame(accountManager).setVisible(true);
    }
}
