package net.crestbank.money;

import net.crestbank.money.database.Account;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {
    private final Money plugin;

    public SignListener(Money plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[Bank]")) {
            if (!event.getPlayer().hasPermission("crestbank.admin")) {
                event.getPlayer().sendMessage("§cYou don't have permission to create Bank signs!");
                event.setCancelled(true);
                return;
            }
            event.setLine(0, "§b§l[Bank]");
            String type = event.getLine(1).toUpperCase();
            if (type.equals("BALANCE")) {
                event.setLine(1, "§aBalance");
            } else if (type.equals("DEPOSIT")) {
                event.setLine(1, "§eDeposit");
            } else if (type.equals("WITHDRAW")) {
                event.setLine(1, "§cWithdraw");
            }
        }
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            if (sign.getLine(0).equals("§b§l[Bank]")) {
                Player player = event.getPlayer();
                String type = sign.getLine(1);

                if (type.contains("Balance")) {
                    Account acc = plugin.getBankManager().getAccount(player.getUniqueId());
                    player.sendMessage("§a§lBank » §fYour balance: §a$" + acc.getBalance());
                } else if (type.contains("Deposit") || type.contains("Withdraw")) {
                    plugin.getGuiManager().openBankGui(player);
                }
            }
        }
    }
}
