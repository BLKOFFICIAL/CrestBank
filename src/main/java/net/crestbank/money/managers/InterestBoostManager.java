package net.crestbank.money.managers;

import net.crestbank.money.Money;
import org.bukkit.Bukkit;

public class InterestBoostManager {
    private final Money plugin;
    private double currentMultiplier = 1.0;
    private int remainingTicks = 0;

    public InterestBoostManager(Money plugin) {
        this.plugin = plugin;
    }

    public void startBoost(double multiplier, int durationMinutes) {
        this.currentMultiplier = multiplier;
        this.remainingTicks = durationMinutes * 60 * 20;

        Bukkit.broadcastMessage("§6§lBANK BOOST » §fA global interest boost of §b" + multiplier + "x §fis now active for §b" + durationMinutes + "m§f!");

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (remainingTicks <= 0) {
                currentMultiplier = 1.0;
                Bukkit.broadcastMessage("§6§lBANK BOOST » §7The global interest boost has ended.");
                task.cancel();
            }
            remainingTicks -= 20;
        }, 20, 20);
    }

    public double getMultiplier() {
        return currentMultiplier;
    }
}
