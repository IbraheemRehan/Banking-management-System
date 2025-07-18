// === File: core/Transaction.java ===
package core;

import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String description;

    public Transaction(String transactionId,
                       String fromAccount,
                       String toAccount,
                       double amount,
                       LocalDateTime timestamp,
                       String description) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getTransactionId() { return transactionId; }
    public String getFromAccount()    { return fromAccount;    }
    public String getToAccount()      { return toAccount;      }
    public double getAmount()         { return amount;         }
    public LocalDateTime getTimestamp() { return timestamp;     }
    public String getDescription()    { return description;    }

    public double getDebitAmount()  { return fromAccount.isEmpty() ? 0 : amount; }
    public double getCreditAmount() { return toAccount.isEmpty()   ? 0 : amount; }

    @Override
    public String toString() {
        return String.format("%s | %s→%s | %.2f | %s",
                timestamp, fromAccount, toAccount, amount, description);
    }
}