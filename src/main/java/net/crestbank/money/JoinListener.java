package net.crestbank.money;

import net.crestbank.money.database.Account;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final Money plugin;

    public JoinListener(Money plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Account account = plugin.getBankManager().getAccount(event.getPlayer().getUniqueId());
        if (account != null) {
            account.setName(event.getPlayer().getName());

            if (account.getLastInterestPayout() > 0) {
                // To eliminate the "CrestBank" wealth inflation (offline catch-up exploit),
                // we reset the payout timestamp on join to ensure interest is only earned 
                // during active online gameplay cycles.
                account.setLastInterestPayout(System.currentTimeMillis());
                event.getPlayer().sendMessage(" §a§lBANK » §7Welcome back! Your bank is secure.");
            }
            
            plugin.getStorageManager().getProvider().saveAccount(account);
        }

        // Update notification for OP players
        if (event.getPlayer().isOp() && plugin.getUpdateChecker().isUpdateAvailable()) {
            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage(" §b§lCRESTBANK UPDATE » §fA new update is available: §e" + plugin.getUpdateChecker().getLatestVersion());
            event.getPlayer().sendMessage(" §b§lCRESTBANK UPDATE » §fGet it at: §bhttps://modrinth.com/plugin/crestbank");
            event.getPlayer().sendMessage("");
        }
    }
}
