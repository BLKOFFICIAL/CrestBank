# 🏦 CrestBank: Industrial-Grade Economy & Financial Infrastructure

![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-green.svg)

**CrestBank** is the definitive financial ecosystem designed for modern Minecraft servers. Built with high-performance, security-first principles, it replaces standard command-based money management with a premium, GUI-driven institutional banking experience.

---

## 🚀 Core Systems & Features

### 🖥️ Categorized Dynamic GUI
Forget typing long, tedious commands. CrestBank features a high-fidelity inventory interface:
*   **Visual Grouping**: Clean separation between Deposit and Withdraw sections.
*   **Smart Percentages**: Interact with **25%, 50%, 75%, and 100%** of your balance. Buttons dynamically calculate and display precise amounts in real-time.
*   **Personal Profile**: View your player head in-game with live readouts of your Bank and Wallet liquidity.

### 🛡️ Unrivaled Transaction Security
*   **Atomic SQL Transfers**: Implemented with `START TRANSACTION` and `COMMIT` logic. Money is never lost or duplicated during server crashes or database hiccups.
*   **Global Forensic Logging**: Every single cent moved is tracked in `/logs/crestbank.log` for out-of-game investigations.
*   **Financial Risk Scoring**: Automated system calculates **Risk Ratings** (LOW to CRITICAL) for every account based on age, volume, and movement patterns.

### 🛑 Emergency Global Controls
*   **Maintenance Mode**: RP-friendly "Server Crash" mode that halts all financial interactions with immersive visual/audio feedback.
*   **Global Lockdown**: Instant emergency freeze that locks every bank account on the server simultaneously.
*   **Account Freezing**: Precisely freeze individual users for investigation without affecting others.

### 📈 Passive Wealth (Interest)
*   **Asynchronous Interest**: Payouts happen in the background to ensure zero TPS impact.
*   **Global Multipliers**: Run server events with **Interest Boosts** (e.g., 2x Interest Weekend).

---

## 📜 Commands & Permissions

### 👤 User Commands
| Command | Permission | Description |
| :--- | :--- | :--- |
| `/bank` | `crestbank.user` | Opens the primary banking GUI. |
| `/bank balance` | `crestbank.user` | Instant readout of combined balances. |
| `/bank pay <player> <amt>` | `crestbank.user` | Digital wire transfer to another holder. |
| `/bank note <amt>` | `crestbank.user` | Draft a physical bank note in hand. |
| `/bank history` | `crestbank.user` | View your 15 most recent transactions. |

### 👑 Administrator Commands
| Command | Permission | Description |
| :--- | :--- | :--- |
| `/bank audit <player>` | `crestbank.admin` | View deep forensic profile & risk score. |
| `/bank lockdown` | `crestbank.admin` | Toggle emergency global economy freeze. |
| `/bank maintenance` | `crestbank.admin` | Toggle maintenance mode for repairs/RP. |
| `/bank boost <x> <time>` | `crestbank.admin` | Inject a global interest multiplier. |
| `/bank reload` | `crestbank.admin` | Reloads the `config.yml` file. |

---

## 🧩 PlaceholderAPI (PAPI)
**Prefix**: `%crestbank_...%`

| Placeholder | Output |
| :--- | :--- |
| `%crestbank_balance%` | Returns numerical balance (e.g., `4523.50`). |
| `%crestbank_status%` | Visual status: §a§lACTIVE or §c§lFROZEN. |
| `%crestbank_top_name_1%` | Name of the richest player (Rank 1). |
| `%crestbank_top_balance_1%` | Balance of the richest player (Rank 1). |

---

## 👨‍💻 Developer API

CrestBank is built for extensibility. Developers can hook into the system via the `BankManager`.

### 📦 Maven Dependency
```xml
<dependency>
    <groupId>net.crestbank</groupId>
    <artifactId>CrestBank</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 💻 Implementation Example
```java
// Import the core classes
import net.crestbank.money.Money;
import net.crestbank.money.database.Account;
import net.crestbank.money.database.Transaction;

// Get the CrestBank instance
Money plugin = Money.getInstance();

// Retrieve a player's account
Account acc = plugin.getBankManager().getAccount(player.getUniqueId());

// Adjust balance programmatically (Atomic)
acc.setBalance(acc.getBalance() + 1000.0);
plugin.getStorageManager().getProvider().saveAccount(acc);

// Log a custom transaction for forensic tracking
Transaction t = new Transaction(System.currentTimeMillis(), uuid, "CUSTOM", 500.0, "API injection");
plugin.getStorageManager().getProvider().logTransaction(t);
```

---

## 🛠️ Build & Installation
1.  **JDK 17+** is required.
2.  Clone the repository and run `mvn clean package`.
3.  Place the generated `CrestBank.jar` in your `/plugins/` folder.
4.  Ensure **Vault** is installed on your server.

---

---

## 🌟 Support
Designed by **CrestMC**. Visit our website at [www.crestmc.xyz](https://www.crestmc.xyz).

---

## ⚖️ Attribution

If you use or modify this project, you must provide proper credit to the original project: **CrestBank**.

*   **Copyright**: Keep all original copyright notices in the source code.
*   **Mention**: Explicitly mention "Based on CrestBank" prominently in your project documentation.
*   **Link**: Provide a functional link back to the original repository.

*Forks are encouraged, but do not claim this project as your own.*
