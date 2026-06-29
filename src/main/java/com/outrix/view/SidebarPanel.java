package com.outrix.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.outrix.util.ActivityLogger;
import com.outrix.util.SessionManager;
import com.outrix.view.components.ThemeManager;
import com.outrix.view.components.RoundedButton;
import com.outrix.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Left sidebar navigation panel with menu items, user info, and theme toggle.
 */
public class SidebarPanel extends JPanel {

    private final Consumer<String> navigator;
    private String activePanel = "dashboard";

    // Menu items: [icon, label, panelKey, adminOnly]
    private static final Object[][] MENU_ITEMS = {
        {"🏠", "Dashboard",   "dashboard",  false},
        {"📦", "Products",    "products",   false},
        {"🏷", "Categories",  "categories", false},
        {"🚚", "Suppliers",   "suppliers",  false},
        {"👥", "Employees",   "employees",  true },
        {"👤", "Customers",   "customers",  false},
        {"📊", "Inventory",   "inventory",  false},
        {"💰", "Sales",       "sales",      false},
        {"📈", "Analytics",   "analytics",  false},
        {"📄", "Reports",     "reports",    false},
        {"📋", "Activity Log","activity",   true },
        {"💾", "Backup",      "backup",     true },
    };

    private java.util.List<SidebarItem> items = new java.util.ArrayList<>();

    public SidebarPanel(Consumer<String> navigator) {
        this.navigator = navigator;
        setPreferredSize(new Dimension(220, 0));
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        // Content wrapper
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.sidebar());
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Logo area
        JPanel logoArea = new JPanel();
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBackground(ThemeManager.sidebar());
        logoArea.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));
        logoArea.setAlignmentX(LEFT_ALIGNMENT);

        JLabel logoIcon = new JLabel("📦 OUTRIX");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoIcon.setForeground(ThemeManager.ACCENT_BLUE);
        logoArea.add(logoIcon);

        JLabel logoSub = new JLabel("Inventory Management");
        logoSub.setFont(ThemeManager.FONT_SMALL);
        logoSub.setForeground(ThemeManager.textMuted());
        logoArea.add(logoSub);

        content.add(logoArea);

        // Separator
        content.add(makeSeparator());
        content.add(Box.createVerticalStrut(8));

        // Navigation label
        JLabel navLabel = new JLabel("NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navLabel.setForeground(ThemeManager.textMuted());
        navLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 0));
        navLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(navLabel);

        boolean isAdmin = SessionManager.isAdmin();

        for (Object[] item : MENU_ITEMS) {
            boolean adminOnly = (boolean) item[3];
            if (adminOnly && !isAdmin) continue;

            SidebarItem si = new SidebarItem(
                    item[0].toString(), item[1].toString(), item[2].toString(), navigator);
            items.add(si);
            content.add(si);
        }

        content.add(Box.createVerticalGlue());
        content.add(makeSeparator());

        // Theme toggle button
        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        themeRow.setBackground(ThemeManager.sidebar());
        themeRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton themeBtn = new JButton("☀ Light Mode");
        themeBtn.setFont(ThemeManager.FONT_SMALL);
        themeBtn.setForeground(ThemeManager.textMuted());
        themeBtn.setBackground(ThemeManager.surface());
        themeBtn.setBorder(BorderFactory.createLineBorder(ThemeManager.border(), 1, true));
        themeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeBtn.addActionListener(e -> {
            ThemeManager.toggle();
            if (ThemeManager.isDark()) {
                try { FlatDarkLaf.setup(); } catch (Exception ex) {}
                themeBtn.setText("☀ Light Mode");
            } else {
                try { FlatLightLaf.setup(); } catch (Exception ex) {}
                themeBtn.setText("🌙 Dark Mode");
            }
            SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
        });
        themeRow.add(themeBtn);
        content.add(themeRow);

        // User info area
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setBackground(ThemeManager.sidebar());
        userPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        String name = SessionManager.getCurrentUser().getFullName();
        String role = SessionManager.getCurrentUser().getRole();
        JLabel nameLabel = new JLabel(name != null ? name : "User");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(ThemeManager.text());

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(ThemeManager.FONT_SMALL);
        roleLabel.setForeground(role != null && role.equals("ADMIN") ?
                ThemeManager.ACCENT_BLUE : ThemeManager.textMuted());

        userInfo.add(nameLabel); userInfo.add(roleLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        JButton keyBtn = new JButton("🔑");
        keyBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        keyBtn.setForeground(ThemeManager.textMuted());
        keyBtn.setBackground(new Color(0,0,0,0));
        keyBtn.setBorderPainted(false); keyBtn.setContentAreaFilled(false);
        keyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        keyBtn.setToolTipText("Change Password");
        keyBtn.addActionListener(e -> openChangePasswordDialog());

        JButton logoutBtn = new JButton("Exit");
        logoutBtn.setFont(ThemeManager.FONT_SMALL);
        logoutBtn.setForeground(ThemeManager.ACCENT_RED);
        logoutBtn.setBackground(new Color(0,0,0,0));
        logoutBtn.setBorderPainted(false); logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                ActivityLogger.log("LOGOUT", "User logged out.");
                SessionManager.clearSession();
                Window w = SwingUtilities.getWindowAncestor(this);
                w.dispose();
                new LoginFrame().setVisible(true);
            }
        });

        actionPanel.add(keyBtn);
        actionPanel.add(logoutBtn);

        userPanel.add(avatarLabel, BorderLayout.WEST);
        userPanel.add(userInfo,    BorderLayout.CENTER);
        userPanel.add(actionPanel,   BorderLayout.EAST);
        content.add(userPanel);

        // Right border separator
        JPanel borderLine = new JPanel();
        borderLine.setPreferredSize(new Dimension(1, 0));
        borderLine.setBackground(ThemeManager.border());

        add(content,    BorderLayout.CENTER);
        add(borderLine, BorderLayout.EAST);
    }

    private JPanel makeSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBackground(ThemeManager.border());
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    /** Highlights the active sidebar item. */
    public void setActive(String panelKey) {
        this.activePanel = panelKey;
        items.forEach(item -> item.setSelected(item.getPanelKey().equals(panelKey)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner class: individual sidebar nav item
    // ─────────────────────────────────────────────────────────────────────────

    private static class SidebarItem extends JPanel {
        private final String    panelKey;
        private boolean         selected = false;
        private boolean         hovered  = false;
        private final JLabel    iconLabel;
        private final JLabel    textLabel;

        SidebarItem(String icon, String label, String key, Consumer<String> nav) {
            this.panelKey = key;
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(220, 40));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));

            textLabel = new JLabel(label);
            textLabel.setFont(ThemeManager.FONT_BODY);
            textLabel.setForeground(ThemeManager.text());

            add(iconLabel); add(textLabel);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { nav.accept(key); }
            });
        }

        void setSelected(boolean sel) {
            this.selected = sel;
            textLabel.setForeground(sel ? ThemeManager.ACCENT_BLUE : ThemeManager.text());
            iconLabel.setForeground(sel ? ThemeManager.ACCENT_BLUE : ThemeManager.text());
            repaint();
        }

        String getPanelKey() { return panelKey; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(new Color(ThemeManager.ACCENT_BLUE.getRed(),
                        ThemeManager.ACCENT_BLUE.getGreen(),
                        ThemeManager.ACCENT_BLUE.getBlue(), 30));
                g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 8, 8);
                g2.setColor(ThemeManager.ACCENT_BLUE);
                g2.fillRoundRect(0, 8, 3, getHeight()-16, 3, 3);
            } else if (hovered) {
                g2.setColor(ThemeManager.hover());
                g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void openChangePasswordDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
        d.setSize(400, 260);
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6,6,6,6); gbc.weightx = 1;

        JPasswordField currentPassField = new JPasswordField();
        stylePass(currentPassField);
        JPasswordField newPassField = new JPasswordField();
        stylePass(newPassField);
        JPasswordField confirmPassField = new JPasswordField();
        stylePass(confirmPassField);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        panel.add(label("Current Password"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6; panel.add(currentPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        panel.add(label("New Password"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6; panel.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        panel.add(label("Confirm Password"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6; panel.add(confirmPassField, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(ThemeManager.card());
        RoundedButton save = new RoundedButton("Update");
        RoundedButton cancel = RoundedButton.secondary("Cancel");
        cancel.addActionListener(e -> d.dispose());

        save.addActionListener(e -> {
            String current = new String(currentPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Please fill in all fields.");
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(d, "New passwords do not match.");
                return;
            }

            try {
                com.outrix.dao.UserDAO dao = new com.outrix.dao.UserDAO();
                User user = dao.authenticate(SessionManager.getCurrentUsername(), current);
                if (user == null) {
                    JOptionPane.showMessageDialog(d, "Incorrect current password.");
                    return;
                }
                dao.changePassword(user.getId(), newPass);
                ActivityLogger.log("CHANGE_PASSWORD", "User successfully changed password.");
                JOptionPane.showMessageDialog(d, "Password updated successfully!");
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        btns.add(cancel); btns.add(save);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btns, gbc);

        d.setContentPane(panel);
        d.setVisible(true);
    }

    private JPasswordField stylePass(JPasswordField f) {
        f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface());
        f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.border(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.FONT_BODY);
        l.setForeground(ThemeManager.text());
        return l;
    }
}
