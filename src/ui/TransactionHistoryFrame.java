package ui;

import core.Transaction;
import core.dao.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransactionHistoryFrame extends JFrame {
    public TransactionHistoryFrame(String accountId) {
        setTitle("History: " + accountId);
        setSize(600, 400);
        setLocationRelativeTo(null);

        String[] cols = {"Timestamp","From→To","Amount","Desc"};
        DefaultTableModel tm = new DefaultTableModel(cols,0);
        JTable table = new JTable(tm);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // load data
        try {
            List<Transaction> list = TransactionDAO.findByAccount(accountId);
            for (var tx : list) {
                tm.addRow(new Object[]{
                        tx.getTimestamp(),
                        tx.getFromAccount() + " → " + tx.getToAccount(),
                        tx.getAmount(),
                        tx.getDescription()
                });
            }
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Unable to load history.");
        }
    }
}
