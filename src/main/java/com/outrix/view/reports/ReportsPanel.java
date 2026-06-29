package com.outrix.view.reports;

import com.outrix.dao.*;
import com.outrix.model.Employee;
import com.outrix.model.Product;
import com.outrix.model.Sale;
import com.outrix.model.Supplier;
import com.outrix.util.ActivityLogger;
import com.outrix.util.ExcelUtil;
import com.outrix.util.ReportGenerator;
import com.outrix.view.components.RoundedButton;
import com.outrix.view.components.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Reports Panel allowing exports of PDF reports (using iText 7) and Excel sheets (using Apache POI).
 */
public class ReportsPanel extends JPanel {

    private JComboBox<String> reportTypeBox;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JLabel fromLabel;
    private JLabel toLabel;

    // KPI Summary labels
    private JLabel lblProductsVal;
    private JLabel lblAlertsVal;
    private JLabel lblSuppliersVal;
    private JLabel lblRevenueVal;

    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
        loadKPIs();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.bg());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("📄  Report Generation Station");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body split layout
        JPanel body = new JPanel(new GridLayout(1, 2, 24, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 24, 24, 24));

        // Left Panel: Selection and Actions
        JPanel leftPanel = buildLeftConfigPanel();
        body.add(leftPanel);

        // Right Panel: KPI Summary Preview
        JPanel rightPanel = buildRightSummaryPanel();
        body.add(rightPanel);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildLeftConfigPanel() {
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

        JLabel sectionTitle = new JLabel("Configure Export Parameters");
        sectionTitle.setFont(ThemeManager.FONT_HEADER);
        sectionTitle.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(sectionTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        // Report Type
        JLabel lblType = new JLabel("Report Category:");
        lblType.setFont(ThemeManager.FONT_BODY);
        lblType.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        card.add(lblType, gbc);

        reportTypeBox = new JComboBox<>(new String[]{
            "Sales Analysis Report",
            "Stock Inventory Report",
            "Suppliers Directory",
            "Employees Roster",
            "Financial Revenue Summary"
        });
        reportTypeBox.setFont(ThemeManager.FONT_BODY);
        reportTypeBox.setBackground(ThemeManager.surface());
        reportTypeBox.addActionListener(e -> toggleDateFields());
        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(reportTypeBox, gbc);

        row++;

        // Date fields (conditional)
        fromLabel = new JLabel("From Date (YYYY-MM-DD):");
        fromLabel.setFont(ThemeManager.FONT_BODY);
        fromLabel.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        card.add(fromLabel, gbc);

        fromDateField = styledField(LocalDate.now().minusMonths(1).toString());
        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(fromDateField, gbc);

        row++;

        toLabel = new JLabel("To Date (YYYY-MM-DD):");
        toLabel.setFont(ThemeManager.FONT_BODY);
        toLabel.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        card.add(toLabel, gbc);

        toDateField = styledField(LocalDate.now().toString());
        gbc.gridx = 1; gbc.weightx = 0.65;
        card.add(toDateField, gbc);

        row++;

        // Action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btns.setOpaque(false);

        RoundedButton pdfBtn = new RoundedButton("📄 Export to PDF");
        pdfBtn.setColors(ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_BLUE_HOVER, Color.WHITE);
        pdfBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pdfBtn.addActionListener(e -> generatePDFReport());

        RoundedButton excelBtn = RoundedButton.secondary("📊 Export to Excel");
        excelBtn.addActionListener(e -> generateExcelReport());

        btns.add(excelBtn);
        btns.add(pdfBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        card.add(btns, gbc);

        // Initial check
        toggleDateFields();

        return card;
    }

    private JPanel buildRightSummaryPanel() {
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

        JLabel sectionTitle = new JLabel("Live ERP Metrics Overview");
        sectionTitle.setFont(ThemeManager.FONT_HEADER);
        sectionTitle.setForeground(ThemeManager.text());
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(sectionTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        lblProductsVal = addMetricRow(card, gbc, row++, "Total Catalog Products:", "–", ThemeManager.text());
        lblAlertsVal   = addMetricRow(card, gbc, row++, "Low Stock Alerts active:", "–", ThemeManager.ACCENT_ORANGE);
        lblSuppliersVal= addMetricRow(card, gbc, row++, "Registered Suppliers Count:", "–", ThemeManager.text());
        lblRevenueVal  = addMetricRow(card, gbc, row++, "Today's Gross Sales Revenue:", "$0.00", ThemeManager.ACCENT_GREEN);

        // Filler
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        card.add(Box.createVerticalGlue(), gbc);

        return card;
    }

    private JLabel addMetricRow(JPanel p, GridBagConstraints gbc, int row, String label, String defaultVal, Color valColor) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.textMuted());
        p.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(defaultVal);
        val.setFont(ThemeManager.FONT_SUBHEAD);
        val.setForeground(valColor);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(val, gbc);

        return val;
    }

    private void toggleDateFields() {
        String type = (String) reportTypeBox.getSelectedItem();
        boolean needsDate = "Sales Analysis Report".equals(type) || "Financial Revenue Summary".equals(type);
        fromLabel.setVisible(needsDate);
        fromDateField.setVisible(needsDate);
        toLabel.setVisible(needsDate);
        toDateField.setVisible(needsDate);
    }

    private void loadKPIs() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            int products, alerts, suppliers;
            BigDecimal revenue = BigDecimal.ZERO;

            @Override
            protected Void doInBackground() throws Exception {
                products = productDAO.count();
                alerts = productDAO.findLowStock().size();
                suppliers = supplierDAO.count();
                revenue = saleDAO.getTodayRevenue();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblProductsVal.setText(String.valueOf(products));
                    lblAlertsVal.setText(String.valueOf(alerts));
                    lblSuppliersVal.setText(String.valueOf(suppliers));
                    lblRevenueVal.setText(String.format("$%,.2f", revenue));
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void generatePDFReport() {
        String type = (String) reportTypeBox.getSelectedItem();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export PDF Report");
        fc.setSelectedFile(new File(type.toLowerCase().replace(" ", "_") + ".pdf"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            
            // Build the PDF based on the selection
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if ("Sales Analysis Report".equals(type)) {
                        String from = fromDateField.getText().trim();
                        String to = toDateField.getText().trim();
                        List<Sale> sales = saleDAO.findByDateRange(from, to);
                        ReportGenerator.generateSalesReport(dest, from, to, sales);
                    } else if ("Stock Inventory Report".equals(type)) {
                        List<Product> products = productDAO.findAll();
                        ReportGenerator.generateInventoryReport(dest, products);
                    } else if ("Suppliers Directory".equals(type)) {
                        List<Supplier> suppliers = supplierDAO.findAll();
                        ReportGenerator.generateSupplierReport(dest, suppliers);
                    } else if ("Employees Roster".equals(type)) {
                        List<Employee> employees = employeeDAO.findAll();
                        ReportGenerator.generateEmployeeReport(dest, employees);
                    } else if ("Financial Revenue Summary".equals(type)) {
                        List<Object[]> revenue = saleDAO.getMonthlyRevenue();
                        ReportGenerator.generateRevenueReport(dest, revenue);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        ActivityLogger.log("GENERATE_PDF", "Generated PDF report: " + type);
                        JOptionPane.showMessageDialog(ReportsPanel.this, "✅ Report generated successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ReportsPanel.this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
    }

    private void generateExcelReport() {
        String type = (String) reportTypeBox.getSelectedItem();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Excel Spreadsheet");
        fc.setSelectedFile(new File(type.toLowerCase().replace(" ", "_") + ".xlsx"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if ("Sales Analysis Report".equals(type)) {
                        String from = fromDateField.getText().trim();
                        String to = toDateField.getText().trim();
                        List<Sale> sales = saleDAO.findByDateRange(from, to);
                        ExcelUtil.exportSales(sales, dest);
                    } else if ("Stock Inventory Report".equals(type)) {
                        List<Product> products = productDAO.findAll();
                        ExcelUtil.exportProducts(products, dest);
                    } else if ("Suppliers Directory".equals(type)) {
                        List<Supplier> suppliers = supplierDAO.findAll();
                        ExcelUtil.exportSuppliers(suppliers, dest);
                    } else if ("Employees Roster".equals(type)) {
                        // POI export for employees roster (simple format, since ExcelUtil has custom ones, we can write a quick custom workbook)
                        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                            org.apache.poi.ss.usermodel.Sheet s = wb.createSheet("Employees");
                            String[] headers = {"ID", "Name", "Email", "Phone", "Role", "Hire Date"};
                            org.apache.poi.ss.usermodel.Row hr = s.createRow(0);
                            for (int i = 0; i < headers.length; i++) {
                                hr.createCell(i).setCellValue(headers[i]);
                            }
                            List<Employee> list = employeeDAO.findAll();
                            int rIdx = 1;
                            for (Employee e : list) {
                                org.apache.poi.ss.usermodel.Row row = s.createRow(rIdx++);
                                row.createCell(0).setCellValue(e.getId());
                                row.createCell(1).setCellValue(e.getName());
                                row.createCell(2).setCellValue(e.getEmail() != null ? e.getEmail() : "");
                                row.createCell(3).setCellValue(e.getPhone() != null ? e.getPhone() : "");
                                row.createCell(4).setCellValue(e.getRole() != null ? e.getRole() : "");
                                row.createCell(5).setCellValue(e.getHireDate() != null ? e.getHireDate().toString() : "");
                            }
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                                wb.write(fos);
                            }
                        }
                    } else if ("Financial Revenue Summary".equals(type)) {
                        List<Object[]> revenue = saleDAO.getMonthlyRevenue();
                        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                            org.apache.poi.ss.usermodel.Sheet s = wb.createSheet("Revenue Summary");
                            String[] headers = {"Month-Year", "Gross Revenue Amount"};
                            org.apache.poi.ss.usermodel.Row hr = s.createRow(0);
                            for (int i = 0; i < headers.length; i++) {
                                hr.createCell(i).setCellValue(headers[i]);
                            }
                            int rIdx = 1;
                            for (Object[] r : revenue) {
                                org.apache.poi.ss.usermodel.Row row = s.createRow(rIdx++);
                                row.createCell(0).setCellValue((String) r[0]);
                                row.createCell(1).setCellValue(((BigDecimal) r[1]).doubleValue());
                            }
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                                wb.write(fos);
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        ActivityLogger.log("GENERATE_EXCEL", "Generated Excel report: " + type);
                        JOptionPane.showMessageDialog(ReportsPanel.this, "✅ Excel generated successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ReportsPanel.this, "Error generating Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
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
