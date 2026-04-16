package net.crestbank.money.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankNote {
    private UUID noteId;
    private double value;
    private String creatorName;
    private long createdAt;
}
