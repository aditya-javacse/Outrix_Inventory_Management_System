package com.outrix.view.inventory;

import com.outrix.dao.InventoryDAO;
import com.outrix.dao.ProductDAO;
import com.outrix.model.InventoryLog;
import com.outrix.model.Product;
import com.outrix.util.ActivityLogger;
import com.outrix.util.SessionManager;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/** Inventory Management panel – stock in, out, adjustment, history. */
public class InventoryPanel extends JPanel {

    private final InventoryDAO invDAO  = new InventoryDAO();
    private final ProductDAO   prodDAO = new ProductDAO();
    private TablePanel logTable;
    private List<InventoryLog> logs;

    public InventoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = new JLabel("📊  Inventory Management");
        title.setFont(ThemeManager.FONT_TITLE); title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        RoundedButton stockInBtn  = new RoundedButton("📥 Stock In");
        stockInBtn.setColors(ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_GREEN.darker(), Color.WHITE);
        RoundedButton stockOutBtn = new RoundedButton("📤 Stock Out");
        stockOutBtn.setColors(ThemeManager.ACCENT_RED, ThemeManager.ACCENT_RED.darker(), Color.WHITE);
        RoundedButton adjustBtn   = new RoundedButton("⚖ Adjust");
        adjustBtn.setColors(ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_ORANGE.darker(), Color.WHITE);

        stockInBtn.addActionListener(e  -> openMovementDialog("STOCK_IN"));
        stockOutBtn.addActionListener(e -> openMovementDialog("STOCK_OUT"));
        adjustBtn.addActionListener(e   -> openMovementDialog("ADJUSTMENT"));
        
        RoundedButton exportBtn = RoundedButton.secondary("📤 Export Excel");
        exportBtn.addActionListener(e -> exportExcel());
        
        actions.add(stockInBtn); actions.add(stockOutBtn); actions.add(adjustBtn); actions.add(exportBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Log table
        logTable = new TablePanel(new String[]{"ID","Product","Type","Qty","Prev Qty","New Qty","Reference","Notes","Date","User"});
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(ThemeManager.bg()); wrap.setBorder(BorderFactory.createEmptyBorder(0,24,24,24));
        wrap.add(logTable, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<List<InventoryLog>,Void> w = new SwingWorker<>() {
            @Override protected List<InventoryLog> doInBackground() throws Exception { return invDAO.findAll(); }
            @Override protected void done() {
                try {
                    logs = get(); logTable.clearRows();
                    for (InventoryLog l : logs)
                        logTable.addRow(new Object[]{
                            l.getId(), l.getProductName(), l.getMovementType(),
                            l.getQuantity(), l.getPreviousQty(), l.getNewQty(),
                            l.getReference()!=null?l.getReference():"–",
                            l.getNotes()!=null?l.getNotes():"–",
                            l.getCreatedAt()!=null?l.getCreatedAt().toString().substring(0,16):"–",
                            l.getUsername()!=null?l.getUsername():"–"
                        });
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void openMovementDialog(String movementType) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                movementType.replace("_"," "), true);
        d.setSize(450, 360); d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card()); panel.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(6,6,6,6);

        // Product selector
        JComboBox<String> productBox = new JComboBox<>();
        java.util.List<Product> products = new java.util.ArrayList<>();
        try { products = prodDAO.findAll(); products.forEach(p -> productBox.addItem(p.getProductName())); }
        catch (Exception ex) { ex.printStackTrace(); }
        final java.util.List<Product> productsFinal = products;

        JTextField qtyField  = sf("1");
        JTextField refField  = sf("");
        JTextField noteField = sf("");
        JLabel currentQtyLbl = new JLabel("Current Qty: –");
        currentQtyLbl.setFont(ThemeManager.FONT_SMALL); currentQtyLbl.setForeground(ThemeManager.textMuted());

        productBox.addActionListener(e -> {
            int idx = productBox.getSelectedIndex();
            if (idx >= 0 && idx < productsFinal.size())
                currentQtyLbl.setText("Current Qty: " + productsFinal.get(idx).getQuantity());
        });
        if (!productsFinal.isEmpty()) currentQtyLbl.setText("Current Qty: "+productsFinal.get(0).getQuantity());

        int r=0;
        addRow(panel,gbc,r++,"Product *",productBox);
        addRow(panel,gbc,r++,"",currentQtyLbl);
        addRow(panel,gbc,r++,"Quantity *",qtyField);
        addRow(panel,gbc,r++,"Reference",refField);
        addRow(panel,gbc,r++,"Notes",noteField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(ThemeManager.card());
        RoundedButton confirm = new RoundedButton("Confirm");
        confirm.setColors("STOCK_IN".equals(movementType)?ThemeManager.ACCENT_GREEN:
                "STOCK_OUT".equals(movementType)?ThemeManager.ACCENT_RED:ThemeManager.ACCENT_ORANGE,
                ThemeManager.ACCENT_BLUE.darker(), Color.WHITE);
        RoundedButton cancel = RoundedButton.secondary("Cancel"); cancel.addActionListener(e->d.dispose());

        final java.util.List<Product> pFinal = productsFinal;
        confirm.addActionListener(e -> {
            int idx = productBox.getSelectedIndex();
            if (idx < 0 || idx >= pFinal.size()) return;
            Product product = pFinal.get(idx);
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) { JOptionPane.showMessageDialog(d,"Quantity must be > 0"); return; }
                int prevQty = product.getQuantity();
                int newQty;
                if      ("STOCK_IN".equals(movementType))   newQty = prevQty + qty;
                else if ("STOCK_OUT".equals(movementType))  newQty = Math.max(0, prevQty - qty);
                else    newQty = qty; // ADJUSTMENT sets absolute value

                InventoryLog log = new InventoryLog();
                log.setProductId(product.getId()); log.setUserId(SessionManager.getCurrentUserId());
                log.setMovementType(movementType); log.setQuantity(qty);
                log.setPreviousQty(prevQty); log.setNewQty(newQty);
                log.setReference(refField.getText().trim()); log.setNotes(noteField.getText().trim());
                invDAO.recordMovement(log);
                ActivityLogger.log(movementType, "Product: "+product.getProductName()+", Qty: "+qty);
                d.dispose(); loadData();
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(d,"Invalid quantity");
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });
        btns.add(cancel); btns.add(confirm);
        gbc.gridx=0;gbc.gridy=r;gbc.gridwidth=2;panel.add(btns,gbc);
        d.setContentPane(panel); d.setVisible(true);
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Stock Log to Excel");
        fc.setSelectedFile(new File("inventory_log_export.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                com.outrix.util.ExcelUtil.exportInventoryLogs(logs, file);
                ActivityLogger.log("EXPORT_INVENTORY", "Exported stock logs to Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully exported stock logs to Excel!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTextField sf(String v){
        JTextField f=new JTextField(v); f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text()); f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }
    private void addRow(JPanel p,GridBagConstraints gbc,int row,String lbl,Component comp){
        gbc.gridx=0;gbc.gridy=row;gbc.gridwidth=1;gbc.weightx=0.4;
        JLabel l=new JLabel(lbl);l.setFont(ThemeManager.FONT_BODY);l.setForeground(ThemeManager.text());p.add(l,gbc);
        gbc.gridx=1;gbc.weightx=0.6;p.add(comp,gbc);
    }
}
