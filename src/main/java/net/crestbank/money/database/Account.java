package net.crestbank.money.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private UUID uuid;
    private String name;
    private double balance;
    private boolean frozen;
    private long lastInterestPayout;
    private long createdAt;
}
