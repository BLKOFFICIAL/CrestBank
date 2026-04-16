package net.crestbank.money.commands;

import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import net.crestbank.money.database.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Money plugin;

    public CommandHandler(Money plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getGuiManager().openBankGui(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "help":
                sendHelp(player);
                break;
            case "confirm":
                plugin.getConfirmationManager().confirm(player);
                break;
            case "stats":
                sendStats(player);
                break;
            case "info":
                sendBankInfo(player);
                break;
            case "history":
                sendHistory(player);
                break;
            case "fines":
                sendFines(player);
                break;
            case "top":
                sendTop(player);
                break;
            case "balance":
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return true;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return true;
                }
                player.sendMessage("§a§lBANK » §fCurrent bank balance: §a$" + String.format("%.2f", plugin.getBankManager().getAccount(player.getUniqueId()).getBalance()));
                player.sendMessage("§a§lBANK » §fCurrent wallet balance: §a$" + String.format("%.2f", plugin.getBankManager().getVaultHook().getEconomy().getBalance(player)));
                break;
            case "pay":
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return true;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return true;
                }
                if (args.length > 2) {
                    try {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        double amt = Double.parseDouble(args[2]);
                        if (plugin.getBankManager().transfer(player, target.getUniqueId(), amt)) {
                            player.sendMessage("§a§lBANK » §fSuccessfully transferred §a$" + String.format("%.2f", amt) + " §fto §e" + target.getName());
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        } else player.sendMessage("§c§lBANK » §7Transfer failed. Check balance or frozen status.");
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Invalid amount format.");
                    }
                } else {
                    player.sendMessage("§c§lBANK » §7Usage: /bank pay <player> <amount>");
                }
                break;
            case "deposit":
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return true;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return true;
                }
                if (args.length > 1) {
                    try {
                        double amt = Double.parseDouble(args[1]);
                        if (plugin.getBankManager().deposit(player, amt)) {
                            plugin.getAlertManager().triggerSuspiciousAlert(player.getName(), "DEPOSIT", amt);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Invalid amount format.");
                    }
                } else {
                    player.sendMessage("§c§lBANK » §7Usage: /bank deposit <amount>");
                }
                break;
            case "withdraw":
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return true;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return true;
                }
                if (args.length > 1) {
                    try {
                        double amt = Double.parseDouble(args[1]);
                        plugin.getConfirmationManager().requestWithdrawal(player, amt);
                        plugin.getAlertManager().triggerSuspiciousAlert(player.getName(), "WITHDRAWAL", amt);
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Invalid amount format.");
                    }
                } else {
                    player.sendMessage("§c§lBANK » §7Usage: /bank withdraw <amount>");
                }
                break;
            case "note":
                if (plugin.isMaintenance() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fServices are closed! The servers probably crashed out or we are doing some RP!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
                    player.sendTitle("§4§lMAINTENANCE", "§cServers crashed out / RP time!", 10, 60, 10);
                    return true;
                }
                if (plugin.isLockdown() && !player.hasPermission("crestbank.admin")) {
                    player.sendMessage("§c§lBANK » §fGlobal lockdown active. Transactions suspended.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                    player.sendTitle("§4§lLOCKDOWN", "§cAll transactions are frozen", 10, 60, 10);
                    return true;
                }
                if (args.length > 1) {
                    try {
                        double amt = Double.parseDouble(args[1]);
                        if (plugin.getNoteManager().createNote(player, amt)) {
                            player.sendMessage("§a§lBANK » §fCreated a bank note for §6$" + String.format("%.2f", amt));
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Invalid amount format.");
                    }
                } else {
                    player.sendMessage("§c§lBANK » §7Usage: /bank note <amount>");
                }
                break;
            // Admin Commands
            case "fine":
                if (checkAdmin(player) && args.length > 3) {
                    try {
                        OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                        double amt = Double.parseDouble(args[2]);
                        StringBuilder reason = new StringBuilder();
                        for (int i = 3; i < args.length; i++) reason.append(args[i]).append(" ");
                        
                        Account acc = plugin.getBankManager().getAccount(t.getUniqueId());
                        plugin.getBankManager().fine(player, acc, amt, reason.toString().trim());
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Usage: /bank fine <player> <amount> <reason>");
                    }
                } else if (checkAdmin(player)) {
                    player.sendMessage("§c§lBANK » §7Usage: /bank fine <player> <amount> <reason>");
                }
                break;
            case "lockdown":
                if (checkAdmin(player)) {
                    plugin.setLockdown(!plugin.isLockdown());
                    Bukkit.broadcastMessage("§c§lBANK » §fGlobal bank lockdown is now " + (plugin.isLockdown() ? "§4§lON" : "§a§lOFF"));
                }
                break;
            case "maintenance":
                if (checkAdmin(player)) {
                    plugin.setMaintenance(!plugin.isMaintenance());
                    Bukkit.broadcastMessage("§c§lBANK » §fBanking services are now in " + (plugin.isMaintenance() ? "§4§lMAINTENANCE MODE" : "§a§lNORMAL MODE"));
                }
                break;
            case "boost":
                if (checkAdmin(player)) {
                    if (args.length > 2) {
                        try {
                            double mult = Double.parseDouble(args[1]);
                            int mins = parseDuration(args[2]);
                            plugin.getBoostManager().startBoost(mult, mins);
                        } catch(Exception e) {
                            player.sendMessage("§c§lBANK » §7Invalid format. Usage: /bank boost <multiplier> <duration e.g., 5m, 1h>");
                        }
                    } else {
                        player.sendMessage("§c§lBANK » §7Usage: /bank boost <multiplier> <duration e.g., 5m, 1h>");
                    }
                }
                break;
            case "economy":
            case "dashboard":
                if (checkAdmin(player)) plugin.getGuiManager().openAdminDashboard(player);
                break;
            case "audit":
                if (checkAdmin(player) && args.length > 1) {
                    if (args[1].equalsIgnoreCase("recent")) {
                        player.sendMessage("§c§lBANK » §7Recent massive transfers and notes are directly piped to §f/plugins/CrestBank/logs/crestbank.log §7for high-performance global logging.");
                        return true;
                    }
                    OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                    sendAudit(player, t);
                } else {
                    if(checkAdmin(player)) player.sendMessage("§c§lBANK » §7Usage: /bank audit <player|recent>");
                }
                break;
            case "freeze":
                if (checkAdmin(player) && args.length > 1) {
                    OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                    Account acc = plugin.getBankManager().getAccount(t.getUniqueId());
                    acc.setFrozen(!acc.isFrozen());
                    plugin.getStorageManager().getProvider().saveAccount(acc);
                    player.sendMessage("§a§lBANK » §f" + t.getName() + "'s account is now " + (acc.isFrozen() ? "§4FROZEN" : "§aACTIVE"));
                } else {
                    if(checkAdmin(player)) player.sendMessage("§c§lBANK » §7Usage: /bank freeze <player>");
                }
                break;
            case "set":
                if (checkAdmin(player) && args.length > 2) {
                    try {
                        OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                        double amt = Double.parseDouble(args[2]);
                        Account acc = plugin.getBankManager().getAccount(t.getUniqueId());
                        acc.setBalance(amt);
                        plugin.getStorageManager().getProvider().saveAccount(acc);
                        player.sendMessage("§a§lBANK » §fAdjusted " + t.getName() + "'s balance to §a$" + amt);
                    } catch (Exception e) {
                        player.sendMessage("§c§lBANK » §7Invalid amount format.");
                    }
                } else {
                    if(checkAdmin(player)) player.sendMessage("§c§lBANK » §7Usage: /bank set <player> <amount>");
                }
                break;
            case "reload":
                if (checkAdmin(player)) {
                    plugin.reloadConfig();
                    plugin.getInterestManager().startInterestTask();
                    player.sendMessage("§a§lBANK » §fConfiguration reloaded and tasks restarted.");
                }
                break;
            default:
                player.sendMessage("§c§lBANK » §7Unknown subcommand. Use /bank help to see all commands.");
                break;
        }

        return true;
    }

    private int parseDuration(String arg) {
        arg = arg.toLowerCase();
        int val = 0;
        try {
            if (arg.endsWith("m")) {
                val = Integer.parseInt(arg.replace("m", ""));
            } else if (arg.endsWith("h")) {
                val = Integer.parseInt(arg.replace("h", "")) * 60;
            } else if (arg.endsWith("d")) {
                val = Integer.parseInt(arg.replace("d", "")) * 60 * 24;
            } else {
                val = Integer.parseInt(arg);
            }
        } catch(Exception ignored) {}
        return val == 0 ? 5 : val; // Default to 5m if error
    }

    private boolean checkAdmin(Player p) {
        if (!p.hasPermission("crestbank.admin")) {
            p.sendMessage("§c§lBANK » §7You do not have permission to use admin features.");
            return false;
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§b§lCrestBank Command Guide");
        p.sendMessage("");
        p.sendMessage("§a/bank §8- §7Open the main banking GUI");
        p.sendMessage("§a/bank deposit <amount> §8- §7Deposit funds");
        p.sendMessage("§c/bank withdraw <amount> §8- §7Withdraw funds");
        p.sendMessage("§e/bank pay <player> <amount> §8- §7Transfer to another player");
        p.sendMessage("§d/bank note <amount> §8- §7Create a physical bank note");
        p.sendMessage("§f/bank balance §8- §7Check your balances");
        p.sendMessage("§f/bank history §8- §7View your recent transactions");
        p.sendMessage("§f/bank fines §8- §7View your personal fine record");
        p.sendMessage("§f/bank top §8- §7View the richest account holders");
        p.sendMessage("§f/bank info §8- §7View global bank institutional stats");
        if (p.hasPermission("crestbank.admin")) {
            p.sendMessage("");
            p.sendMessage("§4§lAdmin Commands:");
            p.sendMessage("§c/bank fine <player> <amt> <reason> §8- §7Forcefully fine a player");
            p.sendMessage("§c/bank audit <player|recent> §8- §7Forensic account profile");
            p.sendMessage("§c/bank lockdown §8- §7Toggle emergency global freeze");
            p.sendMessage("§c/bank maintenance §8- §7Toggle maintenance mode");
            p.sendMessage("§c/bank freeze <player> §8- §7Freeze a specific account");
            p.sendMessage("§c/bank set <player> <amount> §8- §7Force correct balance");
            p.sendMessage("§c/bank boost <multiplier> <time> §8- §7Start an interest boost");
            p.sendMessage("§c/bank economy §8- §7View global economy GUI stats");
            p.sendMessage("§c/bank dashboard §8- §7View Admin GUI Dashboard");
            p.sendMessage("§c/bank reload §8- §7Reload the config.yml");
        }
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendStats(Player p) {
        Account acc = plugin.getBankManager().getAccount(p.getUniqueId());
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§b§lFINANCIAL PROFILE: §f" + p.getName());
        p.sendMessage("§7Current Balance: §a$" + String.format("%.2f", acc.getBalance()));
        p.sendMessage("§7Account State: " + (acc.isFrozen() ? "§c§lFROZEN" : "§a§lACTIVE"));
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendFines(Player p) {
        List<Transaction> fines = plugin.getStorageManager().getProvider().getFines(p.getUniqueId());
        if (fines.isEmpty()) {
            p.sendMessage("§a§lBANK » §7You have no outstanding or past fines. Your record is clean!");
            return;
        }
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§c§lPERSONAL FINE HISTORY");
        double total = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
        for (Transaction t : fines) {
            p.sendMessage("§8[" + sdf.format(new Date(t.getTimestamp())) + "] §c-$" + String.format("%.2f", t.getAmount()) + " §8» §f" + t.getDetails());
            total += t.getAmount();
        }
        p.sendMessage("");
        p.sendMessage("§7Lifetime Fines Paid: §c$" + String.format("%.2f", total));
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendBankInfo(Player p) {
        double totalMoney = plugin.getStorageManager().getProvider().getTotalMoneyInBank();
        double totalFines = plugin.getStorageManager().getProvider().getTotalFinesCollected();
        int accounts = plugin.getStorageManager().getProvider().getTotalAccounts();
        
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§b§lCENTRAL BANK OF CRESTMC §8- §7System Info");
        p.sendMessage("");
        p.sendMessage("§7Institutional Assets: §a$" + String.format("%.2f", totalMoney));
        p.sendMessage("§7Fines Collected: §c$" + String.format("%.2f", totalFines));
        p.sendMessage("§7Active Accounts: §f" + accounts);
        p.sendMessage("§7Current Status: " + (plugin.isLockdown() ? "§4§lHALTED" : "§a§lOPERATIONAL"));
        p.sendMessage("§7Vault Integrity: §aVERIFIED");
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendHistory(Player p) {
        List<Transaction> logs = plugin.getStorageManager().getProvider().getHistory(p.getUniqueId(), 15);
        if (logs.isEmpty()) {
            p.sendMessage("§c§lBANK » §7No transaction history found.");
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§6§lRECENT TRANSACTIONS §8(Last " + logs.size() + ")");
        for (Transaction t : logs) {
            String color = t.getType().contains("WITHDRAW") || t.getType().contains("TRANSFER_OUT") || t.getType().contains("FINE") ? "§c-$" : "§a+$";
            p.sendMessage("§8[" + sdf.format(new Date(t.getTimestamp())) + "] §f" + t.getType() + " §8» " + color + String.format("%.2f", t.getAmount()) + " §e(" + t.getDetails() + ")");
        }
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendTop(Player p) {
        List<Account> top = plugin.getStorageManager().getProvider().getTopAccounts(10);
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§6§lRICHEST BANK HOLDERS");
        int rank = 1;
        for (Account acc : top) {
            p.sendMessage("§e#" + rank + " §f" + acc.getName() + " §8- §a$" + String.format("%.2f", acc.getBalance()));
            rank++;
        }
        p.sendMessage("§8§m--------------------------------");
    }

    private void sendEconomyMonitor(Player p) {
        plugin.getGuiManager().openAdminDashboard(p);
    }

    private void sendAudit(Player p, OfflinePlayer t) {
        Account acc = plugin.getBankManager().getAccount(t.getUniqueId());
        List<Transaction> hist = plugin.getStorageManager().getProvider().getHistory(t.getUniqueId(), 100);
        double interestTotal = plugin.getStorageManager().getProvider().getInterestEarned(t.getUniqueId());
        
        double totalVolume = hist.stream().mapToDouble(Transaction::getAmount).sum();
        long ageDays = (System.currentTimeMillis() - acc.getCreatedAt()) / (1000 * 60 * 60 * 24);
        
        p.sendMessage("§8§m--------------------------------");
        p.sendMessage("§4§lFORENSIC AUDIT: §f" + t.getName());
        p.sendMessage("§7- UUID: §8" + t.getUniqueId());
        p.sendMessage("§7- Balance: §a$" + String.format("%.2f", acc.getBalance()));
        p.sendMessage("§7- Account Age: §f" + ageDays + " days");
        p.sendMessage("§7- Total Interest Earned: §e$" + String.format("%.2f", interestTotal));
        p.sendMessage("§7- Account Status: " + (acc.isFrozen() ? "§c§lFROZEN" : "§a§lSAFE"));
        p.sendMessage("");
        p.sendMessage("§c§lFINANCIAL SCORE:");
        p.sendMessage("§7- Tracking: §f" + hist.size() + " Recent Trans.");
        p.sendMessage("§7- Total Volume Moved: §e$" + String.format("%.2f", totalVolume));
        
        int risk = 0;
        if (hist.size() > 50) risk += 2;
        if (totalVolume > 500000) risk += 3;
        if (ageDays < 1) risk += 5; // New accounts moving money
        if (interestTotal > 100000) risk += 2;

        String riskStr = "§a§lLOW";
        if (risk > 4) riskStr = "§e§lMEDIUM";
        if (risk > 7) riskStr = "§c§lHIGH";
        if (risk > 10) riskStr = "§4§lCRITICAL (Potential Alt/Abuse)";

        p.sendMessage("§7- Risk Rating: " + riskStr);
        p.sendMessage("§8§m--------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("help", "balance", "deposit", "withdraw", "pay", "note", "history", "top", "stats", "confirm", "info", "fines"));
            if (sender.hasPermission("crestbank.admin")) {
                list.addAll(Arrays.asList("lockdown", "maintenance", "boost", "economy", "dashboard", "audit", "freeze", "set", "reload", "fine"));
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return null;
    }
}
