# 🏦 CrestBank: Industrial-Grade Economy & Financial Infrastructure

![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-green.svg)

**CrestBank** is the definitive financial ecosystem designed for modern Minecraft servers. Built with high-performance, security-first principles, it replaces standard command-based money management with a premium, GUI-driven institutional banking experience.

Designed for **Minecraft 1.21.1+**, CrestBank bridges the gap between simple wallet money and a fully managed banking system.

---

## 🚀 Core Systems & Features

### 🖥️ Categorized Dynamic GUI
Forget typing long, tedious commands. CrestBank features a high-fidelity inventory interface:
*   **Visual Grouping**: Clean separation between Deposit and Withdraw sections.
*   **Smart Percentages**: Interact with **25%, 50%, 75%, and 100%** of your balance.
*   **Real-time Logic**: Buttons dynamically calculate and display precise amounts based on your current liquid capital.

![Bank GUI](https://i.ibb.co/Jws2CJCc/Screenshot-2026-03-10-170242.png)

### 📈 Passive Wealth (Interest)
Players earn passive income by storing money in the bank.
*   **Asynchronous Interest**: Payouts happen in the background to ensure zero TPS impact.
*   **Global Multipliers**: Run server events with **Interest Boosts** (e.g., 2x Interest Weekend).
*   **Notifications**: Clear feedback when interest is credited to account holders.

![Interest Messages](https://i.ibb.co/vb8bJsJ/Screenshot-2026-03-10-170614.png)

### 🛡️ Unrivaled Transaction Security
*   **Atomic SQL Transfers**: Implemented with `START TRANSACTION` and `COMMIT` logic. Money is never lost or duplicated during server crashes.
*   **Anti-Dupe Note Protection**: Physical bank notes include unique identifiers to prevent duplication exploits.
*   **Forensic Logging**: Every single cent moved is tracked in `/logs/crestbank.log`.
*   **Risk Scoring**: Automated system calculates **Risk Ratings** (LOW to CRITICAL) for every account based on behavior patterns.

![Invalid Bank Note](https://i.ibb.co/393pdMTj/Screenshot-2026-03-10-170847.png)

### 🛑 Emergency Global Controls
*   **Maintenance Mode**: RP-friendly mode that halts financial interactions with immersive audio/visual feedback.
*   **Global Lockdown**: Instant emergency freeze that locks every bank account on the server simultaneously.
*   **Account Freezing**: Precisely freeze individual users for investigation.

---

## 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/bank` | `crestbank.user` | Opens the primary banking GUI. |
| `/bank pay <p> <amt>` | `crestbank.user` | Digital Wire Transfer to another player. |
| `/bank note <amt>` | `crestbank.user` | Draft a physical bank note in hand. |
| `/bank history` | `crestbank.user` | View your personal transaction ledger. |
| `/bank audit <p>` | `crestbank.admin` | View deep forensic profile & risk score. |
| `/bank lockdown` | `crestbank.admin` | Toggle emergency global economy freeze. |
| `/bank dashboard` | `crestbank.admin` | World financial overview & liquidity stats. |

---

## 🧩 PlaceholderAPI (PAPI)
**Prefix**: `%crestbank_...%`

| Placeholder | Output |
| :--- | :--- |
| `%crestbank_balance%` | Returns numerical balance (e.g., `4523.50`). |
| `%crestbank_status%` | Visual status: §a§lACTIVE or §c§lFROZEN. |
| `%crestbank_top_name_1%` | Name of the richest player (Rank 1). |

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
// Get the CrestBank instance
Money plugin = Money.getInstance();

// Retrieve a player's account
Account acc = plugin.getBankManager().getAccount(player.getUniqueId());

// Adjust balance programmatically (Atomic SQL)
acc.setBalance(acc.getBalance() + 1000.0);
plugin.getStorageManager().getProvider().saveAccount(acc);
```

---

## 🛠️ Build & Requirements
*   **JDK 17+** is required.
*   **Vault API** is mandatory for economy integration.
*   **MySQL / MariaDB** recommended for atomic ledger performance.

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
