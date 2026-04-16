package net.crestbank.money;

import net.crestbank.money.managers.GuiManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final Money plugin;

    public PlayerListener(Money plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = plugin.getConfigHandler().getString("gui.title");
        String viewTitle = event.getView().getTitle();
        
        // Protection for all plugin GUIs
        if (viewTitle.equals(title) || viewTitle.equals("§4§lADMIN DASHBOARD") || viewTitle.equals("§6§lCONFIRM TRANSACTION")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (viewTitle.equals(title)) {
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    player.closeInventory();
                    return;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    player.closeInventory();
                    return;
                }

                ItemStack item = event.getCurrentItem();
                if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE || item.getType() == Material.LIME_STAINED_GLASS_PANE || item.getType() == Material.RED_STAINED_GLASS_PANE) return;

                int slot = event.getRawSlot();
                boolean action = false;
                
                double wBal = plugin.getBankManager().getVaultHook().getEconomy().getBalance(player);
                double bBal = plugin.getBankManager().getAccount(player.getUniqueId()).getBalance();
                
                switch (slot) {
                    // Deposit Slots
                    case 19: plugin.getBankManager().deposit(player, wBal * 0.25); action=true; break; // 25%
                    case 20: plugin.getBankManager().deposit(player, wBal * 0.50); action=true; break; // 50%
                    case 21: plugin.getBankManager().deposit(player, wBal * 0.75); action=true; break; // 75%
                    case 30: plugin.getBankManager().deposit(player, wBal); action=true; break; // 100%
                    
                    // Withdraw Slots
                    case 23: plugin.getBankManager().withdraw(player, bBal * 0.25); action=true; break; // 25%
                    case 24: plugin.getBankManager().withdraw(player, bBal * 0.50); action=true; break; // 50%
                    case 25: plugin.getBankManager().withdraw(player, bBal * 0.75); action=true; break; // 75%
                    case 32: plugin.getConfirmationManager().requestWithdrawal(player, bBal); action=true; break; // 100%

                    // Utility
                    case 40: 
                        player.performCommand("bank history"); 
                        player.closeInventory();
                        return;
                    case 39: 
                        player.performCommand("bank top"); 
                        player.closeInventory();
                        return;
                    case 4: 
                        plugin.getGuiManager().openBankGui(player); 
                        return;
                }
                if(action) {
                    plugin.getGuiManager().openBankGui(player); // Refresh
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.PAPER) {
                if (plugin.isMaintenance() && !event.getPlayer().hasPermission("crestbank.admin")) {
                    event.getPlayer().sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    event.getPlayer().playSound(event.getPlayer().getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    event.getPlayer().sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return;
                }
                if (plugin.isLockdown() && !event.getPlayer().hasPermission("crestbank.admin")) {
                    event.getPlayer().sendMessage("§c§lBANK » §fTransactions are locked.");
                    event.getPlayer().playSound(event.getPlayer().getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    event.getPlayer().sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return;
                }

                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Bank Note")) {
                    double noteValue = 0;
                    if(item.getItemMeta().hasLore()) {
                        for(String l : item.getItemMeta().getLore()) {
                            if(l.contains("Value: §a$")) {
                                try { noteValue = Double.parseDouble(l.split("\\$")[1]); } catch(Exception ignored) {}
                            }
                        }
                    }

                    if (plugin.getNoteManager().redeemNote(event.getPlayer(), item)) {
                        event.getPlayer().sendMessage("§a§lBANK » §fYou successfully redeemed a bank note for §a$" + String.format("%.2f", noteValue) + "§f!");
                        event.getPlayer().playSound(event.getPlayer().getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    } else {
                        event.getPlayer().sendMessage("§c§lBANK » §7This bank note is invalid or has already been redeemed.");
                    }
                }
            }
        }
    }
}
