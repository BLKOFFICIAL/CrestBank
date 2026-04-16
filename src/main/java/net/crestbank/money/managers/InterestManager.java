package net.crestbank.money.managers;

import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import net.crestbank.money.database.Transaction;
import org.bukkit.Bukkit;

import java.util.List;

public class InterestManager {
    private final Money plugin;

    public InterestManager(Money plugin) {
        this.plugin = plugin;
    }

    private org.bukkit.scheduler.BukkitTask task;

    public void startInterestTask() {
        if (task != null) task.cancel();
        int intervalMinutes = plugin.getConfigHandler().getInt("general.interest.intervalMinutes");
        if (intervalMinutes <= 0) return;
        
        int intervalTicks = intervalMinutes * 60 * 20;
        long initialDelay = 20L * 60; // 1 minute after start
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::payoutInterest, initialDelay, intervalTicks);
    }

    public void stopInterestTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void payoutInterest() {
        if (!plugin.getConfigHandler().getBoolean("general.interest.enabled")) return;
        if (plugin.isLockdown()) return;

        double basePercentage = plugin.getConfigHandler().getInterestPercentage();
        double multiplier = plugin.getBoostManager().getMultiplier();
        double finalPercentage = basePercentage * multiplier;
        
        double maxBalance = plugin.getConfigHandler().getMaxBankBalance();

        // Only pay interest to ONLINE players to prevent offline farming and excessive wealth accumulation
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            // Re-verify online status inside loop for async safety
            if (p == null || !p.isOnline()) continue;
            
            Account account = plugin.getBankManager().getAccount(p.getUniqueId());
            if (account == null || account.isFrozen() || account.getBalance() <= 0) continue;

            double eligibleBalance = Math.min(account.getBalance(), maxBalance);
            double amount = eligibleBalance * (finalPercentage / 100.0);

            if (amount > 0) {
                account.setBalance(account.getBalance() + amount);
                account.setLastInterestPayout(System.currentTimeMillis());
                plugin.getStorageManager().getProvider().saveAccount(account);

                plugin.getStorageManager().getProvider().logTransaction(
                    new Transaction(System.currentTimeMillis(), account.getUuid(), "INTEREST", amount, "Interest (Boost: " + multiplier + "x)")
                );
                
                p.sendMessage("§a§lBANK » §fYou received §a$" + String.format("%.2f", amount) + " §fin interest! §8(Boost: " + multiplier + "x)");
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            }
        }
    }
}
