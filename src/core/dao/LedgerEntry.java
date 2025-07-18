// === File: core/LedgerEntry.java ===
package core;

import java.time.LocalDateTime;

public class LedgerEntry {
    private final String entryId;
    private final String accountId;
    private final LocalDateTime entryDate;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String description;

    public LedgerEntry(String entryId,
                       String accountId,
                       LocalDateTime entryDate,
                       String type,
                       double amount,
                       double balanceAfter,
                       String description) {
        this.entryId      = entryId;
        this.accountId    = accountId;
        this.entryDate    = entryDate;
        this.type         = type;
        this.amount       = amount;
        this.balanceAfter = balanceAfter;
        this.description  = description;
    }

    public String getEntryId()      { return entryId; }
    public String getAccountId()    { return accountId; }
    public LocalDateTime getEntryDate() { return entryDate; }
    public String getType()         { return type; }
    public double getAmount()       { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription()  { return description; }
}
