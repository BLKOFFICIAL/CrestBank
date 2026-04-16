package net.crestbank.money.managers;

import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class GuiManager {
    private final Money plugin;

    public GuiManager(Money plugin) {
        this.plugin = plugin;
    }

    public void openBankGui(Player player) {
        if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
            player.sendMessage("§c§lBANK » §fBanking services are currently suspended for maintenance.");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        String title = plugin.getConfigHandler().getString("gui.title");
        Inventory gui = Bukkit.createInventory(player, 45, title);

        Account account = plugin.getBankManager().getAccount(player.getUniqueId());
        double walletBal = plugin.getBankManager().getVaultHook().getEconomy().getBalance(player);

        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, "§8", List.of());
        for(int i=0; i<45; i++) gui.setItem(i, bg);

        gui.setItem(4, getPlayerHead(player, account, walletBal));

        if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
            gui.setItem(22, createItem(Material.BARRIER, "§c§lSYSTEM LOCKDOWN", List.of("§7All transactions are currently frozen.")));
        } else if (account.isFrozen()) {
            gui.setItem(22, createItem(Material.ICE, "§b§lACCOUNT FROZEN", List.of("§7Your account is frozen by an admin.", "§7Contact staff.")));
        } else {
            ItemStack dPane = createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lDEPOSIT SECTION", List.of("§7Move from Wallet to Bank"));
            gui.setItem(10, dPane); gui.setItem(11, dPane); gui.setItem(12, dPane); 

            gui.setItem(19, createItem(Material.IRON_INGOT, "§aDeposit 25%", List.of("§7Amount: §f$" + String.format("%.2f", walletBal * 0.25), "§7Wallet: §f$" + String.format("%.2f", walletBal))));
            gui.setItem(20, createItem(Material.GOLD_INGOT, "§aDeposit 50%", List.of("§7Amount: §f$" + String.format("%.2f", walletBal * 0.50), "§7Wallet: §f$" + String.format("%.2f", walletBal))));
            gui.setItem(21, createItem(Material.DIAMOND, "§aDeposit 75%", List.of("§7Amount: §f$" + String.format("%.2f", walletBal * 0.75), "§7Wallet: §f$" + String.format("%.2f", walletBal))));
            gui.setItem(30, createItem(Material.NETHERITE_INGOT, "§a§lDEPOSIT 100%", List.of("§7Amount: §f$" + String.format("%.2f", walletBal), "§7Move everything to bank!")));

            ItemStack wPane = createItem(Material.RED_STAINED_GLASS_PANE, "§c§lWITHDRAW SECTION", List.of("§7Move from Bank to Wallet"));
            gui.setItem(14, wPane); gui.setItem(15, wPane); gui.setItem(16, wPane); 
            
            gui.setItem(23, createItem(Material.IRON_NUGGET, "§cWithdraw 25%", List.of("§7Amount: §f$" + String.format("%.2f", account.getBalance() * 0.25), "§7Bank: §f$" + String.format("%.2f", account.getBalance()))));
            gui.setItem(24, createItem(Material.GOLD_NUGGET, "§cWithdraw 50%", List.of("§7Amount: §f$" + String.format("%.2f", account.getBalance() * 0.50), "§7Bank: §f$" + String.format("%.2f", account.getBalance()))));
            gui.setItem(25, createItem(Material.DIAMOND, "§cWithdraw 75%", List.of("§7Amount: §f$" + String.format("%.2f", account.getBalance() * 0.75), "§7Bank: §f$" + String.format("%.2f", account.getBalance()))));
            gui.setItem(32, createItem(Material.HOPPER, "§c§lWITHDRAW 100%", List.of("§7Amount: §f$" + String.format("%.2f", account.getBalance()), "§7Clear account to wallet.")));
        }

        gui.setItem(40, createItem(Material.BOOK, "§6Transaction History", List.of("§7View your recent activities.")));
        gui.setItem(39, createItem(Material.SUNFLOWER, "§eTop Balances", List.of("§7View richest players.")));

        player.openInventory(gui);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    private ItemStack getPlayerHead(Player player, Account account, double walletBal) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§b§l" + player.getName() + "'s Profile");
        meta.setLore(List.of(
            "§7Bank Balance: §a$" + String.format("%.2f", account.getBalance()),
            "§7Wallet Balance: §a$" + String.format("%.2f", walletBal),
            "§7Status: " + (account.isFrozen() ? "§4§lFROZEN" : "§a§lACTIVE"),
            "",
            "§eClick to refresh data."
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void openAdminDashboard(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "§4§lADMIN DASHBOARD");
        
        List<Account> all = plugin.getStorageManager().getProvider().getTopAccounts(10000);
        double totalBank = all.stream().mapToDouble(Account::getBalance).sum();
        
        gui.setItem(11, createItem(Material.EMERALD_BLOCK, "§a§lTotal Economy", List.of("§7Total Bank Money: §a$" + String.format("%.2f", totalBank), "§7Active Accounts: §f" + all.size())));
        
        gui.setItem(13, createItem(Material.DIAMOND_BLOCK, "§b§lTop 5 Richest", 
            all.stream().limit(5).map(a -> "§e" + a.getName() + " §8- §a$" + String.format("%.2f", a.getBalance())).toList()
        ));
        
        gui.setItem(15, createItem(Material.GOLD_BLOCK, "§6§lInterest Stats", List.of("§7Recent Payouts Enabled", "§7Interval: Configured")));
        
        player.openInventory(gui);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
}
