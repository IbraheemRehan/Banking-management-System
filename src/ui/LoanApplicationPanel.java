package ui;

import core.Account;
import core.AccountManager;
import core.LoanApplication;
import core.Transaction;
import core.dao.AccountDAO;
import core.dao.LoanApplicationDAO;
import core.dao.TransactionDAO;
import core.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanApplicationPanel extends JPanel {
    private final String accountId;
    private final AccountManager accountManager;

    private JTextField tfAmount;
    private JTextField tfReason;
    private JButton btnSubmit;

    public LoanApplicationPanel(String accountId, AccountManager accountManager) {
        this.accountId = accountId;
        this.accountManager = accountManager;

        setLayout(new GridBagLayout());
        setBackground(new Color(21, 30, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Color textColor = Color.WHITE;
        Color inputColor = new Color(33, 47, 61);
        Color btnColor = new Color(52, 73, 94);

        JLabel lblAmount = new JLabel("Loan Amount:");
        lblAmount.setForeground(textColor);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblAmount, gbc);

        tfAmount = new JTextField(22);
        tfAmount.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfAmount.setBackground(inputColor);
        tfAmount.setForeground(textColor);
        tfAmount.setCaretColor(textColor);
        tfAmount.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(tfAmount, gbc);

        JLabel lblReason = new JLabel("Reason:");
        lblReason.setForeground(textColor);
        lblReason.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblReason, gbc);

        tfReason = new JTextField(22);
        tfReason.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfReason.setBackground(inputColor);
        tfReason.setForeground(textColor);
        tfReason.setCaretColor(textColor);
        tfReason.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(tfReason, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(getBackground());

        btnSubmit = new JButton("Submit");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSubmit.setBackground(btnColor);
        btnSubmit.setForeground(textColor);
        btnSubmit.setFocusPainted(false);
        buttonPanel.add(btnSubmit);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setBackground(btnColor);
        btnCancel.setForeground(textColor);
        btnCancel.setFocusPainted(false);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        btnSubmit.addActionListener(e -> submitLoan());
        btnCancel.addActionListener(e -> clearFields());
    }

    private void submitLoan() {
        try {
            double amount = Double.parseDouble(tfAmount.getText().trim());
            String reason = tfReason.getText().trim();

            if (amount <= 0 || reason.isEmpty()) {
                throw new IllegalArgumentException("Amount must be greater than 0 and reason is required.");
            }

            if (amount > 10000) {
                throw new IllegalArgumentException("Loan amount must be less than or equal to 10,000.");
            }

            LoanApplication loan = new LoanApplication(accountId, amount, LocalDate.now(), reason);
            LoanApplicationDAO.add(loan);

            JOptionPane.showMessageDialog(this, "Loan application submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        tfAmount.setText("");
        tfReason.setText("");
    }
}