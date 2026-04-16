package net.crestbank.money.managers;

import lombok.Getter;
import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import net.crestbank.money.database.Transaction;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class BankManager {
    private final Money plugin;
    @Getter private final VaultHook vaultHook;

    private final Map<UUID, List<Long>> rateLimits = new HashMap<>();

    public BankManager(Money plugin) {
        this.plugin = plugin;
        this.vaultHook = new VaultHook(plugin);
    }

    private boolean isRateLimited(Player player) {
        long now = System.currentTimeMillis();
        rateLimits.putIfAbsent(player.getUniqueId(), new ArrayList<>());
        List<Long> times = rateLimits.get(player.getUniqueId());
        times.removeIf(t -> now - t > 60000);
        if (times.size() >= 15) { // Max 15 transactions per minute
            player.sendMessage("§c§lBANK » §7You are doing that too fast! Please slow down.");
            return true;
        }
        times.add(now);
        return false;
    }

    public Account getAccount(UUID uuid) {
        Account account = plugin.getStorageManager().getProvider().loadAccount(uuid);
        if (account == null) {
            account = new Account(uuid, "Unknown", 0.0, false, System.currentTimeMillis(), System.currentTimeMillis());
            plugin.getStorageManager().getProvider().saveAccount(account);
        }
        return account;
    }

    public boolean deposit(Player player, double amount) {
        if (amount <= 0 || plugin.isLockdown() || plugin.isMaintenance()) return false;
        if (amount > 500000) {
            player.sendMessage("§c§lBANK » §7Transaction exceeds maximum limit ($500,000).");
            return false;
        }
        if (isRateLimited(player)) return false;
        
        Account account = getAccount(player.getUniqueId());
        if (account.isFrozen()) {
            player.sendMessage("§c§lBANK » §7Your account is frozen by an administrator.");
            return false;
        }
        if (account.getBalance() + amount > 1000000000) {
            player.sendMessage("§c§lBANK » §7This deposit would exceed the maximum bank balance ($1,000,000,000).");
            return false;
        }

        if (vaultHook.getEconomy().has(player, amount)) {
            vaultHook.getEconomy().withdrawPlayer(player, amount); // Vault async transaction begin
            account.setBalance(account.getBalance() + amount);
            account.setName(player.getName());
            plugin.getStorageManager().getProvider().saveAccount(account); // Commit
            
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), player.getUniqueId(), "DEPOSIT", amount, "Pocket to Bank")
            );
            plugin.getLogManager().fileLog(player.getName() + " deposited $" + String.format("%.2f", amount));
            player.sendMessage("§a§lBANK » §fDeposited §a$" + String.format("%.2f", amount) + " §fto your account!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        } else {
            player.sendMessage("§c§lBANK » §7You do not have enough money in your wallet.");
        }
        return false;
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0 || plugin.isLockdown() || plugin.isMaintenance()) return false;
        if (amount > 500000) {
            player.sendMessage("§c§lBANK » §7Transaction exceeds maximum limit ($500,000).");
            return false;
        }
        if (isRateLimited(player)) return false;

        Account account = getAccount(player.getUniqueId());
        if (account.isFrozen()) {
            player.sendMessage("§c§lBANK » §7Your account is frozen by an administrator.");
            return false;
        }

        if (account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            plugin.getStorageManager().getProvider().saveAccount(account); // Database commit first
            vaultHook.getEconomy().depositPlayer(player, amount); // Vault complete

            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), player.getUniqueId(), "WITHDRAW", amount, "Bank to Pocket")
            );
            plugin.getLogManager().fileLog(player.getName() + " withdrew $" + String.format("%.2f", amount));
            
            player.sendMessage("§a§lBANK » §fWithdrew §a$" + String.format("%.2f", amount) + " §ffrom your account!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        } else {
            player.sendMessage("§c§lBANK » §7You do not have enough money in your bank account.");
        }
        return false;
    }

    public boolean transfer(Player from, UUID toUuid, double amount) {
        if (amount <= 0 || plugin.isLockdown() || plugin.isMaintenance()) return false;
        if (amount > 500000) {
            from.sendMessage("§c§lBANK » §7Transaction exceeds maximum limit ($500,000).");
            return false;
        }
        if (isRateLimited(from)) return false;

        Account fromAcc = getAccount(from.getUniqueId());
        Account toAcc = getAccount(toUuid);

        if (fromAcc.isFrozen()) {
            from.sendMessage("§c§lBANK » §7Your account is frozen by an administrator.");
            return false;
        }
        if (toAcc.isFrozen()) {
            from.sendMessage("§c§lBANK » §7The target player's account is frozen.");
            return false;
        }
        if (toAcc.getBalance() + amount > 1000000000) {
            from.sendMessage("§c§lBANK » §7This transfer would push the target over the maximum account capacity.");
            return false;
        }

        // START ATOMIC SQL TRANSACTION
        if (plugin.getStorageManager().getProvider().executeTransfer(from.getUniqueId(), toUuid, amount)) {
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), from.getUniqueId(), "TRANSFER_OUT", amount, "To " + toUuid)
            );
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), toUuid, "TRANSFER_IN", amount, "From " + from.getUniqueId())
            );
            
            org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(toUuid);
            String tName = target != null ? target.getName() : toUuid.toString();
            plugin.getLogManager().fileLog(from.getName() + " -> " + tName + " $" + String.format("%.2f", amount) + " (TRANSFER)");
            
            plugin.getAlertManager().triggerSuspiciousAlert(from.getName(), "TRANSFER", amount);
            
            if (target != null && target.isOnline()) {
                target.sendMessage("§a§lBANK » §fYou received a transfer of §a$" + String.format("%.2f", amount) + " §ffrom §e" + from.getName());
                target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
            return true;
        } else {
            from.sendMessage("§c§lBANK » §7Transfer failed. Ensure sufficient funds and target status.");
        }
        return false;
    }

    public boolean fine(org.bukkit.command.CommandSender admin, Account targetAcc, double amount, String reason) {
        if (amount <= 0) return false;
        
        double toCollect = amount;
        double takenFromBank = 0;
        double takenFromWallet = 0;

        // Take from Bank first
        if (targetAcc.getBalance() > 0) {
            takenFromBank = Math.min(targetAcc.getBalance(), toCollect);
            targetAcc.setBalance(targetAcc.getBalance() - takenFromBank);
            toCollect -= takenFromBank;
            plugin.getStorageManager().getProvider().saveAccount(targetAcc);
        }

        // Take from Wallet next if needed
        org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(targetAcc.getUuid());
        if (toCollect > 0 && target != null && target.isOnline()) {
            double walletBalance = vaultHook.getEconomy().getBalance(target);
            if (walletBalance > 0) {
                takenFromWallet = Math.min(walletBalance, toCollect);
                vaultHook.getEconomy().withdrawPlayer(target, takenFromWallet);
                toCollect -= takenFromWallet;
            }
        }

        double totalTaken = takenFromBank + takenFromWallet;
        
        if (totalTaken > 0) {
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), targetAcc.getUuid(), "FINE", totalTaken, reason)
            );
            
            plugin.getLogManager().fileLog("ADMIN " + admin.getName() + " fined " + targetAcc.getName() + " Total: $" + String.format("%.2f", totalTaken) + " (Bank: $" + takenFromBank + ", Wallet: $" + takenFromWallet + ") Reason: " + reason);
            
            if (target != null && target.isOnline()) {
                target.sendMessage("");
                target.sendMessage("§8§m--------------------------------");
                target.sendMessage("§c§lYOU HAVE BEEN FINED!");
                target.sendMessage("");
                target.sendMessage("§7Amount Deducted: §c-$" + String.format("%.2f", totalTaken));
                target.sendMessage("§7Reason: §f" + reason);
                target.sendMessage("§8§m--------------------------------");
                
                // Action Bar Alert
                target.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent("§c§lFINED: §f-$" + String.format("%.2f", totalTaken)));
                
                target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            
            admin.sendMessage("§a§lBANK » §fSuccessfully fined §e" + targetAcc.getName() + " §c$" + String.format("%.2f", totalTaken));
            if (toCollect > 0) {
                admin.sendMessage("§e§lBANK » §7Notice: Requested fine was §f$" + String.format("%.2f", amount) + " §7but player only had §f$" + String.format("%.2f", totalTaken) + " §7total.");
            }
            return true;
        } else {
             admin.sendMessage("§c§lBANK » §7The player §f" + targetAcc.getName() + " §7has NO money in bank or wallet to pay the fine.");
             return false;
        }
    }
}
