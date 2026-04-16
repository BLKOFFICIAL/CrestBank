package net.crestbank.money.managers;

import net.crestbank.money.Money;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final Money plugin;
    private File logFile;

    public LogManager(Money plugin) {
        this.plugin = plugin;
        setupLogFile();
    }

    private void setupLogFile() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        File logDir = new File(dataFolder, "logs");
        if (!logDir.exists()) logDir.mkdirs();
        
        logFile = new File(logDir, "crestbank.log");
        try {
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create crestbank.log!");
        }
    }

    public void fileLog(String entry) {
        if (logFile == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                pw.println("[" + time + "] " + entry);
                
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to write to crestbank.log");
            }
        });
    }

    public void startPruningTask() {
        // Prune once a day (20 ticks * 60 sec * 60 min * 24 hours)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int hours = plugin.getConfigHandler().getPruneHistoryHours();
            long olderThan = System.currentTimeMillis() - (hours * 3600000L);
            plugin.getStorageManager().getProvider().pruneHistory(olderThan);
            plugin.getLogger().info("Aged transaction history pruned (older than " + hours + " hours).");
        }, 100, 24 * 60 * 60 * 20L);
    }
}
