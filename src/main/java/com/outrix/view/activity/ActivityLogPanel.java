package com.outrix.view.activity;

import com.outrix.dao.ActivityLogDAO;
import com.outrix.model.ActivityLog;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Activity Log Dashboard (Admin only audit log).
 */
public class ActivityLogPanel extends JPanel {

    private final ActivityLogDAO dao = new ActivityLogDAO();
    private TablePanel tablePanel;
    private SearchBar searchBar;
    private JComboBox<String> limitBox;
    private List<ActivityLog> currentList;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("📋  System Activity Log");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        // Control Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        searchBar = new SearchBar("Search logs...");
        searchBar.addSearchListener(kw -> searchLogs(kw));
        controls.add(searchBar);

        limitBox = new JComboBox<>(new String[]{
            "Last 50 Logs",
            "Last 100 Logs",
            "Last 200 Logs",
            "Last 500 Logs"
        });
        limitBox.setFont(ThemeManager.FONT_BODY);
        limitBox.setBackground(ThemeManager.surface());
        limitBox.setForeground(ThemeManager.text());
        limitBox.addActionListener(e -> loadData());
        controls.add(limitBox);

        RoundedButton refreshBtn = new RoundedButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        controls.add(refreshBtn);

        top.add(controls, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Logs table
        tablePanel = new TablePanel(new String[]{
            "Log ID", "Cashier / User", "Action Type", "Description", "IP Address", "Timestamp"
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(ThemeManager.bg());
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        wrap.add(tablePanel, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
    }

    public void loadData() {
        String limitStr = (String) limitBox.getSelectedItem();
        int limit = 50;
        if (limitStr != null) {
            limit = Integer.parseInt(limitStr.replaceAll("\\D+", ""));
        }
        final int finalLimit = limit;

        SwingWorker<List<ActivityLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ActivityLog> doInBackground() throws Exception {
                return dao.findAll(finalLimit);
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    populateTable(currentList);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void searchLogs(String keyword) {
        if (keyword.isEmpty()) {
            populateTable(currentList);
            return;
        }

        SwingWorker<List<ActivityLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ActivityLog> doInBackground() throws Exception {
                return dao.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<ActivityLog> filtered = get();
                    populateTable(filtered);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<ActivityLog> list) {
        tablePanel.clearRows();
        if (list == null) return;
        for (ActivityLog log : list) {
            tablePanel.addRow(new Object[]{
                log.getId(),
                log.getUsername() != null ? log.getUsername() : "System",
                log.getAction(),
                log.getDescription(),
                log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1",
                log.getCreatedAt() != null ? log.getCreatedAt().toString().substring(0, 19) : ""
            });
        }
    }
}
