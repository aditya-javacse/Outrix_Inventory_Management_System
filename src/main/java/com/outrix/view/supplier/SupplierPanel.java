package com.outrix.view.supplier;

import com.outrix.config.DBConnection;
import com.outrix.dao.SupplierDAO;
import com.outrix.model.Supplier;
import com.outrix.util.ActivityLogger;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Supplier Management panel. */
public class SupplierPanel extends JPanel {

    private final SupplierDAO dao = new SupplierDAO();
    private TablePanel tablePanel;
    private SearchBar  searchBar;
    private List<Supplier> list;

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = new JLabel("🚚  Supplier Management");
        title.setFont(ThemeManager.FONT_TITLE); title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchBar = new SearchBar("Search suppliers...");
        searchBar.addSearchListener(kw -> filter(kw));
        actions.add(searchBar);

        RoundedButton importBtn = RoundedButton.secondary("📥 Import");
        importBtn.addActionListener(e -> importExcel());
        RoundedButton exportBtn = RoundedButton.secondary("📤 Export");
        exportBtn.addActionListener(e -> exportExcel());
        actions.add(importBtn);
        actions.add(exportBtn);

        RoundedButton addBtn = new RoundedButton("+ Add Supplier");
        addBtn.addActionListener(e -> openDialog(null));
        actions.add(addBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tablePanel = new TablePanel(new String[]{"ID","Name","Contact","Email","Address"});
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(ThemeManager.bg());
        wrap.setBorder(BorderFactory.createEmptyBorder(0,24,0,24));
        wrap.add(tablePanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        btnRow.setBackground(ThemeManager.bg());
        btnRow.setBorder(BorderFactory.createEmptyBorder(0,24,16,24));
        RoundedButton editBtn = RoundedButton.secondary("✏ Edit");
        RoundedButton delBtn  = RoundedButton.danger("🗑 Delete");
        editBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row,0);
            list.stream().filter(s -> s.getId()==id).findFirst().ifPresent(this::openDialog);
        });
        delBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row,0);
            String name = (String) tablePanel.getValueAt(row,1);
            if (JOptionPane.showConfirmDialog(this,"Delete \""+name+"\"?","Confirm",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { dao.delete(id); ActivityLogger.log("DELETE_SUPPLIER","Deleted: "+name); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage()); }
            }
        });
        RoundedButton historyBtn = RoundedButton.secondary("📊 Purchase History");
        historyBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row, 0);
            list.stream().filter(s -> s.getId()==id).findFirst().ifPresent(this::showPurchaseHistory);
        });
        btnRow.add(editBtn); btnRow.add(delBtn); btnRow.add(historyBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.bg());
        center.add(wrap, BorderLayout.CENTER);
        center.add(btnRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<List<Supplier>,Void> w = new SwingWorker<>() {
            @Override protected List<Supplier> doInBackground() throws Exception { return dao.findAll(); }
            @Override protected void done() {
                try { list = get(); populateTable(list); } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void filter(String kw) {
        if (list == null) return;
        List<Supplier> f = kw.isEmpty() ? list : list.stream()
                .filter(s -> s.getName().toLowerCase().contains(kw.toLowerCase()) ||
                        (s.getEmail()!=null && s.getEmail().toLowerCase().contains(kw.toLowerCase())))
                .toList();
        populateTable(f);
    }

    private void populateTable(List<Supplier> data) {
        tablePanel.clearRows();
        for (Supplier s : data)
            tablePanel.addRow(new Object[]{s.getId(), s.getName(), s.getContactNumber(), s.getEmail(), s.getAddress()});
    }

    private void openDialog(Supplier existing) {
        boolean isEdit = existing != null;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Supplier" : "Add Supplier", true);
        d.setSize(450, 320); d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card()); panel.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,5,5,5);

        JTextField nameF = sf(isEdit?existing.getName():"");
        JTextField phoneF= sf(isEdit&&existing.getContactNumber()!=null?existing.getContactNumber():"");
        JTextField emailF= sf(isEdit&&existing.getEmail()!=null?existing.getEmail():"");
        JTextField addrF = sf(isEdit&&existing.getAddress()!=null?existing.getAddress():"");

        addRow(panel,gbc,0,"Name *",nameF); addRow(panel,gbc,1,"Phone",phoneF);
        addRow(panel,gbc,2,"Email",emailF); addRow(panel,gbc,3,"Address",addrF);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(ThemeManager.card());
        RoundedButton save=new RoundedButton(isEdit?"Update":"Save"); RoundedButton cancel=RoundedButton.secondary("Cancel");
        cancel.addActionListener(e->d.dispose());
        save.addActionListener(e->{
            String nm=nameF.getText().trim(); if(nm.isEmpty()){JOptionPane.showMessageDialog(d,"Name required");return;}
            try{
                Supplier s = isEdit?existing:new Supplier();
                s.setName(nm); s.setContactNumber(phoneF.getText().trim());
                s.setEmail(emailF.getText().trim()); s.setAddress(addrF.getText().trim());
                if(isEdit){dao.update(s);ActivityLogger.log("UPDATE_SUPPLIER","Updated: "+nm);}
                else{dao.insert(s);ActivityLogger.log("ADD_SUPPLIER","Added: "+nm);}
                d.dispose(); loadData();
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx=0;gbc.gridy=4;gbc.gridwidth=2;panel.add(btns,gbc);
        d.setContentPane(panel); d.setVisible(true);
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Suppliers from Excel");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                List<Supplier> suppliers = com.outrix.util.ExcelUtil.importSuppliers(file);
                for (Supplier s : suppliers) {
                    dao.insert(s);
                }
                ActivityLogger.log("IMPORT_SUPPLIERS", "Imported " + suppliers.size() + " suppliers from Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully imported " + suppliers.size() + " suppliers!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error importing Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Suppliers to Excel");
        fc.setSelectedFile(new File("suppliers_export.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                com.outrix.util.ExcelUtil.exportSuppliers(list, file);
                ActivityLogger.log("EXPORT_SUPPLIERS", "Exported suppliers to Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully exported suppliers to Excel!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPurchaseHistory(Supplier s) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Purchase History – " + s.getName(), true);
        d.setSize(650, 450);
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeManager.card());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel("Products Supplied by " + s.getName());
        titleLbl.setFont(ThemeManager.FONT_HEADER);
        titleLbl.setForeground(ThemeManager.text());
        panel.add(titleLbl, BorderLayout.NORTH);

        TablePanel table = new TablePanel(new String[]{"Product Name", "Category", "Purchase Price", "Selling Price", "Quantity", "Total Value"});
        panel.add(table, BorderLayout.CENTER);

        // Fetch supplied products using direct JDBC
        List<Object[]> rows = new ArrayList<>();
        double totalValuation = 0;
        int totalQty = 0;

        String sql = "SELECT p.product_name, c.name AS category_name, p.purchase_price, p.selling_price, p.quantity " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.supplier_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("product_name");
                    String cat = rs.getString("category_name");
                    double pp = rs.getDouble("purchase_price");
                    double sp = rs.getDouble("selling_price");
                    int qty = rs.getInt("quantity");
                    double val = pp * qty;
                    totalValuation += val;
                    totalQty += qty;
                    rows.add(new Object[]{name, cat, pp, sp, qty, val});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (Object[] r : rows) {
            table.addRow(new Object[]{
                r[0], r[1],
                String.format("$%.2f", r[2]),
                String.format("$%.2f", r[3]),
                r[4],
                String.format("$%.2f", r[5])
            });
        }

        // Summary row at bottom
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel summaryLbl = new JLabel(String.format("Total Products Supplied: %d  |  Total Valuation: $%,.2f", totalQty, totalValuation));
        summaryLbl.setFont(ThemeManager.FONT_SUBHEAD);
        summaryLbl.setForeground(ThemeManager.text());
        
        RoundedButton closeBtn = RoundedButton.secondary("Close");
        closeBtn.addActionListener(e -> d.dispose());
        
        footer.add(summaryLbl, BorderLayout.WEST);
        footer.add(closeBtn, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);

        d.setContentPane(panel);
        d.setVisible(true);
    }

    private JTextField sf(String v){
        JTextField f=new JTextField(v); f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }
    private void addRow(JPanel p,GridBagConstraints gbc,int row,String lbl,Component comp){
        gbc.gridx=0;gbc.gridy=row;gbc.gridwidth=1;gbc.weightx=0.35;
        JLabel l=new JLabel(lbl);l.setFont(ThemeManager.FONT_BODY);l.setForeground(ThemeManager.text());p.add(l,gbc);
        gbc.gridx=1;gbc.weightx=0.65;p.add(comp,gbc);
    }
}
