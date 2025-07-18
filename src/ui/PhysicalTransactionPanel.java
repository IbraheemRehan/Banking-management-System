package ui;

import core.Account;
import core.AccountManager;
import core.dao.AccountDAO;

import javax.swing.*;
import java.awt.*;

public class PhysicalTransactionPanel extends JPanel {
    private final AccountManager accountManager;

    public PhysicalTransactionPanel(AccountManager accountManager) {
        this.accountManager = accountManager;
        setLayout(new GridBagLayout());
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel idLabel = createLabel("Account ID:");
        JTextField idField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        add(idLabel, gbc);
        gbc.gridx = 1;
        add(idField, gbc);

        JLabel typeLabel = createLabel("Transaction Type:");
        String[] types = {"Deposit", "Withdraw"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        gbc.gridx = 0; gbc.gridy = 1;
        add(typeLabel, gbc);
        gbc.gridx = 1;
        add(typeBox, gbc);

        JLabel amountLabel = createLabel("Amount (>100,000):");
        JTextField amountField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 2;
        add(amountLabel, gbc);
        gbc.gridx = 1;
        add(amountField, gbc);

        JButton submitBtn = new JButton("Submit");
        submitBtn.setBackground(new Color(60, 60, 60));
        submitBtn.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(submitBtn, gbc);

        JTextArea resultArea = new JTextArea(3, 25);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(new Color(45, 45, 45));
        resultArea.setForeground(Color.WHITE);
        resultArea.setBorder(BorderFactory.createTitledBorder("Result"));
        gbc.gridy = 4;
        add(resultArea, gbc);

        submitBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                resultArea.setText("Account ID cannot be empty.");
                return;
            }

            Account acct = accountManager.getAccountById(id);
            if (acct == null) {
                resultArea.setText("Invalid Account ID.");
                return;
            }

            String type = (String) typeBox.getSelectedItem();
            double amt;
            try {
                amt = Double.parseDouble(amountField.getText());
            } catch (Exception ex) {
                resultArea.setText("Invalid amount.");
                return;
            }

            if (amt <= 100000) {
                resultArea.setText("Amount must be greater than 100,000.");
                return;
            }

            try {
                if (type.equals("Deposit")) {
                    acct.deposit(amt);
                    resultArea.setText("Deposited successfully.");
                } else {
                    if (!acct.withdraw(amt)) {
                        resultArea.setText("Insufficient balance.");
                        return;
                    }
                    resultArea.setText("Withdrawn successfully.");
                }
                AccountDAO.update(acct);
            } catch (Exception ex) {
                resultArea.setText("Transaction failed: " + ex.getMessage());
            }
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }
}
