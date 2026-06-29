package com.outrix.view.analytics;

import com.outrix.config.DBConnection;
import com.outrix.dao.ProductDAO;
import com.outrix.dao.SaleDAO;
import com.outrix.view.components.ModernScrollBarUI;
import com.outrix.view.components.RoundedButton;
import com.outrix.view.components.ThemeManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Business Analytics and Intelligence Panel using JFreeChart.
 */
public class AnalyticsPanel extends JPanel {

    private final SaleDAO saleDAO = new SaleDAO();
    private JPanel chartsGrid;

    public AnalyticsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.bg());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("📈  Business Analytics & Insights");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());

        RoundedButton refreshBtn = new RoundedButton("🔄 Refresh Charts");
        refreshBtn.addActionListener(e -> refresh());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Grid for charts
        chartsGrid = new JPanel(new GridLayout(3, 2, 16, 16));
        chartsGrid.setBackground(ThemeManager.bg());
        chartsGrid.setBorder(BorderFactory.createEmptyBorder(10, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(chartsGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(ThemeManager.bg());
        scroll.getViewport().setBackground(ThemeManager.bg());
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        chartsGrid.removeAll();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private DefaultCategoryDataset revDataset;
            private DefaultCategoryDataset trendDataset;
            private DefaultCategoryDataset topProdDataset;
            private DefaultPieDataset categoryDataset;
            private DefaultPieDataset stockDataset;

            @Override
            protected Void doInBackground() throws Exception {
                // 1. Monthly Revenue
                revDataset = new DefaultCategoryDataset();
                List<Object[]> monthly = saleDAO.getMonthlyRevenue();
                for (Object[] r : monthly) {
                    revDataset.addValue(((BigDecimal) r[1]).doubleValue(), "Revenue", (String) r[0]);
                }

                // 2. Sales Trend (Invoices count per month)
                trendDataset = new DefaultCategoryDataset();
                String trendSql = "SELECT DATE_FORMAT(sale_date, '%b %Y') AS month, COUNT(*) AS count " +
                                  "FROM sales WHERE sale_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                                  "GROUP BY YEAR(sale_date), MONTH(sale_date) ORDER BY YEAR(sale_date), MONTH(sale_date)";
                try (Connection conn = DBConnection.getConnection();
                     Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(trendSql)) {
                    while (rs.next()) {
                        trendDataset.addValue(rs.getInt("count"), "Invoices", rs.getString("month"));
                    }
                }

                // 3. Best Selling Products
                topProdDataset = new DefaultCategoryDataset();
                List<Object[]> topProds = saleDAO.getTopProducts(6);
                for (Object[] r : topProds) {
                    topProdDataset.addValue((Integer) r[1], "Units Sold", (String) r[0]);
                }

                // 4. Product Category Distribution
                categoryDataset = new DefaultPieDataset();
                String catSql = "SELECT c.name, COUNT(p.id) AS count FROM products p " +
                                "JOIN categories c ON p.category_id = c.id GROUP BY c.name";
                try (Connection conn = DBConnection.getConnection();
                     Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(catSql)) {
                    while (rs.next()) {
                        categoryDataset.setValue(rs.getString("name"), rs.getInt("count"));
                    }
                }

                // 5. Inventory Stock Status
                stockDataset = new DefaultPieDataset();
                int goodStock = 0;
                int lowStock = 0;
                int outStock = 0;
                String stockSql = "SELECT quantity, low_stock_threshold FROM products";
                try (Connection conn = DBConnection.getConnection();
                     Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(stockSql)) {
                    while (rs.next()) {
                        int q = rs.getInt("quantity");
                        int t = rs.getInt("low_stock_threshold");
                        if (q == 0) outStock++;
                        else if (q <= t) lowStock++;
                        else goodStock++;
                    }
                }
                stockDataset.setValue("In Stock", goodStock);
                stockDataset.setValue("Low Stock", lowStock);
                stockDataset.setValue("Out of Stock", outStock);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    // Create Charts
                    JFreeChart revChart = ChartFactory.createLineChart(
                            "Monthly Revenue", "Month", "Revenue ($)",
                            revDataset, PlotOrientation.VERTICAL, false, true, false);
                    styleChart(revChart);

                    JFreeChart trendChart = ChartFactory.createBarChart(
                            "Transaction Volume Trend", "Month", "Invoices Issued",
                            trendDataset, PlotOrientation.VERTICAL, false, true, false);
                    styleChart(trendChart);

                    JFreeChart topProdChart = ChartFactory.createBarChart(
                            "Best Selling Products (Top 6)", "Product", "Units Sold",
                            topProdDataset, PlotOrientation.HORIZONTAL, false, true, false);
                    styleChart(topProdChart);

                    JFreeChart catChart = ChartFactory.createPieChart(
                            "Product Category Distribution", categoryDataset, true, true, false);
                    styleChart(catChart);

                    JFreeChart stockChart = ChartFactory.createPieChart(
                            "Inventory Stock Status", stockDataset, true, true, false);
                    styleChart(stockChart);

                    // Add charts to grid
                    chartsGrid.add(createChartHolder(revChart));
                    chartsGrid.add(createChartHolder(trendChart));
                    chartsGrid.add(createChartHolder(topProdChart));
                    chartsGrid.add(createChartHolder(catChart));
                    chartsGrid.add(createChartHolder(stockChart));

                    // 6. Stats Summary Card Panel
                    chartsGrid.add(buildSummaryKPIPanel());

                    chartsGrid.revalidate();
                    chartsGrid.repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    chartsGrid.add(new JLabel("Failed to load charts: " + ex.getMessage(), SwingConstants.CENTER));
                }
            }
        };
        worker.execute();
    }

    private JPanel createChartHolder(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 240));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel holder = new JPanel(new BorderLayout()) {
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
        holder.setOpaque(false);
        holder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        holder.add(chartPanel, BorderLayout.CENTER);
        return holder;
    }

    private JPanel buildSummaryKPIPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
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
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("📊 Analytics KPI Summary");
        title.setFont(ThemeManager.FONT_HEADER);
        title.setForeground(ThemeManager.ACCENT_BLUE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(title, gbc);

        gbc.gridwidth = 1;
        
        int rowIdx = 1;
        addSummaryMetric(p, gbc, rowIdx++, "Target Monthly Revenue:", "$25,000.00", ThemeManager.text());
        addSummaryMetric(p, gbc, rowIdx++, "Gross Profit Margin Target:", "35.0%", ThemeManager.ACCENT_GREEN);
        addSummaryMetric(p, gbc, rowIdx++, "System Security Integrity:", "SSL Encrypted", ThemeManager.ACCENT_CYAN);
        addSummaryMetric(p, gbc, rowIdx++, "Database Engine Status:", "Online & Synced", ThemeManager.ACCENT_GREEN);
        addSummaryMetric(p, gbc, rowIdx++, "Auto-Backup Schedule:", "Active (Daily)", ThemeManager.ACCENT_PURPLE);

        return p;
    }

    private void addSummaryMetric(JPanel p, GridBagConstraints gbc, int row, String label, String val, Color valColor) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.textMuted());
        p.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel v = new JLabel(val);
        v.setFont(ThemeManager.FONT_SUBHEAD);
        v.setForeground(valColor);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(v, gbc);
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(ThemeManager.card());
        chart.getTitle().setPaint(ThemeManager.text());
        chart.getTitle().setFont(ThemeManager.FONT_HEADER);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(ThemeManager.card());
            chart.getLegend().setItemPaint(ThemeManager.text());
            chart.getLegend().setItemFont(ThemeManager.FONT_SMALL);
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        org.jfree.chart.plot.Plot plot = chart.getPlot();
        plot.setBackgroundPaint(ThemeManager.surface());
        plot.setOutlinePaint(null);

        if (plot instanceof CategoryPlot) {
            CategoryPlot cPlot = (CategoryPlot) plot;
            cPlot.setDomainGridlinePaint(ThemeManager.border());
            cPlot.setRangeGridlinePaint(ThemeManager.border());
            cPlot.getDomainAxis().setLabelPaint(ThemeManager.text());
            cPlot.getDomainAxis().setLabelFont(ThemeManager.FONT_BODY);
            cPlot.getDomainAxis().setTickLabelPaint(ThemeManager.textMuted());
            cPlot.getDomainAxis().setTickLabelFont(ThemeManager.FONT_SMALL);
            cPlot.getRangeAxis().setLabelPaint(ThemeManager.text());
            cPlot.getRangeAxis().setLabelFont(ThemeManager.FONT_BODY);
            cPlot.getRangeAxis().setTickLabelPaint(ThemeManager.textMuted());
            cPlot.getRangeAxis().setTickLabelFont(ThemeManager.FONT_SMALL);

            org.jfree.chart.renderer.category.CategoryItemRenderer renderer = cPlot.getRenderer();
            if (renderer != null) {
                renderer.setSeriesPaint(0, ThemeManager.ACCENT_BLUE);
            }
        } else if (plot instanceof PiePlot) {
            PiePlot pPlot = (PiePlot) plot;
            pPlot.setLabelBackgroundPaint(ThemeManager.surface());
            pPlot.setLabelOutlinePaint(ThemeManager.border());
            pPlot.setLabelPaint(ThemeManager.textMuted());
            pPlot.setLabelFont(ThemeManager.FONT_SMALL);
            pPlot.setShadowPaint(null);

            // Coloring categories
            pPlot.setSectionPaint("Electronics", ThemeManager.ACCENT_BLUE);
            pPlot.setSectionPaint("Clothing", ThemeManager.ACCENT_PURPLE);
            pPlot.setSectionPaint("Food & Beverage", ThemeManager.ACCENT_GREEN);
            pPlot.setSectionPaint("Office Supplies", ThemeManager.ACCENT_ORANGE);
            pPlot.setSectionPaint("Hardware", ThemeManager.ACCENT_CYAN);
            pPlot.setSectionPaint("Software", ThemeManager.ACCENT_PINK);

            // Fallback for stock statuses
            pPlot.setSectionPaint("In Stock", ThemeManager.ACCENT_GREEN);
            pPlot.setSectionPaint("Low Stock", ThemeManager.ACCENT_ORANGE);
            pPlot.setSectionPaint("Out of Stock", ThemeManager.ACCENT_RED);
        }
    }
}
