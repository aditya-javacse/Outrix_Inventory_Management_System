package com.outrix.view;

import com.outrix.util.ActivityLogger;
import com.outrix.util.SessionManager;
import com.outrix.util.ThemeToggle;
import com.outrix.view.components.ThemeManager;
import com.outrix.view.dashboard.DashboardPanel;
import com.outrix.view.product.ProductPanel;
import com.outrix.view.category.CategoryPanel;
import com.outrix.view.supplier.SupplierPanel;
import com.outrix.view.employee.EmployeePanel;
import com.outrix.view.customer.CustomerPanel;
import com.outrix.view.inventory.InventoryPanel;
import com.outrix.view.sales.SalesPanel;
import com.outrix.view.analytics.AnalyticsPanel;
import com.outrix.view.reports.ReportsPanel;
import com.outrix.view.activity.ActivityLogPanel;
import com.outrix.view.backup.BackupPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window with sidebar navigation and content area.
 */
public class MainFrame extends JFrame {

    private JPanel         contentArea;
    private CardLayout     cardLayout;
    private SidebarPanel   sidebar;

    // Panels (lazy-initialized on first access)
    private DashboardPanel   dashboardPanel;
    private ProductPanel     productPanel;
    private CategoryPanel    categoryPanel;
    private SupplierPanel    supplierPanel;
    private EmployeePanel    employeePanel;
    private CustomerPanel    customerPanel;
    private InventoryPanel   inventoryPanel;
    private SalesPanel       salesPanel;
    private AnalyticsPanel   analyticsPanel;
    private ReportsPanel     reportsPanel;
    private ActivityLogPanel activityLogPanel;
    private BackupPanel      backupPanel;

    public MainFrame() {
        setTitle("Outrix ERP – " + SessionManager.getCurrentUser().getFullName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        buildUI();
        setupWindowListener();

        // Default to dashboard
        showPanel("dashboard");

        // Register theme change listener to repaint everything
        ThemeManager.addChangeListener(() -> SwingUtilities.invokeLater(this::repaintAll));
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(ThemeManager.bg());
        setContentPane(root);

        // Sidebar
        sidebar = new SidebarPanel(this::showPanel);
        root.add(sidebar, BorderLayout.WEST);

        // Content area with CardLayout
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(ThemeManager.bg());
        root.add(contentArea, BorderLayout.CENTER);
    }

    /** Navigates to the named panel, creating it lazily if needed. */
    public void showPanel(String name) {
        switch (name) {
            case "dashboard"   -> { if (dashboardPanel   == null) { dashboardPanel   = new DashboardPanel(this);   contentArea.add(dashboardPanel,   "dashboard"); } }
            case "products"    -> { if (productPanel     == null) { productPanel     = new ProductPanel();         contentArea.add(productPanel,     "products"); } }
            case "categories"  -> { if (categoryPanel    == null) { categoryPanel    = new CategoryPanel();        contentArea.add(categoryPanel,    "categories"); } }
            case "suppliers"   -> { if (supplierPanel    == null) { supplierPanel    = new SupplierPanel();        contentArea.add(supplierPanel,    "suppliers"); } }
            case "employees"   -> { if (employeePanel    == null) { employeePanel    = new EmployeePanel();        contentArea.add(employeePanel,    "employees"); } }
            case "customers"   -> { if (customerPanel    == null) { customerPanel    = new CustomerPanel();        contentArea.add(customerPanel,    "customers"); } }
            case "inventory"   -> { if (inventoryPanel   == null) { inventoryPanel   = new InventoryPanel();       contentArea.add(inventoryPanel,   "inventory"); } }
            case "sales"       -> { if (salesPanel       == null) { salesPanel       = new SalesPanel();           contentArea.add(salesPanel,       "sales"); } }
            case "analytics"   -> { if (analyticsPanel   == null) { analyticsPanel   = new AnalyticsPanel();       contentArea.add(analyticsPanel,   "analytics"); } }
            case "reports"     -> { if (reportsPanel     == null) { reportsPanel     = new ReportsPanel();         contentArea.add(reportsPanel,     "reports"); } }
            case "activity"    -> { if (activityLogPanel == null) { activityLogPanel = new ActivityLogPanel();     contentArea.add(activityLogPanel, "activity"); } }
            case "backup"      -> { if (backupPanel      == null) { backupPanel      = new BackupPanel();          contentArea.add(backupPanel,      "backup"); } }
        }
        cardLayout.show(contentArea, name);
        sidebar.setActive(name);

        // Refresh data when switching to a panel
        refreshPanel(name);
    }

    private void refreshPanel(String name) {
        switch (name) {
            case "dashboard"  -> { if (dashboardPanel   != null) dashboardPanel.refresh(); }
            case "products"   -> { if (productPanel     != null) productPanel.loadData(); }
            case "categories" -> { if (categoryPanel    != null) categoryPanel.loadData(); }
            case "suppliers"  -> { if (supplierPanel    != null) supplierPanel.loadData(); }
            case "employees"  -> { if (employeePanel    != null) employeePanel.loadData(); }
            case "customers"  -> { if (customerPanel    != null) customerPanel.loadData(); }
            case "inventory"  -> { if (inventoryPanel   != null) inventoryPanel.loadData(); }
            case "sales"      -> { if (salesPanel       != null) salesPanel.loadData(); }
            case "analytics"  -> { if (analyticsPanel   != null) analyticsPanel.refresh(); }
            case "activity"   -> { if (activityLogPanel != null) activityLogPanel.loadData(); }
        }
    }

    private void repaintAll() {
        getContentPane().setBackground(ThemeManager.bg());
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this, "Are you sure you want to exit?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    ActivityLogger.log("LOGOUT", "User logged out.");
                    SessionManager.clearSession();
                    System.exit(0);
                }
            }
        });
    }
}
