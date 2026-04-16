package net.crestbank.money.database;

import net.crestbank.money.Money;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.UUID;

public class MysqlStorageProvider implements StorageProvider {
    private final Money plugin;
    private Connection connection;
    private final String host, port, database, user, pass, prefix;

    public MysqlStorageProvider(Money plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("database.mysql.host");
        this.port = plugin.getConfig().getString("database.mysql.port");
        this.database = plugin.getConfig().getString("database.mysql.database");
        this.user = plugin.getConfig().getString("database.mysql.username");
        this.pass = plugin.getConfig().getString("database.mysql.password");
        this.prefix = plugin.getConfig().getString("database.mysql.tablePrefix");
    }

    @Override
    public void setup() {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
            connection = DriverManager.getConnection(url, user, pass);
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL Connection failed! " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS " + prefix + "accounts (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), money DOUBLE, frozen TINYINT, last_interest BIGINT, created_at BIGINT)");
            s.execute("CREATE TABLE IF NOT EXISTS " + prefix + "transactions (id INT AUTO_INCREMENT PRIMARY KEY, time BIGINT, uuid VARCHAR(36), type VARCHAR(24), amount DOUBLE, details TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS " + prefix + "notes (uuid VARCHAR(36) PRIMARY KEY, value DOUBLE, creator VARCHAR(16), time BIGINT)");
            
            // Add created_at column if it doesn't exist (migration)
            try {
                s.execute("ALTER TABLE " + prefix + "accounts ADD COLUMN created_at BIGINT AFTER last_interest");
            } catch (SQLException ignored) {}
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {}
    }

    @Override
    public Account loadAccount(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + prefix + "accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long createdAt = rs.getLong("created_at");
                if (createdAt == 0) createdAt = System.currentTimeMillis();
                return new Account(uuid, rs.getString("name"), rs.getDouble("money"), rs.getBoolean("frozen"), rs.getLong("last_interest"), createdAt);
            }
        } catch (SQLException e) {}
        return null;
    }

    @Override
    public void saveAccount(Account account) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement("REPLACE INTO " + prefix + "accounts (uuid, name, money, frozen, last_interest, created_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, account.getUuid().toString());
                ps.setString(2, account.getName());
                ps.setDouble(3, account.getBalance());
                ps.setBoolean(4, account.isFrozen());
                ps.setLong(5, account.getLastInterestPayout());
                ps.setLong(6, account.getCreatedAt());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    @Override
    public List<Account> getTopAccounts(int limit) {
        List<Account> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + prefix + "accounts ORDER BY money DESC LIMIT ?")) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Account(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getDouble("money"), rs.getBoolean("frozen"), rs.getLong("last_interest"), rs.getLong("created_at")));
            }
        } catch (SQLException e) {}
        return list;
    }

    @Override
    public void logTransaction(Transaction t) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + prefix + "transactions (time, uuid, type, amount, details) VALUES (?, ?, ?, ?, ?)")) {
                ps.setLong(1, t.getTimestamp());
                ps.setString(2, t.getPlayerUuid().toString());
                ps.setString(3, t.getType());
                ps.setDouble(4, t.getAmount());
                ps.setString(5, t.getDetails());
                ps.executeUpdate();
            } catch (SQLException e) {}
        });
    }

    @Override
    public List<Transaction> getHistory(UUID uuid, int limit) {
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + prefix + "transactions WHERE uuid = ? ORDER BY time DESC LIMIT ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(rs.getLong("time"), uuid, rs.getString("type"), rs.getDouble("amount"), rs.getString("details")));
            }
        } catch (SQLException e) {}
        return list;
    }

    @Override
    public void pruneHistory(long olderThanTimestamp) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + prefix + "transactions WHERE time < ?")) {
            ps.setLong(1, olderThanTimestamp);
            ps.executeUpdate();
        } catch (SQLException e) {}
    }

    @Override
    public void createNote(BankNote n) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + prefix + "notes (uuid, value, creator, time) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, n.getNoteId().toString());
                ps.setDouble(2, n.getValue());
                ps.setString(3, n.getCreatorName());
                ps.setLong(4, n.getCreatedAt());
                ps.executeUpdate();
            } catch (SQLException e) {}
        });
    }

    @Override
    public boolean claimNote(UUID noteId) {
        // Atomic SQL Claim
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + prefix + "notes WHERE uuid = ? LIMIT 1")) {
            ps.setString(1, noteId.toString());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {}
        return false;
    }

    @Override
    public BankNote getNote(UUID noteId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + prefix + "notes WHERE uuid = ?")) {
            ps.setString(1, noteId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new BankNote(noteId, rs.getDouble("value"), rs.getString("creator"), rs.getLong("time"));
            }
        } catch (SQLException e) {}
        return null;
    }

    @Override
    public double getInterestEarned(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(amount) FROM " + prefix + "transactions WHERE uuid = ? AND type = 'INTEREST'")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {}
        return 0;
    }

    @Override
    public boolean executeTransfer(UUID from, UUID to, double amount) {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement withdraw = connection.prepareStatement("UPDATE " + prefix + "accounts SET money = money - ? WHERE uuid = ? AND money >= ?");
                 PreparedStatement deposit = connection.prepareStatement("UPDATE " + prefix + "accounts SET money = money + ? WHERE uuid = ?")) {
                
                withdraw.setDouble(1, amount);
                withdraw.setString(2, from.toString());
                withdraw.setDouble(3, amount);
                if (withdraw.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                deposit.setDouble(1, amount);
                deposit.setString(2, to.toString());
                deposit.executeUpdate();

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public double getTotalFinesCollected() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(amount) FROM " + prefix + "transactions WHERE type = 'FINE'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {}
        return 0;
    }

    @Override
    public double getTotalMoneyInBank() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(money) FROM " + prefix + "accounts")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {}
        return 0;
    }

    @Override
    public List<Transaction> getFines(UUID uuid) {
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + prefix + "transactions WHERE uuid = ? AND type = 'FINE' ORDER BY time DESC")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(rs.getLong("time"), uuid, rs.getString("type"), rs.getDouble("amount"), rs.getString("details")));
            }
        } catch (SQLException e) {}
        return list;
    }

    @Override
    public int getTotalAccounts() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + prefix + "accounts")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {}
        return 0;
    }
}
