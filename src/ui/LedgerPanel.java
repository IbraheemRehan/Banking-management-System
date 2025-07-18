package ui;

import core.dao.LedgerDAO;
import core.LedgerEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class LedgerPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField tfSearchAccountId;
    private boolean full;
    private String initialAccountId;

    private static final Color BACKGROUND = new Color(21, 30, 47);
    private static final Color HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_BG = new Color(33, 47, 61);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SELECTION_COLOR = new Color(60, 120, 180);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public LedgerPanel(String accountId, boolean full) {
        this.full = full;
        this.initialAccountId = accountId;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        if (full) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBackground(new Color(35, 45, 65));

            JLabel lblSearch = new JLabel("Search by Account ID:");
            lblSearch.setForeground(Color.WHITE);
            tfSearchAccountId = new JTextField(20);
            JButton btnSearch = new JButton("Search");

            btnSearch.addActionListener(e -> loadLedgerEntries(tfSearchAccountId.getText().trim()));

            searchPanel.add(lblSearch);
            searchPanel.add(tfSearchAccountId);
            searchPanel.add(btnSearch);

            add(searchPanel, BorderLayout.NORTH);
        }

        model = new DefaultTableModel(
                new String[]{"Date", "Account ID", "Type", "Amount", "Balance After", "Description", "Bank Balance"}, 0
        );

        table = new JTable(model);
        table.setFont(TABLE_FONT);
        table.setRowHeight(24);
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setGridColor(BACKGROUND);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);

        loadLedgerEntries(full ? "" : (initialAccountId != null ? initialAccountId : ""));
    }

    private void loadLedgerEntries(String accountId) {
        model.setRowCount(0);

        try {
            List<LedgerEntry> entries = LedgerDAO.findAll();

            if (!full && (accountId == null || accountId.isEmpty())) {
                return;
            }

            if (accountId != null && !accountId.isEmpty()) {
                entries = entries.stream()
                        .filter(e -> e.getAccountId().equalsIgnoreCase(accountId))
                        .collect(Collectors.toList());
            }

            double bankBalance = 10_000_000.00;

            for (LedgerEntry e : entries) {
                switch (e.getType()) {
                    case "DEPOSIT" -> bankBalance += e.getAmount();
                    case "WITHDRAWAL", "LOAN_DISBURSE" -> bankBalance -= e.getAmount();
                    case "LOAN_PAYMENT" -> bankBalance += e.getAmount();
                }

                model.addRow(new Object[]{
                        e.getEntryDate(),
                        e.getAccountId(),
                        e.getType(),
                        e.getAmount(),
                        e.getBalanceAfter(),
                        e.getDescription(),
                        String.format("%.2f", bankBalance)
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading ledger: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
