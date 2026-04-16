package net.crestbank.money.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private long timestamp;
    private UUID playerUuid;
    private String type; // DEPOSIT, WITHDRAW, TRANSFER, INTEREST, NOTE_CREATE, NOTE_REDEEM
    private double amount;
    private String details;
}
