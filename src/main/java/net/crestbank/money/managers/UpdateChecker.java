package net.crestbank.money.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.crestbank.money.Money;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private final Money plugin;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;

    public UpdateChecker(Money plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdates() {
        if (!plugin.getConfigHandler().isUpdateCheckerEnabled()) {
            return;
        }

        String projectSlug = "crestbank";

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "CrestBank Update Checker (" + currentVersion + ")");

                if (connection.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
                        if (versions.size() > 0) {
                            latestVersion = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                            
                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                                updateAvailable = true;
                                Bukkit.getConsoleSender().sendMessage("§b§lCrestBank » §fA new update is available: §e" + latestVersion);
                                Bukkit.getConsoleSender().sendMessage("§b§lCrestBank » §fDownload it at: §bhttps://modrinth.com/plugin/" + projectSlug);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
