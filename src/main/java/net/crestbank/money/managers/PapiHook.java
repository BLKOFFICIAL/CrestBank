package net.crestbank.money.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import net.crestbank.money.database.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

public class PapiHook extends PlaceholderExpansion {
    private final Money plugin;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    public PapiHook(Money plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "crestbank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CrestBank";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    private String formatSmart(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fb", amount / 1_000_000_000.0);
        if (amount >= 1_000_000) return String.format("%.1fm", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fk", amount / 1_000.0);
        return String.format("%.2f", amount);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String query = params.toLowerCase();

        // Top Leaderboard: %crestbank_top_name_1%, %crestbank_top_balance_1%, %crestbank_top_formatted_1%, %crestbank_top_smart_1%
        if (query.startsWith("top_")) {
            String[] split = query.split("_");
            if (split.length >= 3) {
                String type = split[1];
                try {
                    int rankIndex = split.length - 1;
                    int rank = Integer.parseInt(split[rankIndex]);
                    if (rank < 1) return "N/A";
                    
                    List<Account> top = plugin.getStorageManager().getProvider().getTopAccounts(rank);
                    if (top.size() >= rank) {
                        Account target = top.get(rank - 1);
                        if (type.equals("name")) return target.getName();
                        if (type.equals("balance")) return String.format("%.2f", target.getBalance());
                        if (type.equals("smart")) return formatSmart(target.getBalance());
                        if (type.equals("formatted") || query.contains("balance_formatted") || query.contains("balance-formatted")) return FORMATTER.format(target.getBalance());
                    } else {
                        return "---";
                    }
                } catch (NumberFormatException e) {
                    return "Invalid Rank";
                }
            }
        }

        // Specific player balance: %crestbank_balance_<playerName>%, %crestbank_balance_formatted_<playerName>%
        if (query.startsWith("balance_")) {
            String content = params.substring(8);
            if (content.startsWith("formatted_")) {
                String targetName = content.substring(10);
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (target != null && (target.hasPlayedBefore() || target.isOnline())) {
                    Account acc = plugin.getBankManager().getAccount(target.getUniqueId());
                    return FORMATTER.format(acc.getBalance());
                }
                return "0.00";
            } else if (!content.equalsIgnoreCase("formatted") && !content.equalsIgnoreCase("smart")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(content);
                if (target != null && (target.hasPlayedBefore() || target.isOnline())) {
                    Account acc = plugin.getBankManager().getAccount(target.getUniqueId());
                    return String.format("%.2f", acc.getBalance());
                }
                return "0.00";
            }
        }

        // Viewer based placeholders
        if (player == null) return "";
        Account account = plugin.getBankManager().getAccount(player.getUniqueId());
        
        switch (query) {
            case "balance":
                return String.format("%.2f", account.getBalance());
            case "balance_formatted":
            case "balance-formatted":
                return FORMATTER.format(account.getBalance());
            case "balance_smart":
                return formatSmart(account.getBalance());
            case "interest_rate":
                return String.format("%.2f%%", plugin.getConfigHandler().getInterestPercentage() * 100);
            case "total_fines":
                return FORMATTER.format(plugin.getStorageManager().getProvider().getTotalFinesCollected());
            case "total_fines_smart":
                return formatSmart(plugin.getStorageManager().getProvider().getTotalFinesCollected());
            case "player_fines":
                return FORMATTER.format(plugin.getStorageManager().getProvider().getFines(player.getUniqueId()).stream().mapToDouble(Transaction::getAmount).sum());
            case "player_fines_smart":
                return formatSmart(plugin.getStorageManager().getProvider().getFines(player.getUniqueId()).stream().mapToDouble(Transaction::getAmount).sum());
            case "frozen":
                return account.isFrozen() ? "Yes" : "No";
            case "status":
                return account.isFrozen() ? "§c§lFROZEN" : "§a§lACTIVE";
            case "name":
                return account.getName();
        }

        return null;
    }
}
