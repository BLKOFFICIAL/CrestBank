package net.crestbank.money.database;

import net.crestbank.money.Money;

public class StorageManager {
    private final Money plugin;
    private StorageProvider provider;

    public StorageManager(Money plugin) {
        this.plugin = plugin;
    }

    public void setupStorage() {
        String type = plugin.getConfigHandler().getStorageType();
        if ("MYSQL".equalsIgnoreCase(type)) {
            // provider = new MysqlStorageProvider(plugin);
        } else {
            provider = new JsonStorageProvider(plugin);
        }
        provider.setup();
    }

    public void shutdown() {
        if (provider != null) {
            provider.shutdown();
        }
    }

    public StorageProvider getProvider() {
        return provider;
    }
}
