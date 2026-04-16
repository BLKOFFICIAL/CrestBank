package net.crestbank.money.managers;

import net.crestbank.money.Money;
import net.crestbank.money.database.BankNote;
import net.crestbank.money.database.Transaction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoteManager {
    private final Money plugin;

    public NoteManager(Money plugin) {
        this.plugin = plugin;
    }

    public boolean createNote(Player player, double amount) {
        if (amount <= 0 || plugin.isLockdown() || plugin.isMaintenance()) return false;
        
        // Check transaction limits and rate limits via BankManager logic
        if (amount > 500000) {
            player.sendMessage("§c§lBANK » §7Transaction exceeds maximum limit ($500,000).");
            return false;
        }

        net.crestbank.money.database.Account account = plugin.getBankManager().getAccount(player.getUniqueId());
        if (account.isFrozen()) {
            player.sendMessage("§c§lBANK » §7Your account is frozen by an administrator.");
            return false;
        }

        if (account.getBalance() >= amount) {
            // SUCCESS: Remove money from the BANK ACCOUNT and give the NOTE
            // We do NOT call withdraw() here because withdraw() gives money to the pocket.
            // We want the money to stay 'inside' the note.
            account.setBalance(account.getBalance() - amount);
            plugin.getStorageManager().getProvider().saveAccount(account);
            
            UUID noteId = UUID.randomUUID();
            BankNote note = new BankNote(noteId, amount, player.getName(), System.currentTimeMillis());
            plugin.getStorageManager().getProvider().createNote(note);

            ItemStack item = generateNoteItem(note);
            player.getInventory().addItem(item);
            
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), player.getUniqueId(), "NOTE_CREATE", amount, "Note ID: " + noteId)
            );
            
            plugin.getLogManager().fileLog(player.getName() + " created a bank note for $" + String.format("%.2f", amount));
            player.sendMessage("§a§lBANK » §fCreated a bank note for §6$" + String.format("%.2f", amount) + " §f(Removed from your bank!)");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        } else {
            player.sendMessage("§c§lBANK » §7You do not have enough money in your bank account.");
        }
        return false;
    }

    public boolean redeemNote(Player player, ItemStack item) {
        UUID noteId = getNoteIdFromItem(item);
        if (noteId == null) return false;

        // Fetch note data BEFORE deletion to get the value
        BankNote note = plugin.getStorageManager().getProvider().getNote(noteId);
        if (note == null) return false;
        double value = note.getValue();

        // Atomic check: Try to claim (delete) the note from database
        if (plugin.getStorageManager().getProvider().claimNote(noteId)) {
            net.crestbank.money.database.Account acc = plugin.getBankManager().getAccount(player.getUniqueId());
            acc.setBalance(acc.getBalance() + value);
            plugin.getStorageManager().getProvider().saveAccount(acc);

            item.setAmount(item.getAmount() - 1);
            
            plugin.getStorageManager().getProvider().logTransaction(
                new Transaction(System.currentTimeMillis(), player.getUniqueId(), "NOTE_REDEEM", value, "Note ID: " + noteId)
            );
            return true;
        }
        return false;
    }

    private ItemStack generateNoteItem(BankNote note) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lBank Note");
        List<String> lore = new ArrayList<>();
        lore.add("§7Value: §a$" + note.getValue());
        lore.add("§7Creator: §f" + note.getCreatorName());
        lore.add("§7ID: §8" + note.getNoteId());
        lore.add("");
        lore.add("§eRight-click to deposit into your bank!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private UUID getNoteIdFromItem(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return null;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.size() < 3) return null;
        try {
            String idStr = lore.get(2).replace("§7ID: §8", "");
            return UUID.fromString(idStr);
        } catch (Exception e) {
            return null;
        }
    }
}
