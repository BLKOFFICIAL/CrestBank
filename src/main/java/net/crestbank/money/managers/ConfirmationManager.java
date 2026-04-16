package net.crestbank.money.managers;

import net.crestbank.money.Money;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmationManager {
    private final Money plugin;
    private final Map<UUID, PendingWithdrawal> pending = new HashMap<>();

    public ConfirmationManager(Money plugin) {
        this.plugin = plugin;
    }

    public void requestWithdrawal(Player player, double amount) {
        if (amount >= 100000.0) {
            pending.put(player.getUniqueId(), new PendingWithdrawal(amount, System.currentTimeMillis()));
            player.sendMessage("§c§lBANK » §fHigh-value withdrawal detected! Type §e/bank confirm §fwithin 10s to proceed.");
        } else {
            plugin.getBankManager().withdraw(player, amount);
        }
    }

    public void confirm(Player player) {
        PendingWithdrawal pw = pending.get(player.getUniqueId());
        if (pw != null && (System.currentTimeMillis() - pw.time) < 10000) {
            plugin.getBankManager().withdraw(player, pw.amount);
            pending.remove(player.getUniqueId());
            player.sendMessage("§a§lBANK » §fWithdrawal confirmed.");
        } else {
            player.sendMessage("§c§lBANK » §7No pending withdrawals or confirmation expired.");
            pending.remove(player.getUniqueId());
        }
    }

    private static class PendingWithdrawal {
        double amount;
        long time;
        PendingWithdrawal(double a, long t) { amount = a; time = t; }
    }
}
