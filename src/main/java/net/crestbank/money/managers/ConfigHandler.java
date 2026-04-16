package net.crestbank.money.managers;

import net.crestbank.money.Money;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {
    private final Money plugin;
    public ConfigHandler(Money plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getString(String path) {
        return getConfig().getString(path);
    }

    public double getDouble(String path) {
        return getConfig().getDouble(path);
    }

    public int getInt(String path) {
        return getConfig().getInt(path);
    }

    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    // Example config accessors
    public String getStorageType() { return getString("database.typeOfDatabase"); }
    public double getInterestPercentage() { return getDouble("general.interest.percentageAmount"); }
    public double getMaxBankBalance() { return getDouble("general.interest.maxBalanceLimit"); }
    public int getPruneHistoryHours() { return getInt("general.pruneHistoryHours"); }
    public boolean isUpdateCheckerEnabled() { return getBoolean("general.update-checker.enabled"); }
}
