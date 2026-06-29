package com.outrix.view;

import com.outrix.config.DBConnection;
import com.outrix.dao.UserDAO;
import com.outrix.model.User;
import com.outrix.util.ActivityLogger;
import com.outrix.util.SessionManager;
import com.outrix.view.components.RoundedButton;
import com.outrix.view.components.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen with username/password fields, DB status indicator,
 * and animated brand panel.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         statusLabel;
    private RoundedButton  loginButton;

    public LoginFrame() {
        setTitle("Outrix ERP – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 900, 560, 20, 20));

        buildUI();
        checkDBConnection();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeManager.DARK_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        // ── Left brand panel ──────────────────────────────────────────────────
        JPanel brand = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(0x1D4ED8), 0, getHeight(), new Color(0x7C3AED));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Decorative circles
                g2.setColor(new Color(255,255,255,15));
                g2.fillOval(-60, -60, 220, 220);
                g2.fillOval(getWidth()-100, getHeight()-100, 200, 200);
                g2.dispose();
            }
        };
        brand.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0,0,20,0);

        JLabel logo = new JLabel("📦");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        brand.add(logo, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0,0,8,0);
        JLabel title = new JLabel("OUTRIX ERP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        brand.add(title, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0,30,0,30);
        JLabel subtitle = new JLabel("<html><center>Professional Inventory<br>Management System</center></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(255,255,255,180));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        brand.add(subtitle, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(30,40,0,40);
        JLabel features = new JLabel(
            "<html><center>✓ Inventory Tracking<br>✓ Sales & Billing<br>✓ Analytics & Reports</center></html>");
        features.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        features.setForeground(new Color(255,255,255,150));
        features.setHorizontalAlignment(SwingConstants.CENTER);
        brand.add(features, gbc);

        // ── Right login panel ─────────────────────────────────────────────────
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(ThemeManager.DARK_CARD);
        loginPanel.setBorder(new EmptyBorder(50, 50, 40, 50));

        // Drag support
        addDragSupport(loginPanel);

        // Close button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.setAlignmentX(LEFT_ALIGNMENT);
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setForeground(ThemeManager.DARK_TEXT_MUTED);
        closeBtn.setBackground(new Color(0,0,0,0));
        closeBtn.setBorderPainted(false); closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        topBar.add(closeBtn);
        loginPanel.add(topBar);

        loginPanel.add(Box.createVerticalStrut(20));

        // Heading
        JLabel heading = new JLabel("Welcome Back 👋");
        heading.setFont(ThemeManager.FONT_TITLE);
        heading.setForeground(ThemeManager.DARK_TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(heading);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(ThemeManager.FONT_BODY);
        sub.setForeground(ThemeManager.DARK_TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(sub);

        loginPanel.add(Box.createVerticalStrut(30));

        // DB status
        statusLabel = new JLabel("● Connecting to database...");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.ACCENT_ORANGE);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(statusLabel);
        loginPanel.add(Box.createVerticalStrut(20));

        // Username
        loginPanel.add(makeLabel("Username"));
        usernameField = styledTextField("Enter username");
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(usernameField);
        loginPanel.add(Box.createVerticalStrut(14));

        // Password
        loginPanel.add(makeLabel("Password"));
        passwordField = new JPasswordField();
        styleField(passwordField, "Enter password");
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(24));

        // Login button
        loginButton = new RoundedButton("Sign In");
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(e -> performLogin());
        loginPanel.add(loginButton);
        loginPanel.add(Box.createVerticalStrut(12));

        // Forgot Password Link
        JPanel helperRow = new JPanel(new BorderLayout());
        helperRow.setOpaque(false);
        helperRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        helperRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel forgotLink = new JLabel("Forgot Password?");
        forgotLink.setFont(ThemeManager.FONT_SMALL);
        forgotLink.setForeground(ThemeManager.ACCENT_BLUE);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openForgotPasswordDialog(); }
            @Override public void mouseEntered(MouseEvent e) { forgotLink.setForeground(ThemeManager.ACCENT_BLUE.brighter()); }
            @Override public void mouseExited(MouseEvent e) { forgotLink.setForeground(ThemeManager.ACCENT_BLUE); }
        });
        helperRow.add(forgotLink, BorderLayout.EAST);
        loginPanel.add(helperRow);
        loginPanel.add(Box.createVerticalStrut(12));

        // Default credentials hint
        JLabel hint = new JLabel("Demo: admin / Admin@123");
        hint.setFont(ThemeManager.FONT_SMALL);
        hint.setForeground(ThemeManager.DARK_TEXT_MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(hint);

        loginPanel.add(Box.createVerticalGlue());

        JLabel ver = new JLabel("Outrix ERP v1.0  •  © 2025");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(ThemeManager.DARK_TEXT_MUTED);
        ver.setAlignmentX(LEFT_ALIGNMENT);
        loginPanel.add(ver);

        root.add(brand);
        root.add(loginPanel);

        // Enter key triggers login
        getRootPane().setDefaultButton(null);
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeManager.FONT_SUBHEAD);
        lbl.setForeground(ThemeManager.DARK_TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField f = new JTextField(placeholder) {
            { setForeground(ThemeManager.DARK_TEXT_MUTED); }
            public void addNotify() {
                super.addNotify();
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) {
                        if (getText().equals(placeholder)) { setText(""); setForeground(ThemeManager.DARK_TEXT); }
                    }
                    @Override public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) { setText(placeholder); setForeground(ThemeManager.DARK_TEXT_MUTED); }
                    }
                });
            }
        };
        styleField(f, placeholder);
        return f;
    }

    private void styleField(JTextField f, String placeholder) {
        f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.DARK_SURFACE);
        f.setForeground(ThemeManager.DARK_TEXT);
        f.setCaretColor(ThemeManager.DARK_TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.DARK_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        char[] passChars = passwordField.getPassword();
        String password  = new String(passChars);

        if (username.isEmpty() || password.isEmpty()) {
            shake();
            statusLabel.setText("⚠ Please enter username and password.");
            statusLabel.setForeground(ThemeManager.ACCENT_ORANGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() throws Exception {
                UserDAO dao = new UserDAO();
                return dao.authenticate(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user == null) {
                        statusLabel.setText("✕ Invalid username or password.");
                        statusLabel.setForeground(ThemeManager.ACCENT_RED);
                        shake();
                    } else {
                        SessionManager.setCurrentUser(user);
                        ActivityLogger.log(user.getId(), user.getUsername(), "LOGIN",
                                "User logged in successfully.");
                        statusLabel.setText("✓ Login successful!");
                        statusLabel.setForeground(ThemeManager.ACCENT_GREEN);
                        // Launch main window
                        Timer timer = new Timer(400, ev -> {
                            MainFrame mainFrame = new MainFrame();
                            mainFrame.setVisible(true);
                            dispose();
                        });
                        timer.setRepeats(false); timer.start();
                    }
                } catch (Exception ex) {
                    statusLabel.setText("✕ Error: " + ex.getMessage());
                    statusLabel.setForeground(ThemeManager.ACCENT_RED);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    private void checkDBConnection() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() { return DBConnection.testConnection(); }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    statusLabel.setText(ok ? "● Database connected" : "● Database connection failed");
                    statusLabel.setForeground(ok ? ThemeManager.ACCENT_GREEN : ThemeManager.ACCENT_RED);
                } catch (Exception e) {
                    statusLabel.setText("● Database connection failed");
                    statusLabel.setForeground(ThemeManager.ACCENT_RED);
                }
            }
        };
        worker.execute();
    }

    /** Shakes the frame to indicate an error. */
    private void shake() {
        int orig = getLocationOnScreen().x;
        Timer t = new Timer(30, null);
        int[] count = {0};
        int[] offsets = {5, -10, 10, -10, 10, -5, 0};
        t.addActionListener(e -> {
            if (count[0] < offsets.length) {
                setLocation(orig + offsets[count[0]], getLocationOnScreen().y);
                count[0]++;
            } else { ((Timer)e.getSource()).stop(); setLocation(orig, getLocationOnScreen().y); }
        });
        t.start();
    }

    private Point dragStart;
    private void addDragSupport(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart = e.getPoint(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    Point loc = getLocationOnScreen();
                    setLocation(loc.x + e.getX() - dragStart.x, loc.y + e.getY() - dragStart.y);
                }
            }
        });
    }

    private void openForgotPasswordDialog() {
        JDialog d = new JDialog(this, "Forgot Password", true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.DARK_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6,6,6,6); gbc.weightx = 1;

        JTextField unameField = styledTextField("Enter username");
        JTextField emailField = styledTextField("Enter email");
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        panel.add(makeLabel("Username"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; panel.add(unameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        panel.add(makeLabel("Email"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; panel.add(emailField, gbc);

        // Security check panel (for new password)
        JPanel resetPassPanel = new JPanel(new GridBagLayout());
        resetPassPanel.setOpaque(false);
        resetPassPanel.setVisible(false);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.HORIZONTAL; gbc2.insets = new Insets(6,6,6,6); gbc2.weightx = 1;

        JPasswordField newPassField = new JPasswordField();
        styleField(newPassField, "Enter new password");
        JPasswordField confirmPassField = new JPasswordField();
        styleField(confirmPassField, "Confirm password");

        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.weightx = 0.35;
        resetPassPanel.add(makeLabel("New Password"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 0.65; resetPassPanel.add(newPassField, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.weightx = 0.35;
        resetPassPanel.add(makeLabel("Confirm"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 0.65; resetPassPanel.add(confirmPassField, gbc2);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(resetPassPanel, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(ThemeManager.DARK_CARD);
        RoundedButton verifyBtn = new RoundedButton("Verify Account");
        RoundedButton resetBtn = new RoundedButton("Reset Password");
        resetBtn.setVisible(false);
        RoundedButton cancel = RoundedButton.secondary("Cancel");
        cancel.addActionListener(e -> d.dispose());
        
        final User[] verifiedUser = {null};

        verifyBtn.addActionListener(e -> {
            String username = unameField.getText().trim();
            String email = emailField.getText().trim();
            if (username.isEmpty() || email.isEmpty() || username.equals("Enter username") || email.equals("Enter email")) {
                JOptionPane.showMessageDialog(d, "Please enter both fields.");
                return;
            }
            try {
                UserDAO dao = new UserDAO();
                User user = dao.findByUsername(username);
                if (user != null && email.equalsIgnoreCase(user.getEmail())) {
                    verifiedUser[0] = user;
                    unameField.setEnabled(false);
                    emailField.setEnabled(false);
                    resetPassPanel.setVisible(true);
                    verifyBtn.setVisible(false);
                    resetBtn.setVisible(true);
                    d.pack();
                    d.setSize(400, 380);
                    d.setLocationRelativeTo(this);
                } else {
                    JOptionPane.showMessageDialog(d, "Invalid username or email match.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        resetBtn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword());
            String confPass = new String(confirmPassField.getPassword());
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Password cannot be empty.");
                return;
            }
            if (!newPass.equals(confPass)) {
                JOptionPane.showMessageDialog(d, "Passwords do not match.");
                return;
            }
            try {
                UserDAO dao = new UserDAO();
                dao.changePassword(verifiedUser[0].getId(), newPass);
                ActivityLogger.log(verifiedUser[0].getId(), verifiedUser[0].getUsername(), "RESET_PASSWORD", "User reset password via Forgot Password screen.");
                JOptionPane.showMessageDialog(d, "Password successfully reset!");
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        btns.add(cancel); btns.add(verifyBtn); btns.add(resetBtn);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btns, gbc);

        d.setContentPane(panel);
        d.setVisible(true);
    }
}
