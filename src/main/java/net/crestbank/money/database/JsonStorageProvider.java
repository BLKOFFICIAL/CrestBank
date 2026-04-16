package net.crestbank.money.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.crestbank.money.Money;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonStorageProvider implements StorageProvider {
    private final Money plugin;
    private final Gson gson;
    private final File accountsDir;
    private final File transactionsFile;
    private final File notesFile;
    
    // Memory Cache
    private final Map<UUID, Account> accountCache = new ConcurrentHashMap<>();
    private final List<Transaction> transactionBuffer = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, BankNote> notesCache = new ConcurrentHashMap<>();

    public JsonStorageProvider(Money plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.accountsDir = new File(plugin.getDataFolder(), "data/accounts");
        this.transactionsFile = new File(plugin.getDataFolder(), "data/transactions.json");
        this.notesFile = new File(plugin.getDataFolder(), "data/notes.json");
    }

    @Override
    public void setup() {
        if (!accountsDir.exists()) accountsDir.mkdirs();
        loadNotes();
    }

    @Override
    public void shutdown() {
        saveAllCachedData();
    }

    private void saveAllCachedData() {
        accountCache.values().forEach(this::saveAccountToFile);
        saveTransactions();
        saveNotes();
    }

    @Override
    public Account loadAccount(UUID uuid) {
        if (accountCache.containsKey(uuid)) return accountCache.get(uuid);
        
        File file = new File(accountsDir, uuid.toString() + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Account account = gson.fromJson(reader, Account.class);
                accountCache.put(uuid, account);
                return account;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void saveAccount(Account account) {
        accountCache.put(account.getUuid(), account);
        // Async save to file
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveAccountToFile(account));
    }

    private synchronized void saveAccountToFile(Account account) {
        File file = new File(accountsDir, account.getUuid().toString() + ".json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(account, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Account> getTopAccounts(int limit) {
        // In FlatFile, we might need to scan all files. For efficiency, we rely on the cache or partial scans.
        // For now, let's load all and sort (heavy operation, should be cached)
        List<Account> all = new ArrayList<>();
        File[] files = accountsDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".json")) {
                    try (Reader reader = new FileReader(f)) {
                        all.add(gson.fromJson(reader, Account.class));
                    } catch (IOException e) {}
                }
            }
        }
        all.sort((a, b) -> Double.compare(b.getBalance(), a.getBalance()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    @Override
    public void logTransaction(Transaction transaction) {
        transactionBuffer.add(transaction);
        if (transactionBuffer.size() >= 50) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveTransactions);
        }
    }

    @Override
    public List<Transaction> getHistory(UUID uuid, int limit) {
        // Load from file and buffer, filter by UUID
        return new ArrayList<>(); // Stub for brevity, implementation would involve reading transactions.json
    }

    @Override
    public void pruneHistory(long olderThanTimestamp) {
        // Implementation for pruning
    }

    @Override
    public void createNote(BankNote note) {
        notesCache.put(note.getNoteId(), note);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveNotes);
    }

    @Override
    public boolean claimNote(UUID noteId) {
        if (notesCache.containsKey(noteId)) {
            notesCache.remove(noteId);
            saveNotes(); // Atomic-ish in memory, then persist
            return true;
        }
        return false;
    }

    @Override
    public BankNote getNote(UUID noteId) {
        return notesCache.get(noteId);
    }

    private synchronized void saveTransactions() {
        // Append to transactions.json logic
    }

    private void loadNotes() {
        if (notesFile.exists()) {
            try (Reader reader = new FileReader(notesFile)) {
                BankNote[] notes = gson.fromJson(reader, BankNote[].class);
                if (notes != null) {
                    for (BankNote n : notes) notesCache.put(n.getNoteId(), n);
                }
            } catch (IOException e) {}
        }
    }

    private synchronized void saveNotes() {
        try (Writer writer = new FileWriter(notesFile) ) {
            gson.toJson(notesCache.values(), writer);
        } catch (IOException e) {}
    }

    @Override
    public double getInterestEarned(UUID uuid) {
        return 0; // Flat-file audit stats not fully implemented for performance
    }

    @Override
    public boolean executeTransfer(UUID from, UUID to, double amount) {
        Account fa = loadAccount(from);
        Account ta = loadAccount(to);
        if (fa != null && ta != null && fa.getBalance() >= amount) {
            fa.setBalance(fa.getBalance() - amount);
            ta.setBalance(ta.getBalance() + amount);
            saveAccount(fa);
            saveAccount(ta);
            return true;
        }
        return false;
    }

    @Override
    public double getTotalFinesCollected() {
        return transactionBuffer.stream().filter(t -> t.getType().equals("FINE")).mapToDouble(Transaction::getAmount).sum();
    }

    @Override
    public double getTotalMoneyInBank() {
        double total = 0;
        File[] files = accountsDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".json")) {
                    try (Reader reader = new FileReader(f)) {
                        Account acc = gson.fromJson(reader, Account.class);
                        if (acc != null) total += acc.getBalance();
                    } catch (IOException e) {}
                }
            }
        }
        return total;
    }

    @Override
    public List<Transaction> getFines(UUID uuid) {
        // Implementation for history would visit transactions.json, for now buffer
        return transactionBuffer.stream()
                .filter(t -> t.getPlayerUuid().equals(uuid) && t.getType().equals("FINE"))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public int getTotalAccounts() {
        File[] files = accountsDir.listFiles();
        return files != null ? (int) Arrays.stream(files).filter(f -> f.getName().endsWith(".json")).count() : 0;
    }
}
