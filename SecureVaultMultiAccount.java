import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;

public class SecureVaultMultiAccount extends JFrame {

    // ================= THEME & FONTS =================
    Font titleFont = new Font("Segoe UI", Font.BOLD, 26);
    Font normalFont = new Font("Segoe UI", Font.PLAIN, 14);

    // ================= COMPONENTS =================
    JTextField loginIdField;
    JPasswordField masterPassField;
    JButton loginButton, createAccountButton;
    JTable passwordTable;
    DefaultTableModel tableModel;
    JButton addButton, showHideButton, deleteButton, logoutBtn;

    HashMap<String, String> accounts = new HashMap<>();
    String currentAccountID = null;
    boolean showPasswords = false;

    String accountsFile = "accounts.dat";
    String passwordsFile = "passwords.dat";

    public SecureVaultMultiAccount() {
        loadAccounts();
        initLoginUI();
        fadeInFrame();
    }

    // =================================================
    // RESPONSIVE LOGIN UI
    // =================================================
    private void initLoginUI() {
        setTitle("SecureVault - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 450));
        setSize(850, 500);
        setLocationRelativeTo(null);

        // Main Container: Split into 2 halves (Left: Info, Right: Form)
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));

        // --- LEFT PANEL (Gradient & Info) ---
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 180, 120), 0, getHeight(),
                        new Color(0, 120, 80));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcome = new JLabel("Welcome Back!", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcome.setForeground(Color.WHITE);
        leftPanel.add(welcome, gbc);

        JLabel info = new JLabel(
                "<html><center>To keep connected with us<br>please login with your info</center></html>",
                SwingConstants.CENTER);
        info.setFont(normalFont);
        info.setForeground(Color.WHITE);
        leftPanel.add(info, gbc);

        createAccountButton = new JButton("Create Account");
        styleOutlineButton(createAccountButton);
        createAccountButton.addActionListener(e -> createAccountUI());
        gbc.insets = new Insets(30, 60, 10, 60);
        leftPanel.add(createAccountButton, gbc);

        // --- RIGHT PANEL (Login Form) ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        GridBagConstraints rbc = new GridBagConstraints();
        rbc.gridx = 0;
        rbc.fill = GridBagConstraints.HORIZONTAL;
        rbc.insets = new Insets(5, 50, 5, 50);

        JLabel loginTitle = new JLabel("Login Account", SwingConstants.CENTER);
        loginTitle.setFont(titleFont);
        loginTitle.setForeground(new Color(0, 150, 100));
        rbc.insets = new Insets(0, 50, 30, 50);
        rightPanel.add(loginTitle, rbc);

        rbc.insets = new Insets(5, 50, 2, 50);
        rightPanel.add(new JLabel("Account ID"), rbc);
        loginIdField = new JTextField(15);
        loginIdField.setPreferredSize(new Dimension(0, 35));
        rightPanel.add(loginIdField, rbc);

        rightPanel.add(new JLabel("Master Password"), rbc);
        masterPassField = new JPasswordField(15);
        masterPassField.setPreferredSize(new Dimension(0, 35));
        rightPanel.add(masterPassField, rbc);

        loginButton = new JButton("SIGN IN");
        loginButton.setBackground(new Color(0, 180, 120));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(0, 40));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> checkLogin());
        rbc.insets = new Insets(20, 50, 10, 50);
        rightPanel.add(loginButton, rbc);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        setContentPane(mainContainer);
        setVisible(true);
    }

    // =================================================
    // RESPONSIVE DASHBOARD UI
    // =================================================
    private void initDashboardUI() {
        setTitle("SecureVault - Dashboard (" + currentAccountID + ")");
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel header = new JLabel("  Password Manager Dashboard - " + currentAccountID);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(33, 150, 243));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(header, BorderLayout.NORTH);

        String[] columns = { "Website/App", "Username", "Password" };
        tableModel = new DefaultTableModel(columns, 0);
        passwordTable = new JTable(tableModel);
        passwordTable.setFont(normalFont);
        passwordTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(passwordTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        loadPasswords();

        // Responsive Button Panel using FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));

        addButton = createButton("➕ Add", new Color(76, 175, 80));
        showHideButton = createButton("👁 Show / Hide", new Color(33, 150, 243));
        deleteButton = createButton("🗑 Delete", new Color(244, 67, 54));
        logoutBtn = createButton("Logout", new Color(255, 152, 0));

        buttonPanel.add(addButton);
        buttonPanel.add(showHideButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        addButton.addActionListener(e -> addPassword());
        showHideButton.addActionListener(e -> toggleShowPasswords());
        deleteButton.addActionListener(e -> deletePassword());
        logoutBtn.addActionListener(e -> {
            currentAccountID = null;
            initLoginUI();
        });

        revalidate();
        repaint();
    }

    // =================================================
    // LOGIC & UTILITIES
    // =================================================
    private void checkLogin() {
        String accountID = loginIdField.getText().trim();
        String pass = new String(masterPassField.getPassword());

        if (accounts.containsKey(accountID)) {
            if (decrypt(accounts.get(accountID)).equals(pass)) {
                currentAccountID = accountID;
                initDashboardUI();
            } else {
                JOptionPane.showMessageDialog(this, "Wrong Password ❌");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Account ID not found ❌");
        }
    }

    private void createAccountUI() {
        JTextField accountField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] msg = { "Account ID:", accountField, "Master Password:", passField };

        if (JOptionPane.showConfirmDialog(this, msg, "Create Account",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = accountField.getText().trim();
            String pw = new String(passField.getPassword());
            if (!id.isEmpty() && !pw.isEmpty()) {
                accounts.put(id, encrypt(pw));
                saveAccounts();
                JOptionPane.showMessageDialog(this, "Account Created! ✅");
            }
        }
    }

    private void addPassword() {
        JTextField site = new JTextField();
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        Object[] msg = { "Site:", site, "User:", user, "Pass:", pass };

        if (JOptionPane.showConfirmDialog(this, msg, "Add Entry",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String enc = encrypt(new String(pass.getPassword()));
            savePasswordToFile(currentAccountID, site.getText(), user.getText(), enc);
            tableModel.addRow(new Object[] { site.getText(), user.getText(), "******" });
        }
    }

    private void toggleShowPasswords() {
        showPasswords = !showPasswords;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String enc = getEncryptedPasswordFromFile(currentAccountID, (String) tableModel.getValueAt(i, 0),
                    (String) tableModel.getValueAt(i, 1));
            tableModel.setValueAt(showPasswords ? decrypt(enc) : "******", i, 2);
        }
    }

    private void deletePassword() {
        int row = passwordTable.getSelectedRow();
        if (row >= 0) {
            removePasswordFromFile(currentAccountID, (String) tableModel.getValueAt(row, 0),
                    (String) tableModel.getValueAt(row, 1));
            tableModel.removeRow(row);
        }
    }

    // --- STYLING HELPERS ---
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 35));
        return btn;
    }

    private void styleOutlineButton(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btn.setFocusPainted(false);
    }

    private void fadeInFrame() {
        setOpacity(0f);
        Timer timer = new Timer(20, e -> {
            float op = getOpacity() + 0.05f;
            if (op >= 1f) {
                setOpacity(1f);
                ((Timer) e.getSource()).stop();
            } else
                setOpacity(op);
        });
        timer.start();
    }

    // --- FILE OPS (Simplified) ---
    private void saveAccounts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(accountsFile))) {
            for (String key : accounts.keySet())
                bw.write(key + "|" + accounts.get(key) + "\n");
        } catch (Exception e) {
        }
    }

    private void loadAccounts() {
        try (BufferedReader br = new BufferedReader(new FileReader(accountsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 2)
                    accounts.put(p[0], p[1]);
            }
        } catch (Exception e) {
        }
    }

    private void loadPasswords() {
        try (BufferedReader br = new BufferedReader(new FileReader(passwordsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(currentAccountID))
                    tableModel.addRow(new Object[] { p[1], p[2], "******" });
            }
        } catch (Exception e) {
        }
    }

    private void savePasswordToFile(String id, String s, String u, String p) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(passwordsFile, true))) {
            bw.write(id + "|" + s + "|" + u + "|" + p + "\n");
        } catch (Exception e) {
        }
    }

    private String getEncryptedPasswordFromFile(String id, String s, String u) {
        try (BufferedReader br = new BufferedReader(new FileReader(passwordsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals(id) && p[1].equals(s) && p[2].equals(u))
                    return p[3];
            }
        } catch (Exception e) {
        }
        return "";
    }

    private void removePasswordFromFile(String id, String s, String u) {
        File temp = new File("temp.dat");
        try (BufferedReader br = new BufferedReader(new FileReader(passwordsFile));
                BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (!(p[0].equals(id) && p[1].equals(s) && p[2].equals(u)))
                    bw.write(line + "\n");
            }
        } catch (Exception e) {
        }
        new File(passwordsFile).delete();
        temp.renameTo(new File(passwordsFile));
    }

    private String encrypt(String t) {
        return Base64.getEncoder().encodeToString(t.getBytes());
    }

    private String decrypt(String e) {
        return new String(Base64.getDecoder().decode(e));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SecureVaultMultiAccount::new);
    }
}
