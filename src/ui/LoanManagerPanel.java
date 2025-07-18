package ui;

import core.*;
import core.dao.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LoanManagerPanel extends JFrame {
    private final AccountManager accountManager;
    private List<LoanApplication> loanApplications;
    private JTable loanTable;
    private DefaultTableModel tableModel;
    private JTextField tfSearchAccountId;
    private JButton btnSearch;

    private static final Color PRIMARY_COLOR = new Color(21, 30, 47);
    private static final Color SECONDARY_COLOR = new Color(29, 40, 81);
    private static final Color BUTTON_HOVER_COLOR = new Color(38, 50, 94);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public LoanManagerPanel(AccountManager accountManager) {
        this.accountManager = accountManager;
        setTitle("Loan Manager Panel");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(PRIMARY_COLOR);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.add(createLoanTable(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(panel);
    }

    private JPanel createLoanTable() {
        String[] columns = {"Account ID", "Status", "Reason", "Application ID", "Amount", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        loanTable = new JTable(tableModel);
        loanTable.setFont(TEXT_FONT);
        loanTable.setForeground(TEXT_COLOR);
        loanTable.setBackground(new Color(33, 47, 61));
        loanTable.setRowHeight(24);
        loanTable.setSelectionBackground(new Color(60, 120, 180));
        loanTable.setGridColor(PRIMARY_COLOR);
        loanTable.setShowGrid(true);

        JTableHeader header = loanTable.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);

        // Search Panel above table
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(PRIMARY_COLOR);

        JLabel lblSearch = new JLabel("Search by Account ID:");
        lblSearch.setForeground(Color.WHITE);

        tfSearchAccountId = new JTextField(15);
        btnSearch = new JButton("Search");

        btnSearch.setBackground(SECONDARY_COLOR);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);

        btnSearch.addActionListener(e -> {
            String searchId = tfSearchAccountId.getText().trim();
            filterLoansByAccountId(searchId);
        });

        searchPanel.add(lblSearch);
        searchPanel.add(tfSearchAccountId);
        searchPanel.add(btnSearch);

        // Load all loans initially
        loadLoanApplications();

        JScrollPane scrollPane = new JScrollPane(loanTable);
        scrollPane.getViewport().setBackground(PRIMARY_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void filterLoansByAccountId(String accountId) {
        tableModel.setRowCount(0);
        try {
            loanApplications = LoanApplicationDAO.findAll();
            for (LoanApplication loan : loanApplications) {
                if (accountId.isEmpty() || loan.getAccountId().equalsIgnoreCase(accountId)) {
                    tableModel.addRow(new Object[]{
                            loan.getAccountId(),
                            loan.getStatus(),
                            loan.getReason(),
                            loan.getApplicationId(),
                            loan.getLoanAmount(),
                            loan.getApplicationDate()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load loans: " + e.getMessage());
        }
    }

    private void loadLoanApplications() {
        tableModel.setRowCount(0);
        try {
            loanApplications = LoanApplicationDAO.findAll();
            for (LoanApplication loan : loanApplications) {
                tableModel.addRow(new Object[]{
                        loan.getAccountId(),
                        loan.getStatus(),
                        loan.getReason(),
                        loan.getApplicationId(),
                        loan.getLoanAmount(),
                        loan.getApplicationDate()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load loans: " + e.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnPanel.setBackground(PRIMARY_COLOR);

        btnPanel.add(makeButton("Approve", e -> approveLoan()));
        btnPanel.add(makeButton("Reject", e -> rejectLoan()));
        btnPanel.add(makeButton("Logout", e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(accountManager);
                loginFrame.setVisible(true);
            });
        }));

        return btnPanel;
    }

    private JButton makeButton(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(SECONDARY_COLOR);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.addActionListener(al);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(SECONDARY_COLOR);
            }
        });
        return b;
    }

    private void approveLoan() {
        int row = loanTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan");
            return;
        }
        String appId = (String) tableModel.getValueAt(row, 3);
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            LoanApplication loan = LoanApplicationDAO.findById(appId);
            loan.approve();
            LoanApplicationDAO.update(loan, c);

            Account acct = accountManager.getAccountById(loan.getAccountId());
            acct.applyLoan(loan.getLoanAmount());
            AccountDAO.update(acct, c);

            Transaction tx = new Transaction(
                    "T" + UUID.randomUUID(),
                    "LEDGER",
                    acct.getAccountId(),
                    loan.getLoanAmount(),
                    LocalDateTime.now(),
                    "Loan Approved"
            );
            TransactionDAO.add(tx, c);

            c.commit();
            JOptionPane.showMessageDialog(this, "Approved");
            loadLoanApplications();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void rejectLoan() {
        int row = loanTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan");
            return;
        }
        String appId = (String) tableModel.getValueAt(row, 3);
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            LoanApplication loan = LoanApplicationDAO.findById(appId);
            loan.reject();
            LoanApplicationDAO.update(loan, c);
            c.commit();
            JOptionPane.showMessageDialog(this, "Rejected");
            loadLoanApplications();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new LoanManagerPanel(new AccountManager()).setVisible(true)
        );
    }
}
