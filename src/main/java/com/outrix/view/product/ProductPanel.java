package com.outrix.view.product;

import com.outrix.dao.CategoryDAO;
import com.outrix.dao.ProductDAO;
import com.outrix.dao.SupplierDAO;
import com.outrix.model.Category;
import com.outrix.model.Product;
import com.outrix.model.Supplier;
import com.outrix.util.ActivityLogger;
import com.outrix.view.components.*;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product Management panel – full CRUD with search and filter.
 */
public class ProductPanel extends JPanel {

    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private TablePanel tablePanel;
    private SearchBar  searchBar;
    private JComboBox<String> categoryFilter;
    private List<Category>   categories;
    private List<Supplier>   suppliers;
    private List<Product>    currentList;

    public ProductPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(ThemeManager.bg());
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("📦  Product Management");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());
        topBar.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchBar = new SearchBar("Search products...");
        searchBar.addSearchListener(kw -> filterProducts(kw));
        actions.add(searchBar);

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(ThemeManager.FONT_BODY);
        categoryFilter.setBackground(ThemeManager.surface());
        categoryFilter.setForeground(ThemeManager.text());
        categoryFilter.addActionListener(e -> filterProducts(searchBar.getSearchText()));
        actions.add(categoryFilter);

        RoundedButton importBtn = RoundedButton.secondary("📥 Import");
        importBtn.addActionListener(e -> importExcel());
        RoundedButton exportBtn = RoundedButton.secondary("📤 Export");
        exportBtn.addActionListener(e -> exportExcel());
        actions.add(importBtn);
        actions.add(exportBtn);

        RoundedButton addBtn = new RoundedButton("+ Add Product");
        addBtn.addActionListener(e -> openProductDialog(null));
        actions.add(addBtn);

        topBar.add(actions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        tablePanel = new TablePanel(new String[]{
            "ID", "Product Name", "Category", "Supplier",
            "Purchase Price", "Selling Price", "Quantity", "Status", "Barcode"
        });
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(ThemeManager.bg());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        tableWrapper.add(tablePanel, BorderLayout.CENTER);

        // ── Action buttons below table ────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(ThemeManager.bg());
        btnRow.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));

        RoundedButton editBtn   = RoundedButton.secondary("✏ Edit");
        RoundedButton deleteBtn = RoundedButton.danger("🗑 Delete");
        RoundedButton barcodeBtn= RoundedButton.secondary("📊 Codes");
        RoundedButton scanBtn   = RoundedButton.secondary("🔍 Scan Code");

        editBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow();
            if (row < 0) { toast("Select a product first."); return; }
            int id = (int) tablePanel.getValueAt(row, 0);
            Product p = currentList.stream().filter(pr -> pr.getId() == id).findFirst().orElse(null);
            openProductDialog(p);
        });

        deleteBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow();
            if (row < 0) { toast("Select a product first."); return; }
            int id   = (int)    tablePanel.getValueAt(row, 0);
            String nm= (String) tablePanel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete product \"" + nm + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try { productDAO.delete(id); ActivityLogger.log("DELETE_PRODUCT", "Deleted: " + nm); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        barcodeBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow();
            if (row < 0) { toast("Select a product first."); return; }
            int id = (int) tablePanel.getValueAt(row, 0);
            Product p = currentList.stream().filter(pr -> pr.getId() == id).findFirst().orElse(null);
            if (p != null) showBarcodeDialog(p);
        });

        scanBtn.addActionListener(e -> showScanDialog());

        btnRow.add(editBtn); btnRow.add(deleteBtn); btnRow.add(barcodeBtn); btnRow.add(scanBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.bg());
        center.add(tableWrapper, BorderLayout.CENTER);
        center.add(btnRow,       BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                categories  = categoryDAO.findAll();
                suppliers   = supplierDAO.findAll();
                currentList = productDAO.findAll();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    // Populate category filter
                    categoryFilter.removeAllItems();
                    categoryFilter.addItem("All Categories");
                    categories.forEach(c -> categoryFilter.addItem(c.getName()));
                    populateTable(currentList);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void filterProducts(String keyword) {
        if (currentList == null) return;
        String cat = (String) categoryFilter.getSelectedItem();
        boolean allCats = (cat == null || cat.equals("All Categories"));
        List<Product> filtered = currentList.stream()
                .filter(p -> keyword.isEmpty() ||
                        p.getProductName().toLowerCase().contains(keyword.toLowerCase()) ||
                        (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(keyword.toLowerCase())))
                .filter(p -> allCats || p.getCategoryName().equals(cat))
                .toList();
        populateTable(filtered);
    }

    private void populateTable(List<Product> list) {
        tablePanel.clearRows();
        for (Product p : list) {
            String status = p.isOutOfStock() ? "OUT OF STOCK" : p.isLowStock() ? "LOW STOCK" : "In Stock";
            tablePanel.addRow(new Object[]{
                p.getId(), p.getProductName(), p.getCategoryName(),
                p.getSupplierName() != null ? p.getSupplierName() : "–",
                String.format("$%.2f", p.getPurchasePrice()),
                String.format("$%.2f", p.getSellingPrice()),
                p.getQuantity(), status,
                p.getBarcode() != null ? p.getBarcode() : "–"
            });
        }
    }

    private void openProductDialog(Product existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Product" : "Add Product", true);
        dialog.setSize(520, 580);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(5,5,5,5);

        JTextField nameField  = field(existing != null ? existing.getProductName() : "");
        JTextField descField  = field(existing != null ? existing.getDescription() : "");
        JTextField ppField    = field(existing != null ? existing.getPurchasePrice().toPlainString() : "0.00");
        JTextField spField    = field(existing != null ? existing.getSellingPrice().toPlainString()  : "0.00");
        JTextField qtyField   = field(existing != null ? String.valueOf(existing.getQuantity())      : "0");
        JTextField threshField= field(existing != null ? String.valueOf(existing.getLowStockThreshold()): "10");
        JTextField barcodeField= field(existing != null ? (existing.getBarcode() != null ? existing.getBarcode() : "") : "");

        JComboBox<String> catBox = new JComboBox<>();
        categories.forEach(c -> catBox.addItem(c.getName()));
        if (existing != null) catBox.setSelectedItem(existing.getCategoryName());

        JComboBox<String> supBox = new JComboBox<>();
        supBox.addItem("None");
        suppliers.forEach(s -> supBox.addItem(s.getName()));
        if (existing != null && existing.getSupplierName() != null) supBox.setSelectedItem(existing.getSupplierName());

        int row = 0;
        addField(panel, gbc, row++, "Product Name *", nameField);
        addField(panel, gbc, row++, "Description",    descField);
        addField(panel, gbc, row++, "Category *",     catBox);
        addField(panel, gbc, row++, "Supplier",       supBox);
        addField(panel, gbc, row++, "Purchase Price", ppField);
        addField(panel, gbc, row++, "Selling Price",  spField);
        addField(panel, gbc, row++, "Quantity",       qtyField);
        addField(panel, gbc, row++, "Low Stock Alert",threshField);
        addField(panel, gbc, row++, "Barcode",        barcodeField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(ThemeManager.card());
        RoundedButton save = new RoundedButton(isEdit ? "Update" : "Save");
        RoundedButton cancel = RoundedButton.secondary("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Product name is required."); return; }
                String catName = (String) catBox.getSelectedItem();
                Category cat = categories.stream().filter(c -> c.getName().equals(catName)).findFirst().orElse(null);
                if (cat == null) { JOptionPane.showMessageDialog(dialog, "Invalid category."); return; }
                String supName = (String) supBox.getSelectedItem();
                Supplier sup = "None".equals(supName) ? null :
                        suppliers.stream().filter(s -> s.getName().equals(supName)).findFirst().orElse(null);

                Product p = isEdit ? existing : new Product();
                p.setProductName(name);
                p.setDescription(descField.getText().trim());
                p.setCategoryId(cat.getId()); p.setCategoryName(cat.getName());
                p.setSupplierId(sup != null ? sup.getId() : null);
                p.setSupplierName(sup != null ? sup.getName() : null);
                p.setPurchasePrice(new BigDecimal(ppField.getText().trim()));
                p.setSellingPrice(new BigDecimal(spField.getText().trim()));
                p.setQuantity(Integer.parseInt(qtyField.getText().trim()));
                p.setLowStockThreshold(Integer.parseInt(threshField.getText().trim()));
                String bc = barcodeField.getText().trim();
                p.setBarcode(bc.isEmpty() ? null : bc);

                if (isEdit) { productDAO.update(p); ActivityLogger.log("UPDATE_PRODUCT", "Updated: " + name); }
                else        { productDAO.insert(p); ActivityLogger.log("ADD_PRODUCT",    "Added: "   + name); }
                dialog.dispose(); loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage()); }
        });
        btnPanel.add(cancel); btnPanel.add(save);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showBarcodeDialog(Product p) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Codes – " + p.getProductName(), true);
        d.setSize(580, 360);
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(ThemeManager.card());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(p.getProductName() + " (Code: " + (p.getBarcode() != null ? p.getBarcode() : "NCODE") + ")");
        titleLbl.setFont(ThemeManager.FONT_HEADER);
        titleLbl.setForeground(ThemeManager.text());
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLbl, BorderLayout.NORTH);

        JPanel codesRow = new JPanel(new GridLayout(1, 2, 20, 0));
        codesRow.setOpaque(false);

        // Barcode Col
        JPanel barcodeCol = new JPanel(new BorderLayout(0, 8));
        barcodeCol.setOpaque(false);
        barcodeCol.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeManager.border()), "Barcode (Code 128)"));
        try {
            com.outrix.util.BarcodeUtil util = new com.outrix.util.BarcodeUtil();
            java.awt.image.BufferedImage img = util.generateBarcode(
                    p.getBarcode() != null ? p.getBarcode() : "NCODE", 240, 80);
            barcodeCol.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        } catch (Exception ex) {
            barcodeCol.add(new JLabel("Barcode generation failed", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        codesRow.add(barcodeCol);

        // QR Code Col
        JPanel qrCol = new JPanel(new BorderLayout(0, 8));
        qrCol.setOpaque(false);
        qrCol.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeManager.border()), "QR Code"));
        try {
            com.outrix.util.BarcodeUtil util = new com.outrix.util.BarcodeUtil();
            java.awt.image.BufferedImage img = util.generateQRCode(
                    p.getBarcode() != null ? p.getBarcode() : "NCODE", 160);
            qrCol.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        } catch (Exception ex) {
            qrCol.add(new JLabel("QR Code generation failed", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        codesRow.add(qrCol);

        panel.add(codesRow, BorderLayout.CENTER);

        RoundedButton close = RoundedButton.secondary("Close");
        close.addActionListener(e -> d.dispose());
        panel.add(close, BorderLayout.SOUTH);

        d.setContentPane(panel);
        d.setVisible(true);
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Products from Excel");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                List<Product> products = com.outrix.util.ExcelUtil.importProducts(file);
                for (Product p : products) {
                    Product existing = null;
                    if (p.getBarcode() != null) {
                        existing = productDAO.findByBarcode(p.getBarcode());
                    }
                    if (existing != null) {
                        p.setId(existing.getId());
                        productDAO.update(p);
                    } else {
                        productDAO.insert(p);
                    }
                }
                ActivityLogger.log("IMPORT_PRODUCTS", "Imported " + products.size() + " products from Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully imported " + products.size() + " products!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error importing Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Products to Excel");
        fc.setSelectedFile(new File("products_export.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                com.outrix.util.ExcelUtil.exportProducts(currentList, file);
                ActivityLogger.log("EXPORT_PRODUCTS", "Exported products to Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully exported products to Excel!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showScanDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Simulate Scanner", true);
        d.setSize(350, 160);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeManager.card());
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6,6,6,6); gbc.weightx = 1;

        JTextField codeField = field("");
        codeField.setToolTipText("Type or scan a barcode/QR Code");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lbl = new JLabel("Barcode:");
        lbl.setFont(ThemeManager.FONT_BODY); lbl.setForeground(ThemeManager.text());
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(codeField, gbc);

        RoundedButton searchBtn = new RoundedButton("Scan/Search");
        RoundedButton cancelBtn = RoundedButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> d.dispose());
        
        searchBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (code.isEmpty()) return;
            JTable jt = tablePanel.getTable();
            boolean found = false;
            for (int i = 0; i < jt.getRowCount(); i++) {
                String bc = (String) jt.getValueAt(i, 8); // Column 8 is Barcode
                if (code.equalsIgnoreCase(bc)) {
                    jt.setRowSelectionInterval(i, i);
                    jt.scrollRectToVisible(jt.getCellRect(i, 0, true));
                    found = true;
                    d.dispose();
                    break;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(d, "Product with barcode \"" + code + "\" not found in list.", "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        codeField.addActionListener(e -> searchBtn.doClick());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(ThemeManager.card());
        btns.add(cancelBtn); btns.add(searchBtn);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        p.add(btns, gbc);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val);
        f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.border(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void addField(JPanel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label); lbl.setFont(ThemeManager.FONT_BODY); lbl.setForeground(ThemeManager.text());
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}
