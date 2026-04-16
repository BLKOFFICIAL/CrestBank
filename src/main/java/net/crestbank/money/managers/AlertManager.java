package net.crestbank.money.managers;

import net.crestbank.money.Money;
import org.bukkit.Bukkit;

public class AlertManager {
    private final Money plugin;

    public AlertManager(Money plugin) {
        this.plugin = plugin;
    }

    public void triggerSuspiciousAlert(String player, String type, double amount) {
        double threshold = 500000.0;
        if (amount >= threshold) {
            String message = "§4§lALERT » §fSuspicious " + type + " by §e" + player + "§f: §c$" + amount;
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("crestbank.admin"))
                .forEach(p -> p.sendMessage(message));
            plugin.getLogger().warning("[Alert] Suspicious " + type + " by " + player + ": $" + amount);
        }
    }
}
