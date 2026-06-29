package com.outrix.view.sales;

import com.outrix.dao.CustomerDAO;
import com.outrix.dao.ProductDAO;
import com.outrix.dao.SaleDAO;
import com.outrix.model.Customer;
import com.outrix.model.Product;
import com.outrix.model.Sale;
import com.outrix.model.SaleItem;
import com.outrix.util.ActivityLogger;
import com.outrix.util.SessionManager;
import com.outrix.view.components.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales/Billing panel – POS interface + sales history.
 */
public class SalesPanel extends JPanel {

    private final SaleDAO     saleDAO     = new SaleDAO();
    private final ProductDAO  productDAO  = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private TablePanel historyTable;

    // POS state
    private List<SaleItem>  cartItems   = new ArrayList<>();
    private List<Product>   productList = new ArrayList<>();
    private List<Customer>  customerList= new ArrayList<>();

    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JLabel grandTotalLabel;
    private JComboBox<String> customerBox;
    private JComboBox<String> productBox;
    private JTextField qtyField;
    private JTextField discountField;
    private JComboBox<String> paymentBox;

    public SalesPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = new JLabel("💰  Sales & Billing");
        title.setFont(ThemeManager.FONT_TITLE); title.setForeground(ThemeManager.text());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeManager.FONT_SUBHEAD);
        tabs.setBackground(ThemeManager.bg());
        tabs.addTab("🧾 New Sale", buildPOSPanel());
        tabs.addTab("📜 Sales History", buildHistoryPanel());
        tabs.addChangeListener(e -> { if (tabs.getSelectedIndex()==1) loadData(); });

        add(top, BorderLayout.NORTH);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setBackground(ThemeManager.bg());
        contentWrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        contentWrap.add(tabs, BorderLayout.CENTER);
        add(contentWrap, BorderLayout.CENTER);

        // Load reference data
        loadRefData();
    }

    private JPanel buildPOSPanel() {
        JPanel pos = new JPanel(new BorderLayout(12, 0));
        pos.setBackground(ThemeManager.bg());
        pos.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // ── Left: product selector + cart ────────────────────────────────────
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selector.setBackground(ThemeManager.card());
        selector.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),
            BorderFactory.createEmptyBorder(8,8,8,8)));

        productBox = new JComboBox<>();
        productBox.setFont(ThemeManager.FONT_BODY); productBox.setBackground(ThemeManager.surface());
        productBox.setPreferredSize(new Dimension(250, 32));

        qtyField = new JTextField("1", 4);
        qtyField.setFont(ThemeManager.FONT_BODY); qtyField.setBackground(ThemeManager.surface());
        qtyField.setForeground(ThemeManager.text()); qtyField.setCaretColor(ThemeManager.text());

        RoundedButton addToCart = new RoundedButton("Add to Cart");
        addToCart.addActionListener(e -> addToCart());

        JLabel prodLbl = new JLabel("Product:"); prodLbl.setForeground(ThemeManager.text()); prodLbl.setFont(ThemeManager.FONT_BODY);
        JLabel qtyLbl  = new JLabel("Qty:");     qtyLbl.setForeground(ThemeManager.text());  qtyLbl.setFont(ThemeManager.FONT_BODY);

        selector.add(prodLbl); selector.add(productBox);
        selector.add(qtyLbl);  selector.add(qtyField);
        selector.add(addToCart);
        left.add(selector, BorderLayout.NORTH);

        // Cart table
        String[] cartCols = {"Product","Qty","Unit Price","Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setFont(ThemeManager.FONT_BODY); cartTable.setForeground(ThemeManager.text());
        cartTable.setBackground(ThemeManager.card()); cartTable.setRowHeight(32); cartTable.setShowGrid(false);
        cartTable.getTableHeader().setFont(ThemeManager.FONT_SUBHEAD);
        cartTable.getTableHeader().setBackground(ThemeManager.surface()); cartTable.getTableHeader().setForeground(ThemeManager.textMuted());

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.border(),1,true));
        cartScroll.getViewport().setBackground(ThemeManager.card());
        left.add(cartScroll, BorderLayout.CENTER);

        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        cartBtns.setOpaque(false);
        RoundedButton removeBtn = RoundedButton.danger("Remove Item");
        RoundedButton clearBtn  = RoundedButton.secondary("Clear Cart");
        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) { cartItems.remove(row); cartModel.removeRow(row); updateTotals(); }
        });
        clearBtn.addActionListener(e -> { cartItems.clear(); cartModel.setRowCount(0); updateTotals(); });
        cartBtns.add(removeBtn); cartBtns.add(clearBtn);
        left.add(cartBtns, BorderLayout.SOUTH);

        // ── Right: customer + payment + totals ────────────────────────────────
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setPreferredSize(new Dimension(260, 0));
        right.setOpaque(false);

        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(ThemeManager.card());
        details.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,4,5,4); gbc.weightx=1;

        customerBox = new JComboBox<>(); customerBox.setFont(ThemeManager.FONT_BODY);
        discountField = new JTextField("0.00"); discountField.setFont(ThemeManager.FONT_BODY);
        discountField.setBackground(ThemeManager.surface()); discountField.setForeground(ThemeManager.text()); discountField.setCaretColor(ThemeManager.text());
        paymentBox = new JComboBox<>(new String[]{"CASH","CARD","MOBILE","OTHER"}); paymentBox.setFont(ThemeManager.FONT_BODY);

        totalLabel      = new JLabel("Subtotal: $0.00"); totalLabel.setFont(ThemeManager.FONT_BODY); totalLabel.setForeground(ThemeManager.text());
        grandTotalLabel = new JLabel("TOTAL: $0.00");   grandTotalLabel.setFont(new Font("Segoe UI",Font.BOLD,18)); grandTotalLabel.setForeground(ThemeManager.ACCENT_GREEN);

        addRow(details,gbc,0,"Customer",customerBox);
        addRow(details,gbc,1,"Discount ($)",discountField);
        addRow(details,gbc,2,"Payment",paymentBox);

        gbc.gridx=0;gbc.gridy=3;gbc.gridwidth=2; details.add(new JSeparator(),gbc);
        gbc.gridy=4; details.add(totalLabel,gbc);
        gbc.gridy=5; details.add(grandTotalLabel,gbc);

        right.add(details, BorderLayout.CENTER);

        RoundedButton checkoutBtn = new RoundedButton("💳 Checkout");
        checkoutBtn.setFont(new Font("Segoe UI",Font.BOLD,15));
        checkoutBtn.setColors(ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_GREEN.darker(), Color.WHITE);
        checkoutBtn.addActionListener(e -> checkout());
        right.add(checkoutBtn, BorderLayout.SOUTH);

        discountField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e){ updateTotals(); }
        });

        pos.add(left,  BorderLayout.CENTER);
        pos.add(right, BorderLayout.EAST);
        return pos;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setBackground(ThemeManager.bg());
        panel.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        historyTable = new TablePanel(new String[]{"Invoice","Customer","User","Total","Grand Total","Payment","Date"});
        panel.add(historyTable, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        btnRow.setBackground(ThemeManager.bg());
        RoundedButton viewBtn = RoundedButton.secondary("👁 View Details");
        viewBtn.addActionListener(e -> {
            int row = historyTable.getSelectedRow(); if (row<0) return;
            String inv = (String) historyTable.getValueAt(row,0);
            try {
                List<Sale> all = saleDAO.findAll();
                Sale s = all.stream().filter(sale->sale.getInvoiceNumber().equals(inv)).findFirst().orElse(null);
                if (s!=null) { Sale full = saleDAO.findById(s.getId()); showSaleDetails(full); }
            } catch(Exception ex){ ex.printStackTrace(); }
        });
        
        RoundedButton exportBtn = RoundedButton.secondary("📤 Export Excel");
        exportBtn.addActionListener(e -> exportSalesExcel());
        
        btnRow.add(viewBtn);
        btnRow.add(exportBtn);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRefData() {
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                productList  = productDAO.findAll();
                customerList = customerDAO.findAll();
                return null;
            }
            @Override protected void done() {
                productBox.removeAllItems();
                productList.forEach(p->productBox.addItem(p.getProductName()+"  [Qty:"+p.getQuantity()+"]"));
                customerBox.removeAllItems();
                customerList.forEach(c->customerBox.addItem(c.getName()));
            }
        };
        w.execute();
    }

    public void loadData() {
        SwingWorker<List<Sale>,Void> w = new SwingWorker<>() {
            @Override protected List<Sale> doInBackground() throws Exception { return saleDAO.findAll(); }
            @Override protected void done() {
                try {
                    List<Sale> list=get();
                    historyTable.clearRows();
                    for (Sale s: list)
                        historyTable.addRow(new Object[]{s.getInvoiceNumber(),
                            s.getCustomerName()!=null?s.getCustomerName():"Walk-in",
                            s.getUsername()!=null?s.getUsername():"–",
                            String.format("$%.2f",s.getTotalAmount()),
                            String.format("$%.2f",s.getGrandTotal()),
                            s.getPaymentMethod(),
                            s.getSaleDate()!=null?s.getSaleDate().toString().substring(0,16):"–"});
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void addToCart() {
        int idx = productBox.getSelectedIndex();
        if (idx < 0 || idx >= productList.size()) return;
        Product p = productList.get(idx);
        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) return;
            if (qty > p.getQuantity()) {
                JOptionPane.showMessageDialog(this,"Insufficient stock! Available: "+p.getQuantity()); return;
            }
            // Check if already in cart
            for (SaleItem item : cartItems) {
                if (item.getProductId()==p.getId()) {
                    item.setQuantity(item.getQuantity()+qty);
                    refreshCartTable(); updateTotals(); return;
                }
            }
            SaleItem item = new SaleItem(p.getId(), p.getProductName(), qty, p.getSellingPrice());
            cartItems.add(item);
            cartModel.addRow(new Object[]{p.getProductName(), qty, String.format("$%.2f",p.getSellingPrice()),
                String.format("$%.2f",item.getSubtotal())});
            updateTotals();
        } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(this,"Invalid quantity"); }
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (SaleItem item : cartItems)
            cartModel.addRow(new Object[]{item.getProductName(), item.getQuantity(),
                String.format("$%.2f",item.getUnitPrice()), String.format("$%.2f",item.getSubtotal())});
    }

    private void updateTotals() {
        BigDecimal total = cartItems.stream().map(SaleItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal disc;
        try { disc = new BigDecimal(discountField.getText().trim()); } catch(Exception e){ disc=BigDecimal.ZERO; }
        BigDecimal grand = total.subtract(disc).max(BigDecimal.ZERO);
        totalLabel.setText(String.format("Subtotal: $%.2f", total));
        grandTotalLabel.setText(String.format("TOTAL: $%.2f", grand));
    }

    private void checkout() {
        if (cartItems.isEmpty()) { JOptionPane.showMessageDialog(this,"Cart is empty!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,"Complete this sale?","Confirm Checkout",JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            BigDecimal total = cartItems.stream().map(SaleItem::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal disc;
            try { disc = new BigDecimal(discountField.getText().trim()); } catch(Exception e){ disc=BigDecimal.ZERO; }
            BigDecimal grand = total.subtract(disc).max(BigDecimal.ZERO);

            Sale sale = new Sale();
            sale.setInvoiceNumber(SaleDAO.generateInvoiceNumber());
            int custIdx = customerBox.getSelectedIndex();
            if (custIdx>=0&&custIdx<customerList.size()) sale.setCustomerId(customerList.get(custIdx).getId());
            sale.setUserId(SessionManager.getCurrentUserId());
            sale.setTotalAmount(total); sale.setDiscount(disc); sale.setTax(BigDecimal.ZERO);
            sale.setGrandTotal(grand); sale.setPaymentMethod((String)paymentBox.getSelectedItem());
            sale.setItems(cartItems);

            int saleId = saleDAO.saveSale(sale);
            ActivityLogger.log("CREATE_SALE","Invoice: "+sale.getInvoiceNumber()+", Total: $"+grand);

            JOptionPane.showMessageDialog(this,"✅ Sale completed!\nInvoice: "+sale.getInvoiceNumber()+"\nTotal: $"+String.format("%.2f",grand));
            cartItems.clear(); cartModel.setRowCount(0); updateTotals(); loadRefData();
        } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage()); }
    }

    private void showSaleDetails(Sale sale) {
        if (sale==null) return;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),"Sale – "+sale.getInvoiceNumber(),true);
        d.setSize(500,420); d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setBackground(ThemeManager.card()); panel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JLabel inv = new JLabel("Invoice: "+sale.getInvoiceNumber());
        inv.setFont(ThemeManager.FONT_HEADER); inv.setForeground(ThemeManager.text());
        panel.add(inv, BorderLayout.NORTH);

        TablePanel items = new TablePanel(new String[]{"Product","Qty","Unit Price","Subtotal"});
        if (sale.getItems()!=null)
            for (SaleItem i : sale.getItems())
                items.addRow(new Object[]{i.getProductName(),i.getQuantity(),
                    String.format("$%.2f",i.getUnitPrice()),String.format("$%.2f",i.getSubtotal())});
        panel.add(items, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        JPanel summary = new JPanel(new GridLayout(3,2,4,4));
        summary.setBackground(ThemeManager.card());
        addSummaryRow(summary,"Total:",String.format("$%.2f",sale.getTotalAmount()));
        addSummaryRow(summary,"Discount:",String.format("$%.2f",sale.getDiscount()));
        addSummaryRow(summary,"Grand Total:",String.format("$%.2f",sale.getGrandTotal()));
        bottom.add(summary, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        RoundedButton printBtn = new RoundedButton("📄 Print PDF");
        printBtn.addActionListener(e -> printInvoicePDF(sale));
        RoundedButton closeBtn = RoundedButton.secondary("Close");
        closeBtn.addActionListener(e -> d.dispose());
        actionRow.add(printBtn); actionRow.add(closeBtn);
        bottom.add(actionRow, BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);

        d.setContentPane(panel); d.setVisible(true);
    }

    private void exportSalesExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Sales to Excel");
        fc.setSelectedFile(new File("sales_history_export.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                List<Sale> sales = saleDAO.findAll();
                com.outrix.util.ExcelUtil.exportSales(sales, file);
                ActivityLogger.log("EXPORT_SALES", "Exported sales history to Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully exported sales history to Excel!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printInvoicePDF(Sale sale) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Invoice PDF");
        fc.setSelectedFile(new File(sale.getInvoiceNumber() + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                com.outrix.util.ReportGenerator.generateInvoice(file, sale);
                ActivityLogger.log("PRINT_INVOICE", "Printed invoice PDF: " + sale.getInvoiceNumber());
                JOptionPane.showMessageDialog(this, "✅ Invoice PDF successfully generated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating invoice PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addSummaryRow(JPanel p, String lbl, String val) {
        JLabel l = new JLabel(lbl); l.setFont(ThemeManager.FONT_BODY); l.setForeground(ThemeManager.textMuted()); p.add(l);
        JLabel v = new JLabel(val); v.setFont(ThemeManager.FONT_SUBHEAD); v.setForeground(ThemeManager.text()); p.add(v);
    }
 
    private void addRow(JPanel p,GridBagConstraints gbc,int row,String lbl,Component comp){
        gbc.gridx=0;gbc.gridy=row;gbc.gridwidth=1;gbc.weightx=0.4;
        JLabel l=new JLabel(lbl);l.setFont(ThemeManager.FONT_BODY);l.setForeground(ThemeManager.text());p.add(l,gbc);
        gbc.gridx=1;gbc.weightx=0.6;p.add(comp,gbc);
    }
}
