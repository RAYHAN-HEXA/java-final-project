<div align="center">

<br/>

<img src="https://img.shields.io/badge/-%F0%9F%94%90%20SECUREVAULT-000000?style=for-the-badge&labelColor=000000" alt="SecureVault"/>

# SecureVault Multi-Account

**A multi-user, offline desktop password manager built with Java Swing.**  
Secure · Fast · Demo-ready · Zero configuration.

<br/>

[![Java](https://img.shields.io/badge/Java-SE%208%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.java.com)
[![GUI](https://img.shields.io/badge/GUI-Java%20Swing-2E86C1?style=flat-square)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Storage](https://img.shields.io/badge/Storage-Flat%20File-27AE60?style=flat-square)](#)
[![Size](https://img.shields.io/badge/Source-230%20lines-8E44AD?style=flat-square)](#)
[![Platform](https://img.shields.io/badge/Platform-Cross--Platform-555?style=flat-square&logo=windows&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-Academic-E74C3C?style=flat-square)](#)

<br/>

[Getting Started](#-getting-started) &nbsp;•&nbsp;
[Features](#-features) &nbsp;•&nbsp;
[Architecture](#-architecture) &nbsp;•&nbsp;
[Tech Stack](#-tech-stack) &nbsp;•&nbsp;
[Roadmap](#-roadmap)

<br/>

</div>

---

## 📌 Overview

**SecureVault Multi-Account** is a fully offline, desktop-based credential management system developed in Java. It provides isolated, password-protected vaults per user with Base64-encoded storage, a polished Swing GUI, and a frictionless demo mode — making it production-quality for academic demonstration and a solid foundation for a real-world tool.

Built as part of **CSE 2110 — Object-Oriented Programming II Lab** at Northern University of Business and Technology Khulna, the project demonstrates real-world OOP principles including encapsulation, event-driven design, file I/O persistence, and CRUD operations — all within a single optimized Java class.

---

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) **8 or later**
- Any Java-compatible IDE *(IntelliJ IDEA, Eclipse, NetBeans)* — or just a terminal

### Installation & Run

```bash
# Clone or download the project, then:

# Step 1 — Compile
javac SecureVaultMultiAccount.java

# Step 2 — Run
java SecureVaultMultiAccount
```

### First Launch (Demo Mode)

The application **auto-creates a demo account** on first run. No setup needed.

| | |
|---|---|
| **Account ID** | `demo` |
| **Master Password** | `demo123` |

> Login fields are pre-filled. Click **⚡ Demo Login** → then **🗃 Load Demo Data** to populate 10 real-world vault entries in one click.

---

## ✨ Features

### Security & Access Control

| Feature | Description |
|---------|-------------|
| 🔐 Multi-Account Isolation | Each user account has its own completely separate vault |
| 🛡️ Master Password Auth | Vault access gated behind Account ID + Master Password pair |
| 🔒 Base64 Encoding | Stored credentials are encoded — not stored as plaintext |
| 👤 Account Registration | New accounts can be created via a built-in registration dialog |

### User Interface

| Feature | Description |
|---------|-------------|
| 🎨 Gradient Login Screen | Split-panel login with animated gradient branding pane |
| 🌅 Fade-In Animation | Smooth opacity transition on application launch |
| ⚡ One-Click Demo Login | Purple button pre-fills and submits login in a single action |
| 💡 On-Screen Credential Hint | Demo credentials displayed on the login panel for presenters |

### Password Vault

| Feature | Description |
|---------|-------------|
| ➕ Add Entry | Dialog with randomized auto-fill + 🎲 Refill button for new entries |
| 👁️ Show / Hide Passwords | Toggle between masked `******` and plaintext for all rows |
| 🗑️ Delete Entry | Removes selected entry safely using a temp-file swap pattern |
| 📦 Bulk Vault Loader | Loads 10 pre-defined demo entries with one click, skipping duplicates |
| 💾 Persistent Storage | All data survives restarts via `accounts.dat` and `passwords.dat` |

---

## 📁 Project Structure

```
SecureVaultProject/
│
├── SecureVaultMultiAccount.java    # Complete application source (230 lines)
├── accounts.dat                    # Auto-generated — encoded account credentials
├── passwords.dat                   # Auto-generated — per-user vault entries
└── README.md
```

> **Note:** Both `.dat` files are created automatically on first launch. Do not edit or delete them while the application is running.

---

## 🏗️ Architecture

SecureVault follows a **single-class, state-swap architecture**. Rather than multiple screens or navigation stacks, the JFrame's content pane is replaced at runtime between `loginUI()` and `dashUI()`, keeping the entire application self-contained in one class.

```
SecureVaultMultiAccount  (extends JFrame)
│
├── Data Layer
│   ├── HashMap<String, String> accounts     In-memory account store
│   ├── String[][]              VAULT        Built-in demo credential set
│   ├── accounts.dat                         File-backed account persistence
│   └── passwords.dat                        File-backed credential persistence
│
├── Application State
│   ├── currentID   : String                 Active user (null = logged out)
│   └── showPass    : boolean                Password visibility flag
│
└── UI Layer
    ├── loginUI()                            Login screen — gradient + form
    └── dashUI()                            Dashboard — JTable + action bar
```

### Method Reference

| Method | Responsibility |
|--------|---------------|
| `loginUI()` | Renders split login screen with pre-filled credentials and demo hint |
| `dashUI()` | Renders credential dashboard with table and action button bar |
| `checkLogin()` | Authenticates via HashMap O(1) lookup + Base64-decoded comparison |
| `createAccountUI()` | JOptionPane-based registration form for new accounts |
| `loadVaultData()` | Bulk-inserts VAULT entries; skips existing via `getPW()` guard |
| `addPassword()` | Opens auto-filled add dialog with randomized VAULT entry + Refill |
| `toggleShow()` | Reads and decodes/masks every row in the table on toggle |
| `deletePassword()` | Deletes selected row and rewrites `passwords.dat` atomically |
| `enc(t)` / `dec(e)` | Base64 encode and decode utility methods |
| `fade()` | Launches `javax.swing.Timer` fade-in animation at startup |

---

## ⚙️ Core Algorithms

<details>
<summary><strong>Authentication — O(1) HashMap Lookup</strong></summary>

<br/>

```java
void checkLogin() {
    String id = loginIdField.getText().trim();
    String pw = new String(masterPassField.getPassword());

    if (!accounts.containsKey(id)) {
        JOptionPane.showMessageDialog(this, "Account ID not found ❌");
        return;
    }
    if (dec(accounts.get(id)).equals(pw)) {
        currentID = id;
        dashUI();
    } else {
        JOptionPane.showMessageDialog(this, "Wrong Password ❌");
    }
}
```

</details>

<details>
<summary><strong>Bulk Loader — Idempotent Duplicate Guard</strong></summary>

<br/>

```java
void loadVaultData() {
    int added = 0;
    for (String[] row : VAULT) {
        if (getPW(currentID, row[0], row[1]).isEmpty()) {   // not already stored
            savePW(currentID, row[0], row[1], enc(row[2]));
            tableModel.addRow(new Object[]{row[0], row[1], "******"});
            added++;
        }
    }
    JOptionPane.showMessageDialog(this,
        added > 0 ? added + " demo entries loaded! ✅" : "All demo data already loaded ℹ️");
}
```

</details>

<details>
<summary><strong>Safe Delete — Atomic Temp-File Swap</strong></summary>

<br/>

```java
void delPW(String id, String s, String u) {
    File tmp = new File("temp.dat");
    try (BufferedReader br = new BufferedReader(new FileReader(PF));
         BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
        String l;
        while ((l = br.readLine()) != null) {
            String[] p = l.split("\\|");
            if (!(p[0].equals(id) && p[1].equals(s) && p[2].equals(u)))
                bw.write(l + "\n");  // write everything except deleted row
        }
    } catch (Exception ignored) {}
    new File(PF).delete();
    tmp.renameTo(new File(PF));
}
```

</details>

<details>
<summary><strong>Fade-In — Swing Timer Animation</strong></summary>

<br/>

```java
void fade() {
    setOpacity(0f);
    Timer tm = new Timer(20, e -> {
        float op = getOpacity() + 0.05f;
        if (op >= 1f) { setOpacity(1f); ((Timer) e.getSource()).stop(); }
        else setOpacity(op);
    });
    tm.start();
}
```

</details>

---

## 🧰 Tech Stack

| Category | Details |
|----------|---------|
| **Language** | Java SE (JDK 8+) |
| **GUI Framework** | Java Swing — `JFrame`, `JTable`, `JPanel`, `JScrollPane`, `GridBagLayout`, `BorderLayout`, `FlowLayout` |
| **Encoding** | `java.util.Base64` |
| **Animation** | `javax.swing.Timer` |
| **File I/O** | `java.io.BufferedReader`, `BufferedWriter`, `FileReader`, `FileWriter` |
| **Data Structures** | `java.util.HashMap` |
| **IDE** | IntelliJ IDEA / Eclipse / NetBeans |

---

## ⚠️ Limitations

This project was built for academic scope. The following are known constraints:

- **Encryption:** Base64 is obfuscation, not cryptography. Any production deployment must replace it with AES-256 via `javax.crypto`.
- **Storage:** Flat-file persistence does not support concurrent access or large datasets. A relational database (SQLite, H2) would be required at scale.
- **Session Security:** No idle timeout or auto-lock is implemented. An open session is vulnerable on unattended machines.
- **Demo Data:** The built-in VAULT array uses the developer's own usernames and should be replaced with anonymized placeholders before any public distribution.
- **Performance:** The duplicate check in `loadVaultData()` performs a full linear scan per entry — acceptable for 10 rows, not suitable for thousands.

---

## 🔮 Roadmap

Planned improvements for future versions:

- [ ] Replace Base64 with **AES-256** encryption (`javax.crypto`)
- [ ] Migrate flat files to **embedded SQLite or H2** database
- [ ] Add **password strength meter** with complexity enforcement
- [ ] Implement **session auto-lock** after a configurable idle timeout
- [ ] Add **search and filter** across large credential sets
- [ ] Support **secure vault export** to encrypted PDF or CSV
- [ ] Introduce optional **cloud sync** with end-to-end encryption

---

## 📚 References

1. Mane, V., & Raje, A. (2023). Implementation of secure Password Manager. *Journal of Information Technology and Cryptography*, 5–8. https://doi.org/10.48001/joitc.2023.115-8

2. Anisul Islam. *Java Swing Tutorial Series.* YouTube. https://www.youtube.com/@AnisulIslam

3. GeeksforGeeks. (2024). *How to build a basic Password Manager in Java.* https://www.geeksforgeeks.org/password-manager-using-java

---

<div align="center">

<br/>

**CSE 2110 — Object-Oriented Programming II Lab &nbsp;|&nbsp; Section 3D**

Department of Computer Science and Engineering  
Northern University of Business and Technology Khulna

<br/>

Developed by **MD. Rayhan** &nbsp;·&nbsp; Student ID: `11240321831` &nbsp;·&nbsp; md.rayhanx617@gmail.com

Supervised by **Shovon Mandal (SM)** — Lecturer, Dept. of CSE, NUBTK

<br/>

*Submitted: 25 February, 2026*

</div>