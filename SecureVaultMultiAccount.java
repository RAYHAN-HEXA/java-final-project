import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;

public class SecureVaultMultiAccount extends JFrame {

    Font TF = new Font("Segoe UI", Font.BOLD, 26), NF = new Font("Segoe UI", Font.PLAIN, 14);
    JTextField loginIdField; JPasswordField masterPassField;
    JTable passwordTable; DefaultTableModel tableModel;
    HashMap<String, String> accounts = new HashMap<>();
    String currentID = null; boolean showPass = false;
    final String AF = "accounts.dat", PF = "passwords.dat";

    // ── Demo vault data ──────────────────────────────────────────────────────
    final String[][] VAULT = {
        {"facebook.com",  "md.rayhan@gmail.com",  "Rayhan@FB2024"},
        {"github.com",    "rayhan-dev",            "Git#Secure99!"},
        {"gmail.com",     "md.rayhanx617",         "Gmail@Pass#1"},
        {"youtube.com",   "rayhan_yt",             "YT!Watch2024"},
        {"linkedin.com",  "rayhan.pro",            "Link$In2024"},
        {"instagram.com", "rayhan_ig",             "Insta@Snap#7"},
        {"twitter.com",   "rayhan_tw",             "Tweet!Bird99"},
        {"netflix.com",   "rayhan.stream",         "N3tFlix@2024"},
        {"amazon.com",    "rayhan.shop",           "Amaz0n$Buy!"},
        {"dropbox.com",   "rayhan.cloud",          "Dr0pB0x#Safe"}
    };

    public SecureVaultMultiAccount() {
        loadAccounts();
        // Auto-create demo account on first run
        if (!accounts.containsKey("demo")) {
            accounts.put("demo", enc("demo123"));
            saveAccounts();
        }
        loginUI();
        fade();
    }

    // ── LOGIN UI ─────────────────────────────────────────────────────────────
    void loginUI() {
        setTitle("SecureVault - Login"); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 450)); setSize(850, 530); setLocationRelativeTo(null);
        JPanel root = new JPanel(new GridLayout(1, 2));

        JPanel left = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,new Color(0,180,120),0,getHeight(),new Color(0,120,80)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        GridBagConstraints c = new GridBagConstraints(); c.gridx=0; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(10,20,10,20);
        JLabel wl = new JLabel("Welcome Back!", SwingConstants.CENTER);
        wl.setFont(new Font("Segoe UI",Font.BOLD,32)); wl.setForeground(Color.WHITE); left.add(wl,c);
        JLabel il = new JLabel("<html><center>To keep connected with us<br>please login with your info</center></html>",SwingConstants.CENTER);
        il.setFont(NF); il.setForeground(Color.WHITE); left.add(il,c);
        JButton ca = new JButton("Create Account");
        ca.setContentAreaFilled(false); ca.setForeground(Color.WHITE);
        ca.setBorder(BorderFactory.createLineBorder(Color.WHITE,2)); ca.setFocusPainted(false);
        ca.addActionListener(e -> createAccountUI());
        c.insets=new Insets(30,60,10,60); left.add(ca,c);
        // Demo credentials hint
        JLabel hint = new JLabel("<html><center><b>Demo Credentials</b><br>ID: demo | Pass: demo123</center></html>", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI",Font.PLAIN,12)); hint.setForeground(new Color(220,255,220));
        c.insets=new Insets(10,20,5,20); left.add(hint,c);

        JPanel right = new JPanel(new GridBagLayout()); right.setBackground(Color.WHITE);
        GridBagConstraints r = new GridBagConstraints(); r.gridx=0; r.fill=GridBagConstraints.HORIZONTAL;
        JLabel lt = new JLabel("Login Account",SwingConstants.CENTER);
        lt.setFont(TF); lt.setForeground(new Color(0,150,100));
        r.insets=new Insets(0,50,20,50); right.add(lt,r);
        r.insets=new Insets(5,50,2,50);
        right.add(new JLabel("Account ID"),r);
        loginIdField=new JTextField("demo"); loginIdField.setPreferredSize(new Dimension(0,35)); right.add(loginIdField,r);
        right.add(new JLabel("Master Password"),r);
        masterPassField=new JPasswordField("demo123"); masterPassField.setPreferredSize(new Dimension(0,35)); right.add(masterPassField,r);
        JButton lb=new JButton("SIGN IN"); lb.setBackground(new Color(0,180,120)); lb.setForeground(Color.WHITE);
        lb.setPreferredSize(new Dimension(0,40)); lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lb.addActionListener(e -> checkLogin());
        r.insets=new Insets(15,50,5,50); right.add(lb,r);
        JButton demo=new JButton("⚡ Demo Login");
        demo.setBackground(new Color(103,58,183)); demo.setForeground(Color.WHITE);
        demo.setPreferredSize(new Dimension(0,38)); demo.setFocusPainted(false);
        demo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demo.addActionListener(e -> { loginIdField.setText("demo"); masterPassField.setText("demo123"); checkLogin(); });
        r.insets=new Insets(4,50,10,50); right.add(demo,r);

        root.add(left); root.add(right); setContentPane(root); setVisible(true);
    }

    // ── DASHBOARD UI ─────────────────────────────────────────────────────────
    void dashUI() {
        setTitle("SecureVault - Dashboard (" + currentID + ")");
        getContentPane().removeAll(); setLayout(new BorderLayout());
        JLabel hdr = new JLabel("  Password Manager Dashboard - " + currentID);
        hdr.setFont(new Font("Segoe UI",Font.BOLD,20)); hdr.setForeground(Color.WHITE);
        hdr.setBackground(new Color(33,150,243)); hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createEmptyBorder(15,15,15,15)); add(hdr, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"Website/App","Username","Password"},0);
        passwordTable = new JTable(tableModel); passwordTable.setFont(NF); passwordTable.setRowHeight(30);
        add(new JScrollPane(passwordTable), BorderLayout.CENTER);
        loadPasswords();
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER,12,12)); bp.setBackground(new Color(245,245,245));
        JButton add=btn("➕ Add",new Color(76,175,80)), sh=btn("👁 Show/Hide",new Color(33,150,243)),
                del=btn("🗑 Delete",new Color(244,67,54)),
                vault=btn("🗃 Load Demo Data",new Color(103,58,183)),
                lo=btn("Logout",new Color(255,152,0));
        bp.add(add); bp.add(sh); bp.add(del); bp.add(vault); bp.add(lo); add(bp, BorderLayout.SOUTH);
        add.addActionListener(e -> addPassword());
        sh.addActionListener(e -> toggleShow());
        del.addActionListener(e -> deletePassword());
        vault.addActionListener(e -> loadVaultData());
        lo.addActionListener(e -> { currentID=null; loginUI(); });
        revalidate(); repaint();
    }

    // ── LOAD BULK DEMO VAULT DATA ─────────────────────────────────────────────
    void loadVaultData() {
        int added = 0;
        for (String[] row : VAULT) {
            // avoid duplicates
            if (getPW(currentID, row[0], row[1]).isEmpty()) {
                savePW(currentID, row[0], row[1], enc(row[2]));
                tableModel.addRow(new Object[]{row[0], row[1], "******"});
                added++;
            }
        }
        JOptionPane.showMessageDialog(this,
            added > 0 ? added + " demo entries loaded! ✅" : "All demo data already loaded ℹ️");
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────
    //  Login Verification (checkLogin)
    void checkLogin() {
        String id=loginIdField.getText().trim(), pw=new String(masterPassField.getPassword());
        if (!accounts.containsKey(id)) { JOptionPane.showMessageDialog(this,"Account ID not found ❌"); return; }
        if (dec(accounts.get(id)).equals(pw)) { currentID=id; dashUI(); }
        else JOptionPane.showMessageDialog(this,"Wrong Password ❌");
    }

    void createAccountUI() {
        JTextField af=new JTextField(); JPasswordField pf=new JPasswordField();
        if(JOptionPane.showConfirmDialog(this,new Object[]{"Account ID:",af,"Master Password:",pf},"Create Account",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            String id=af.getText().trim(), pw=new String(pf.getPassword());
            if(!id.isEmpty()&&!pw.isEmpty()){ accounts.put(id,enc(pw)); saveAccounts(); JOptionPane.showMessageDialog(this,"Account Created! ✅"); }
        }
    }

    // ── PASSWORD CRUD ─────────────────────────────────────────────────────────
    void addPassword() {
        String[][] s = VAULT; int idx=(int)(Math.random()*s.length);
        JTextField sf=new JTextField(s[idx][0]), uf=new JTextField(s[idx][1]);
        JPasswordField pf=new JPasswordField(s[idx][2]);
        JButton rf=new JButton("🎲 Refill"); rf.setFocusPainted(false);
        rf.addActionListener(e->{ int i=(int)(Math.random()*s.length); sf.setText(s[i][0]); uf.setText(s[i][1]); pf.setText(s[i][2]); });
        if(JOptionPane.showConfirmDialog(this,new Object[]{"Site:",sf,"User:",uf,"Pass:",pf,rf},"Add Entry ⚡ Auto-Filled",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            savePW(currentID,sf.getText(),uf.getText(),enc(new String(pf.getPassword())));
            tableModel.addRow(new Object[]{sf.getText(),uf.getText(),"******"});
        }
    }

    void toggleShow() {
        showPass=!showPass;
        for(int i=0;i<tableModel.getRowCount();i++){
            String e=getPW(currentID,(String)tableModel.getValueAt(i,0),(String)tableModel.getValueAt(i,1));
            tableModel.setValueAt(showPass?dec(e):"******",i,2);
        }
    }

    void deletePassword() {
        int row=passwordTable.getSelectedRow();
        if(row>=0){ delPW(currentID,(String)tableModel.getValueAt(row,0),(String)tableModel.getValueAt(row,1)); tableModel.removeRow(row); }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    JButton btn(String t, Color bg) {
        JButton b=new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setPreferredSize(new Dimension(145,35)); return b;
    }

    void fade() {
        setOpacity(0f);
        Timer tm=new Timer(20,e->{ float op=getOpacity()+0.05f; if(op>=1f){setOpacity(1f);((Timer)e.getSource()).stop();}else setOpacity(op); });
        tm.start();
    }

    // ── FILE OPS ─────────────────────────────────────────────────────────────
    void saveAccounts() {
        try(BufferedWriter bw=new BufferedWriter(new FileWriter(AF))){
            for(String k:accounts.keySet()) bw.write(k+"|"+accounts.get(k)+"\n");
        }catch(Exception ignored){}
    }

    void loadAccounts() {
        try(BufferedReader br=new BufferedReader(new FileReader(AF))){
            String l; while((l=br.readLine())!=null){String[]p=l.split("\\|");if(p.length==2)accounts.put(p[0],p[1]);}
        }catch(Exception ignored){}
    }

    void loadPasswords() {
        try(BufferedReader br=new BufferedReader(new FileReader(PF))){
            String l; while((l=br.readLine())!=null){String[]p=l.split("\\|");if(p[0].equals(currentID))tableModel.addRow(new Object[]{p[1],p[2],"******"});}
        }catch(Exception ignored){}
    }

    void savePW(String id,String s,String u,String p) {
        try(BufferedWriter bw=new BufferedWriter(new FileWriter(PF,true))){ bw.write(id+"|"+s+"|"+u+"|"+p+"\n"); }catch(Exception ignored){}
    }

    String getPW(String id,String s,String u) {
        try(BufferedReader br=new BufferedReader(new FileReader(PF))){
            String l; while((l=br.readLine())!=null){String[]p=l.split("\\|");if(p[0].equals(id)&&p[1].equals(s)&&p[2].equals(u))return p[3];}
        }catch(Exception ignored){} return "";
    }

    void delPW(String id,String s,String u) {
        File tmp=new File("temp.dat");
        try(BufferedReader br=new BufferedReader(new FileReader(PF));BufferedWriter bw=new BufferedWriter(new FileWriter(tmp))){
            String l; while((l=br.readLine())!=null){String[]p=l.split("\\|");if(!(p[0].equals(id)&&p[1].equals(s)&&p[2].equals(u)))bw.write(l+"\n");}
        }catch(Exception ignored){}
        new File(PF).delete(); tmp.renameTo(new File(PF));
    }

    String enc(String t){ return Base64.getEncoder().encodeToString(t.getBytes()); }
    String dec(String e){ return new String(Base64.getDecoder().decode(e)); }

    public static void main(String[] args){ SwingUtilities.invokeLater(SecureVaultMultiAccount::new); }
}