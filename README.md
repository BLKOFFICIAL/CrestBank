<div align="center">
  <img src="https://i.ibb.co/Jws2CJCc/Screenshot-2026-03-10-170242.png" width="128" height="128" style="border-radius: 20%">
  <h1>🏦 CrestBank</h1>
  <h3>Industrial-Grade Economy & Financial Infrastructure</h3>

  <p>
    <a href="https://github.com/BLKOFFICIAL/CrestBank/releases"><img src="https://img.shields.io/github/v/release/BLKOFFICIAL/CrestBank?color=blue&style=for-the-badge" alt="Release"></a>
    <a href="https://www.java.com/"><img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java" alt="Java"></a>
    <a href="https://papermc.io/"><img src="https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-green?style=for-the-badge" alt="Platform"></a>
    <a href="https://github.com/BLKOFFICIAL/CrestBank/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT_%2B_Attribution-green?style=for-the-badge" alt="License"></a>
  </p>

  <p align="center">
    <b>CrestBank</b> is the definitive institutional financial ecosystem for modern Minecraft servers. 
    <br /> Built with security-first principles to bridge the gap between pocket money and enterprise-level banking.
  </p>
</div>

---

## ✨ Premium Features

> [!TIP]
> CrestBank uses **Atomic SQL Transactions** to ensure that money is never lost or duplicated, even during catastrophic server crashes.

### 🍱 Categorized Dynamic GUI
Forget typing long, tedious commands. Access your wealth through a high-fidelity interface.
*   **Visual Grouping**: Clean separation between Deposit and Withdraw sections.
*   **Smart Percentages**: Interact with **25%, 50%, 75%, and 100%** of your balance.
*   **Dynamic Logic**: Real-time calculation of liquidity.

<div align="center">
  <img src="https://i.ibb.co/Jws2CJCc/Screenshot-2026-03-10-170242.png" width="600" alt="Bank GUI">
</div>

### 📈 Passive Wealth (Interest)
Players earn passive income by storing money in the bank.
*   **Zero TPS Impact**: All payout logic happens asynchronously.
*   **Global Boosts**: Run server events with **Interest Multipliers**.
*   **Immersive Messaging**: High-fidelity readouts for interest credits.

<div align="center">
  <img src="https://i.ibb.co/vb8bJsJ/Screenshot-2026-03-10-170614.png" width="600" alt="Interest Messages">
</div>

### 🛡️ Unrivaled Transaction Security
*   **Forensic Auditing**: Every cent moved is tracked in `/logs/crestbank.log`.
*   **Risk Scoring**: Detect alts and economy abusers with automated "Financial Risk Ratings."
*   **Anti-Dupe Notes**: Physical bank notes with unique, cryptographically signed identifiers.

<div align="center">
  <img src="https://i.ibb.co/393pdMTj/Screenshot-2026-03-10-170847.png" width="600" alt="Invalid Bank Note">
</div>

---

## 📜 Commands & Permissions

| Command | Permission | Category |
| :--- | :--- | :--- |
| `/bank` | `crestbank.user` | 🏦 Primary Banking GUI |
| `/bank pay <p> <amt>` | `crestbank.user` | 💸 Wire Transfer |
| `/bank note <amt>` | `crestbank.user` | 💵 Physical Check |
| `/bank audit <p>` | `crestbank.admin` | 🕵️ Forensic Audit |
| `/bank lockdown` | `crestbank.admin` | 🛑 Global Freeze |
| `/bank dashboard` | `crestbank.admin` | 📊 Admin Dashboard |

---

## 👨‍💻 Developer API

Hook into the central bank institution with our streamlined Java API.

### 📦 Dependency (Maven)
```xml
<dependency>
    <groupId>net.crestbank</groupId>
    <artifactId>CrestBank</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

<details>
<summary>🛠️ Implementation Example (Click to expand)</summary>

```java
// Import core classes
import net.crestbank.money.Money;
import net.crestbank.money.database.Account;

// Get the CrestBank instance
Money plugin = Money.getInstance();

// Retrieve a player's account
Account acc = plugin.getBankManager().getAccount(player.getUniqueId());

// Adjust balance programmatically (Atomic SQL)
acc.setBalance(acc.getBalance() + 1000.0);
plugin.getStorageManager().getProvider().saveAccount(acc);
```
</details>

---

## 🧩 PlaceholderAPI (PAPI)
**Prefix**: `%crestbank_...%`

<details>
<summary>📊 List of Placeholders (Click to expand)</summary>

| Placeholder | Output Description |
| :--- | :--- |
| `%crestbank_balance%` | Numerical bank capital. |
| `%crestbank_status%` | Visual status: §a§lACTIVE or §c§lFROZEN. |
| `%crestbank_top_name_1%` | Name of the richest player. |
| `%crestbank_interest_rate%` | Current interval percentage. |
</details>

---

## ⚙️ Configuration & Setup
1.  **JDK 17+** is mandatory.
2.  Install **Vault** (required) and a supported economy plugin.
3.  (Optional) Setup **MySQL/MariaDB** for high-volume atomic ledgers.

---

## ⚖️ Attribution

If you use or modify this project, you must provide proper credit to the original project: **CrestBank**.

*   **Copyright**: Keep all original copyright notices in the source code.
*   **Mention**: Explicitly mention "Based on CrestBank" prominently in your documentation.
*   **Link**: Provide a functional link back to the original repository.

*Forks are encouraged, but do not claim this project as your own.*

---

<div align="center">
  <p>Designed with ❤️ by <b>CrestMC</b></p>
  <a href="https://www.crestmc.xyz">Official Website</a> • <a href="https://github.com/BLKOFFICIAL/CrestBank/issues">Report Issues</a>
</div>
