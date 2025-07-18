package ui;

import core.Account;
import core.AccountManager;
import core.Transaction;
import core.TransactionManager;
import core.dao.AccountDAO;
import core.dao.TransactionDAO;
import core.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDateTime;

public class CustomerPanel extends JFrame {
    private final Account account;
    private final TransactionManager transactionManager;
    private final AccountManager accountManager;

    private final JLabel accountInfoLabel;
    private final JLabel loanInfoLabel;

    private final JPanel contentPanel; // Right-side panel for dynamic content

    public CustomerPanel(Account account, TransactionManager transactionManager, AccountManager accountManager) {
        this.account = account;
        this.transactionManager = transactionManager;
        this.accountManager = accountManager;

        setTitle("Customer Panel - " + account.getUser().getName());
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(new Color(21, 30, 47));
        accountInfoLabel = new JLabel();
        loanInfoLabel = new JLabel();
        accountInfoLabel.setForeground(Color.WHITE);
        loanInfoLabel.setForeground(Color.WHITE);
        infoPanel.add(accountInfoLabel);
        infoPanel.add(loanInfoLabel);
        add(infoPanel, BorderLayout.NORTH);
        updateAccountInfo();

        // Split layout for buttons and content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);
        splitPane.setBackground(new Color(21, 30, 47));
        add(splitPane, BorderLayout.CENTER);

        // Left button panel
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        buttonPanel.setBackground(new Color(21, 30, 47));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));



        buttonPanel.add(makeRoundedBtn("Deposit", () -> showPanel(new DepositPanel())));
        buttonPanel.add(makeRoundedBtn("Withdraw", () -> showPanel(new WithdrawPanel())));
        buttonPanel.add(makeRoundedBtn("Transfer", () -> showPanel(new TransferPanel())));
        buttonPanel.add(makeRoundedBtn("Request Loan", () -> showPanel(new LoanApplicationPanel(account.getAccountId(), accountManager))));
        buttonPanel.add(makeRoundedBtn("Pay Loan", () -> showPanel(new PayLoanPanel())));
        buttonPanel.add(makeRoundedBtn("History", () -> showPanel(new LedgerPanel(account.getAccountId(), false))));
        buttonPanel.add(makeRoundedBtn("Logout", this::handleLogout));

        splitPane.setLeftComponent(buttonPanel);

        // Right content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(32, 40, 60));
        splitPane.setRightComponent(contentPanel);

        setVisible(true);
    }

    private JButton makeRoundedBtn(String text, Runnable action) {
        RoundedButton btn = new RoundedButton(text, 16);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void updateAccountInfo() {
        accountInfoLabel.setText("ID: " + account.getAccountId() + "   Balance: " + String.format("%.2f", account.getBalance()));
        loanInfoLabel.setText("Loan: " + String.format("%.2f", account.getLoanAmount()));
    }

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void handleLogout() {
        dispose();
        new LoginFrame(accountManager).setVisible(true);
    }

    // ----- Inner Panel Classes -----

    class DepositPanel extends JPanel {
        public DepositPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(32, 40, 60));

            // Top Heading
            JLabel heading = new JLabel("Deposit Funds", SwingConstants.CENTER);
            heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
            heading.setForeground(Color.WHITE);
            heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(heading, BorderLayout.NORTH);

            // Center Wrapper for Input + Button
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setBackground(getBackground());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 10, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;

            // Form Layout
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(getBackground());

            JLabel info = new JLabel("<html><div style='text-align: center;'>Please enter the amount you'd like to deposit.<br>Make sure the value is valid and positive.</div></html>");
            info.setFont(new Font("Segoe UI", Font.BOLD, 14));
            info.setForeground(Color.LIGHT_GRAY);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(info);

            formPanel.add(Box.createVerticalStrut(20));

            // Input
            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            inputPanel.setBackground(getBackground());
            JLabel lblAmount = new JLabel("Amount:");
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblAmount.setForeground(Color.WHITE);
            JTextField amountField = new JTextField(15);
            amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(lblAmount);
            inputPanel.add(amountField);
            formPanel.add(inputPanel);

            formPanel.add(Box.createVerticalStrut(20));

            // Button
            JButton depositBtn = new JButton("Confirm Deposit");
            depositBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            depositBtn.setBackground(new Color(0, 102, 204));
            depositBtn.setForeground(Color.WHITE);
            depositBtn.setFocusPainted(false);
            depositBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            depositBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(depositBtn);

            centerWrapper.add(formPanel, gbc);
            add(centerWrapper, BorderLayout.CENTER);

            // Action
            depositBtn.addActionListener(e -> {
                try {
                    double amt = Double.parseDouble(amountField.getText().trim());
                    if (amt <= 0) throw new IllegalArgumentException("Amount must be positive");
                    account.deposit(amt);
                    try (Connection c = DBConnection.getConnection()) {
                        AccountDAO.update(account, c);
                    }
                    TransactionDAO.add(new Transaction("T" + System.currentTimeMillis(), "LEDGER", account.getAccountId(), amt, LocalDateTime.now(), "DEPOSIT"));
                    updateAccountInfo();
                    JOptionPane.showMessageDialog(this, "Deposited successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
        }
    }




    class WithdrawPanel extends JPanel {
        public WithdrawPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(32, 40, 60));

            // Top Heading
            JLabel heading = new JLabel("Withdraw Funds", SwingConstants.CENTER);
            heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
            heading.setForeground(Color.WHITE);
            heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(heading, BorderLayout.NORTH);

            // Center Wrapper for Input + Button
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setBackground(getBackground());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 10, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;

            // Form Layout
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(getBackground());

            JLabel info = new JLabel("<html><div style='text-align: center;'>Enter the amount you want to withdraw.<br>Only available funds can be withdrawn.</div></html>");
            info.setFont(new Font("Segoe UI", Font.BOLD, 14));
            info.setForeground(Color.LIGHT_GRAY);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(info);

            formPanel.add(Box.createVerticalStrut(20));

            // Input
            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            inputPanel.setBackground(getBackground());
            JLabel lblAmount = new JLabel("Amount:");
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblAmount.setForeground(Color.WHITE);
            JTextField amountField = new JTextField(15);
            amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(lblAmount);
            inputPanel.add(amountField);
            formPanel.add(inputPanel);

            formPanel.add(Box.createVerticalStrut(20));

            // Button
            JButton withdrawBtn = new JButton("Confirm Withdraw");
            withdrawBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            withdrawBtn.setBackground(new Color(0, 102, 204));
            withdrawBtn.setForeground(Color.WHITE);
            withdrawBtn.setFocusPainted(false);
            withdrawBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            withdrawBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(withdrawBtn);

            centerWrapper.add(formPanel, gbc);
            add(centerWrapper, BorderLayout.CENTER);

            // Action
            withdrawBtn.addActionListener(e -> {
                try {
                    double amt = Double.parseDouble(amountField.getText().trim());
                    if (!account.withdraw(amt)) throw new IllegalArgumentException("Insufficient funds");
                    try (Connection c = DBConnection.getConnection()) {
                        AccountDAO.update(account, c);
                    }
                    TransactionDAO.add(new Transaction("T" + System.currentTimeMillis(), account.getAccountId(), "LEDGER", amt, LocalDateTime.now(), "WITHDRAWAL"));
                    updateAccountInfo();
                    JOptionPane.showMessageDialog(this, "Withdrawn successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
        }
    }



    public class TransferPanel extends JPanel {
        public TransferPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(21, 30, 47));  // Darker modern tone

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            Color textColor = Color.WHITE;
            Color inputColor = new Color(33, 47, 61);
            Color buttonColor = new Color(52, 73, 94);

            // Label: To Account
            JLabel lblTo = new JLabel("To Account ID:");
            lblTo.setForeground(textColor);
            lblTo.setFont(new Font("Segoe UI", Font.BOLD, 15));
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(lblTo, gbc);

            JTextField toField = new JTextField(22);
            toField.setBackground(inputColor);
            toField.setForeground(textColor);
            toField.setCaretColor(textColor);
            toField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            toField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(toField, gbc);

            // Label: Amount
            JLabel lblAmount = new JLabel("Amount:");
            lblAmount.setForeground(textColor);
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(lblAmount, gbc);

            JTextField amountField = new JTextField(22);
            amountField.setBackground(inputColor);
            amountField.setForeground(textColor);
            amountField.setCaretColor(textColor);
            amountField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            amountField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            gbc.gridx = 1;
            gbc.gridy = 1;
            add(amountField, gbc);

            // Transfer Button
            JButton transferBtn = new JButton("Transfer");
            transferBtn.setBackground(buttonColor);
            transferBtn.setForeground(textColor);
            transferBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            transferBtn.setFocusPainted(false);
            transferBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(transferBtn, gbc);

            // ActionListener (logic not modified, assumed provided elsewhere)
        }
    }


    class PayLoanPanel extends JPanel {
        public PayLoanPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(32, 40, 60));

            // Top Title
            JLabel heading = new JLabel("Loan Payment", SwingConstants.CENTER);
            heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
            heading.setForeground(Color.WHITE);
            heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(heading, BorderLayout.NORTH);

            // Center Wrapper Panel for Centered Layout
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setBackground(getBackground());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 10, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;

            // Form Panel (Vertically stacked)
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(getBackground());

            // Info Label
            JLabel info = new JLabel("<html><div style='text-align: center;'>Enter the amount you want to pay toward your loan.<br>Ensure you have enough balance.</div></html>", SwingConstants.CENTER);
            info.setFont(new Font("Segoe UI", Font.BOLD, 14));
            info.setForeground(Color.LIGHT_GRAY);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            formPanel.add(info);

            formPanel.add(Box.createVerticalStrut(20));

            // Amount Input
            JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            amountPanel.setBackground(getBackground());

            JLabel lblAmount = new JLabel("Amount:");
            lblAmount.setForeground(Color.WHITE);
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JTextField amountField = new JTextField(15);
            amountField.setFont(new Font("Segoe UI", Font.BOLD, 16));

            amountPanel.add(lblAmount);
            amountPanel.add(amountField);
            formPanel.add(amountPanel);

            formPanel.add(Box.createVerticalStrut(25));

            // Styled Button
            JButton payBtn = new JButton("Pay Loan");
            payBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            payBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            payBtn.setBackground(new Color(0, 102, 204)); // Deep blue
            payBtn.setForeground(Color.WHITE);
            payBtn.setFocusPainted(false);
            payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            formPanel.add(payBtn);

            // Add to center wrapper
            centerWrapper.add(formPanel, gbc);
            add(centerWrapper, BorderLayout.CENTER);

            // Action Listener
            payBtn.addActionListener(e -> {
                try {
                    double amt = Double.parseDouble(amountField.getText().trim());
                    account.payLoan(amt);
                    try (Connection c = DBConnection.getConnection()) {
                        AccountDAO.update(account, c);
                    }
                    TransactionDAO.add(new Transaction("T" + System.currentTimeMillis(), account.getAccountId(), "LEDGER", amt, LocalDateTime.now(), "LOAN_PAYMENT"));
                    updateAccountInfo();
                    JOptionPane.showMessageDialog(this, "Loan payment successful.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
        }
    }



}
