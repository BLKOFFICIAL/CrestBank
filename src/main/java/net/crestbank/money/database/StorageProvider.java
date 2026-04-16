package net.crestbank.money.database;

import java.util.List;
import java.util.UUID;

public interface StorageProvider {
    void setup();
    void shutdown();

    // Account operations
    Account loadAccount(UUID uuid);
    void saveAccount(Account account);
    List<Account> getTopAccounts(int limit);

    // Transaction operations
    void logTransaction(Transaction transaction);
    List<Transaction> getHistory(UUID uuid, int limit);
    void pruneHistory(long olderThanTimestamp);

    // Bank Note operations
    void createNote(BankNote note);
    boolean claimNote(UUID noteId); // Returns true if note was successfully deleted (atomic check)
    BankNote getNote(UUID noteId);
    
    // Advanced Audit
    double getInterestEarned(UUID uuid);
    boolean executeTransfer(UUID from, UUID to, double amount);
    
    // Bank Stats & Fine System
    double getTotalFinesCollected();
    double getTotalMoneyInBank();
    List<Transaction> getFines(UUID uuid);
    int getTotalAccounts();
}
