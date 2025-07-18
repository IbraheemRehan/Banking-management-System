package ui;

import core.AccountManager;
import core.LoanApplication;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LoanTableModel extends AbstractTableModel {

    private AccountManager accountManager;
    private List<LoanApplication> loanApplications;

    // Column headers for the table
    private String[] columnNames = {"Account ID", "Loan Amount", "Application Date", "Reason"};

    public LoanTableModel(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.loanApplications = accountManager.getLoanApplications(); // Fetch loan applications from AccountManager
    }

    @Override
    public int getRowCount() {
        return loanApplications.size();  // Return the size of the loan applications list
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;  // Return the number of columns
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LoanApplication loan = loanApplications.get(rowIndex);
        switch (columnIndex) {
            case 0: return loan.getAccountId();  // Account ID
            case 1: return loan.getLoanAmount();  // Loan Amount
            case 2: return loan.getApplicationDate();  // Application Date
            case 3: return loan.getReason();  // Reason for the loan
            default: return null;  // Return null for invalid column index
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];  // Return the column name for the given index
    }

    // Method to refresh the loan data from AccountManager
    public void refreshData() {
        this.loanApplications = accountManager.getLoanApplications();  // Fetch updated loan applications from AccountManager
        fireTableDataChanged();  // Notify the JTable to refresh its data
    }

    // Get loan application at a specific row
    public LoanApplication getLoanApplicationAt(int rowIndex) {
        return loanApplications.get(rowIndex);  // Return the loan application at the specified row
    }

}
