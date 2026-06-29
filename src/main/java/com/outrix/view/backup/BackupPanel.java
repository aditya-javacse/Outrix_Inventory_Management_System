package com.outrix.view.backup;

import com.outrix.util.ActivityLogger;
import com.outrix.util.DatabaseBackupUtil;
import com.outrix.view.components.RoundedButton;
import com.outrix.view.components.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database Backup and Restore Control Panel (Admin only).
 * Supports manual sql dumps/restores and configures scheduled background auto-backups.
 */
public class BackupPanel extends JPanel {

    private JCheckBox chkEnableAuto;
    private JComboBox<String> cmbFrequency;
    private JTextField txtDestDir;
    private RoundedButton btnBrowse;
    private RoundedButton btnSavePolicy;

    private static final String CONFIG_FILE = "backup_policy.properties";
    private static ScheduledExecutorService scheduler;

    public BackupPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
        loadPolicySettings();
        initAutoBackupScheduler();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.bg());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("💾  Database Backup & Recovery Services");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel body = new JPanel(new GridLayout(1, 2, 24, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 24, 24, 24));

        // Left Panel: Manual Maintenance
        JPanel manualPanel = buildManualMaintenancePanel();
        body.add(manualPanel);

        // Right Panel: Automated Maintenance Policy
        JPanel autoPanel = buildAutoPolicyPanel();
        body.add(autoPanel);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildManualMaintenancePanel() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.card());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(ThemeManager.border());
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(12, 8, 12, 8);

        JLabel sectionTitle = new JLabel("Manual Database Operations");
        sectionTitle.setFont(ThemeManager.FONT_HEADER);
        sectionTitle.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(sectionTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        JLabel lblDump = new JLabel("Backup Data: Snapshot database to SQL file.");
        lblDump.setFont(ThemeManager.FONT_BODY);
        lblDump.setForeground(ThemeManager.textMuted());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(lblDump, gbc);

        row++;

        RoundedButton btnBackup = new RoundedButton("💾 Backup Database Now");
        btnBackup.setColors(ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_GREEN.darker(), Color.WHITE);
        btnBackup.addActionListener(e -> runManualBackup());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(btnBackup, gbc);

        row++;
        card.add(Box.createVerticalStrut(15), gbc);
        row++;

        JLabel lblRestore = new JLabel("Restore Data: Load database from SQL backup file.");
        lblRestore.setFont(ThemeManager.FONT_BODY);
        lblRestore.setForeground(ThemeManager.textMuted());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(lblRestore, gbc);

        row++;

        RoundedButton btnRestore = new RoundedButton("🔄 Restore Database Snapshot");
        btnRestore.setColors(ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_ORANGE.darker(), Color.WHITE);
        btnRestore.addActionListener(e -> runManualRestore());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(btnRestore, gbc);

        // Filler
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        card.add(Box.createVerticalGlue(), gbc);

        return card;
    }

    private JPanel buildAutoPolicyPanel() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.card());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(ThemeManager.border());
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 8, 10, 8);

        JLabel sectionTitle = new JLabel("Automated Backup Settings");
        sectionTitle.setFont(ThemeManager.FONT_HEADER);
        sectionTitle.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(sectionTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        chkEnableAuto = new JCheckBox("Enable Automatic Periodic Backups");
        chkEnableAuto.setFont(ThemeManager.FONT_BODY);
        chkEnableAuto.setForeground(ThemeManager.text());
        chkEnableAuto.setOpaque(false);
        chkEnableAuto.addActionListener(e -> toggleAutoFields());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(chkEnableAuto, gbc);

        row++;
        gbc.gridwidth = 1;

        JLabel lblFreq = new JLabel("Backup Frequency:");
        lblFreq.setFont(ThemeManager.FONT_BODY);
        lblFreq.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        card.add(lblFreq, gbc);

        cmbFrequency = new JComboBox<>(new String[]{
            "Every 24 Hours",
            "Every 7 Days",
            "Every 30 Days"
        });
        cmbFrequency.setFont(ThemeManager.FONT_BODY);
        cmbFrequency.setBackground(ThemeManager.surface());
        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(cmbFrequency, gbc);

        row++;

        JLabel lblDest = new JLabel("Backup Folder Path:");
        lblDest.setFont(ThemeManager.FONT_BODY);
        lblDest.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        card.add(lblDest, gbc);

        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setOpaque(false);
        txtDestDir = styledField("");
        btnBrowse = RoundedButton.secondary("Browse...");
        btnBrowse.addActionListener(e -> selectBackupFolder());
        pathRow.add(txtDestDir, BorderLayout.CENTER);
        pathRow.add(btnBrowse, BorderLayout.EAST);

        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(pathRow, gbc);

        row++;

        btnSavePolicy = new RoundedButton("⚙ Save Policy Settings");
        btnSavePolicy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSavePolicy.addActionListener(e -> savePolicySettings());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        card.add(btnSavePolicy, gbc);

        // Init visibility
        toggleAutoFields();

        return card;
    }

    private void toggleAutoFields() {
        boolean active = chkEnableAuto.isSelected();
        cmbFrequency.setEnabled(active);
        txtDestDir.setEnabled(active);
        btnBrowse.setEnabled(active);
    }

    private void selectBackupFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Folder for Auto-Backups");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDestDir.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void runManualBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Backup Database Snapshot");
        fc.setSelectedFile(new File("outrix_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".sql"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    DatabaseBackupUtil.backup(dest);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        ActivityLogger.log("DB_BACKUP", "Manual database backup created: " + dest.getName());
                        JOptionPane.showMessageDialog(BackupPanel.this, "✅ Database backup snapshot saved successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BackupPanel.this, "Backup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
    }

    private void runManualRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠ WARNING: Restoring will overwrite all current database data.\nAre you sure you want to proceed?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Restore SQL Script");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Backups (*.sql)", "sql"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File src = fc.getSelectedFile();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    DatabaseBackupUtil.restore(src);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        ActivityLogger.log("DB_RESTORE", "Database successfully restored from: " + src.getName());
                        JOptionPane.showMessageDialog(BackupPanel.this, "✅ Database successfully restored from snapshot!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BackupPanel.this, "Restore failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
    }

    private void loadPolicySettings() {
        Properties prop = new Properties();
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                prop.load(fis);
                chkEnableAuto.setSelected(Boolean.parseBoolean(prop.getProperty("auto.enabled", "false")));
                cmbFrequency.setSelectedItem(prop.getProperty("auto.frequency", "Every 24 Hours"));
                txtDestDir.setText(prop.getProperty("auto.destDir", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        toggleAutoFields();
    }

    private void savePolicySettings() {
        Properties prop = new Properties();
        prop.setProperty("auto.enabled", String.valueOf(chkEnableAuto.isSelected()));
        prop.setProperty("auto.frequency", (String) cmbFrequency.getSelectedItem());
        prop.setProperty("auto.destDir", txtDestDir.getText().trim());

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            prop.store(fos, "Outrix Auto Backup Config");
            ActivityLogger.log("SAVE_BACKUP_POLICY", "Backup policy settings updated.");
            JOptionPane.showMessageDialog(this, "✅ Policy settings saved successfully!");
            initAutoBackupScheduler();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save config: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initAutoBackupScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        boolean active = chkEnableAuto.isSelected();
        if (!active) return;

        String freq = (String) cmbFrequency.getSelectedItem();
        String path = txtDestDir.getText().trim();
        if (path.isEmpty()) return;

        int hours = 24;
        if ("Every 7 Days".equals(freq)) hours = 168;
        else if ("Every 30 Days".equals(freq)) hours = 720;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                File dest = new File(dir, "outrix_auto_backup_" + stamp + ".sql");
                try {
                    DatabaseBackupUtil.backup(dest);
                    ActivityLogger.log(0, "SYSTEM", "AUTO_BACKUP", "Automatic scheduled database backup completed: " + dest.getName());
                    System.out.println("[AutoBackup] Scheduled backup completed: " + dest.getName());
                } catch (Exception e) {
                    System.err.println("[AutoBackup] Scheduled backup failed: " + e.getMessage());
                }
            }
        }, 1, hours, TimeUnit.HOURS);
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.border(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }
}
