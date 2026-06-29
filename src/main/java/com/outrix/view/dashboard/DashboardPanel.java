package com.outrix.view.dashboard;

import com.outrix.dao.*;
import com.outrix.model.ActivityLog;
import com.outrix.model.Product;
import com.outrix.view.MainFrame;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard panel showing key metrics, low-stock alerts, and recent activity.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;

    private StatsCard cardProducts;
    private StatsCard cardCategories;
    private StatsCard cardSuppliers;
    private StatsCard cardEmployees;
    private StatsCard cardSales;
    private StatsCard cardRevenue;

    private TablePanel lowStockTable;
    private TablePanel recentActivityTable;

    private final ProductDAO     productDAO     = new ProductDAO();
    private final CategoryDAO    categoryDAO    = new CategoryDAO();
    private final SupplierDAO    supplierDAO    = new SupplierDAO();
    private final EmployeeDAO    employeeDAO    = new EmployeeDAO();
    private final SaleDAO        saleDAO        = new SaleDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.bg());
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 24));

        JLabel title = new JLabel("Dashboard");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());

        JLabel date = new JLabel(new java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new java.util.Date()));
        date.setFont(ThemeManager.FONT_BODY);
        date.setForeground(ThemeManager.textMuted());

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(title); titleBlock.add(date);
        header.add(titleBlock, BorderLayout.WEST);

        RoundedButton refreshBtn = new RoundedButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Scrollable body ────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(ThemeManager.bg());
        body.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        // Stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        cardProducts   = new StatsCard("Total Products",   "–", "📦", ThemeManager.ACCENT_BLUE);
        cardCategories = new StatsCard("Categories",       "–", "🏷", ThemeManager.ACCENT_PURPLE);
        cardSuppliers  = new StatsCard("Suppliers",        "–", "🚚", ThemeManager.ACCENT_CYAN);
        cardEmployees  = new StatsCard("Employees",        "–", "👥", ThemeManager.ACCENT_GREEN);
        cardSales      = new StatsCard("Total Sales",      "–", "💰", ThemeManager.ACCENT_ORANGE);
        cardRevenue    = new StatsCard("Today's Revenue",  "–", "💵", ThemeManager.ACCENT_PINK);

        statsRow.add(cardProducts); statsRow.add(cardCategories);
        statsRow.add(cardSuppliers); statsRow.add(cardEmployees);
        statsRow.add(cardSales); statsRow.add(cardRevenue);
        body.add(statsRow);
        body.add(Box.createVerticalStrut(20));

        // ── Lower two-column layout ────────────────────────────────────────────
        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Low stock alerts
        JPanel lowStockCard = buildSectionCard("⚠ Low Stock Alerts");
        lowStockTable = new TablePanel(new String[]{"Product", "Category", "Stock", "Threshold"});
        lowStockCard.add(lowStockTable, BorderLayout.CENTER);

        RoundedButton viewProducts = RoundedButton.secondary("View Products →");
        viewProducts.addActionListener(e -> mainFrame.showPanel("products"));
        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow1.setOpaque(false); btnRow1.add(viewProducts);
        lowStockCard.add(btnRow1, BorderLayout.SOUTH);
        lower.add(lowStockCard);

        // Recent activity
        JPanel activityCard = buildSectionCard("📋 Recent Activity");
        recentActivityTable = new TablePanel(new String[]{"User", "Action", "Description", "Time"});
        activityCard.add(recentActivityTable, BorderLayout.CENTER);
        lower.add(activityCard);

        body.add(lower);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(ThemeManager.bg());
        scroll.getViewport().setBackground(ThemeManager.bg());
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.card());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(ThemeManager.border());
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(ThemeManager.FONT_HEADER);
        lbl.setForeground(ThemeManager.text());
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    /** Loads/reloads all dashboard data in background thread. */
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int products, categories, suppliers, employees, sales;
            BigDecimal revenue = BigDecimal.ZERO;
            List<Product> lowStock;
            List<ActivityLog> logs;

            @Override protected Void doInBackground() throws Exception {
                products   = productDAO.count();
                categories = categoryDAO.count();
                suppliers  = supplierDAO.count();
                employees  = employeeDAO.count();
                sales      = saleDAO.count();
                revenue    = saleDAO.getTodayRevenue();
                lowStock   = productDAO.findLowStock();
                logs       = activityLogDAO.findAll(20);
                return null;
            }

            @Override protected void done() {
                try {
                    get(); // rethrow exceptions
                    cardProducts.setValue(String.valueOf(products));
                    cardCategories.setValue(String.valueOf(categories));
                    cardSuppliers.setValue(String.valueOf(suppliers));
                    cardEmployees.setValue(String.valueOf(employees));
                    cardSales.setValue(String.valueOf(sales));
                    cardRevenue.setValue(String.format("$%,.2f", revenue));

                    lowStockTable.clearRows();
                    for (Product p : lowStock) {
                        String stock = p.isOutOfStock() ? "OUT" : String.valueOf(p.getQuantity());
                        lowStockTable.addRow(new Object[]{
                            p.getProductName(), p.getCategoryName(), stock, p.getLowStockThreshold()
                        });
                    }

                    recentActivityTable.clearRows();
                    for (ActivityLog log : logs) {
                        recentActivityTable.addRow(new Object[]{
                            log.getUsername(), log.getAction(), log.getDescription(),
                            log.getCreatedAt() != null ?
                                new java.text.SimpleDateFormat("MM/dd HH:mm").format(log.getCreatedAt()) : ""
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
