package com.outrix.view.customer;

import com.outrix.config.DBConnection;
import com.outrix.dao.CustomerDAO;
import com.outrix.model.Customer;
import com.outrix.util.ActivityLogger;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Customer Management panel. */
public class CustomerPanel extends JPanel {

    private final CustomerDAO dao = new CustomerDAO();
    private TablePanel tablePanel;
    private SearchBar  searchBar;
    private List<Customer> list;

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = new JLabel("👤  Customer Management");
        title.setFont(ThemeManager.FONT_TITLE); title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchBar = new SearchBar("Search customers...");
        searchBar.addSearchListener(kw -> filter(kw));
        actions.add(searchBar);

        RoundedButton importBtn = RoundedButton.secondary("📥 Import");
        importBtn.addActionListener(e -> importExcel());
        RoundedButton exportBtn = RoundedButton.secondary("📤 Export");
        exportBtn.addActionListener(e -> exportExcel());
        actions.add(importBtn);
        actions.add(exportBtn);

        RoundedButton addBtn = new RoundedButton("+ Add Customer");
        addBtn.addActionListener(e -> openDialog(null));
        actions.add(addBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tablePanel = new TablePanel(new String[]{"ID","Name","Phone","Email","Address","Joined"});
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(ThemeManager.bg()); wrap.setBorder(BorderFactory.createEmptyBorder(0,24,0,24));
        wrap.add(tablePanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        btnRow.setBackground(ThemeManager.bg()); btnRow.setBorder(BorderFactory.createEmptyBorder(0,24,16,24));
        RoundedButton editBtn = RoundedButton.secondary("✏ Edit");
        RoundedButton delBtn  = RoundedButton.danger("🗑 Delete");
        editBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row<0) return;
            int id = (int) tablePanel.getValueAt(row,0);
            list.stream().filter(c->c.getId()==id).findFirst().ifPresent(this::openDialog);
        });
        delBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row<0) return;
            int id = (int) tablePanel.getValueAt(row,0);
            String nm = (String) tablePanel.getValueAt(row,1);
            if (JOptionPane.showConfirmDialog(this,"Delete \""+nm+"\"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                try { dao.delete(id); ActivityLogger.log("DELETE_CUSTOMER","Deleted: "+nm); loadData(); }
                catch(Exception ex){ JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage()); }
            }
        });
        RoundedButton historyBtn = RoundedButton.secondary("🧾 Purchase History");
        historyBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row, 0);
            list.stream().filter(c -> c.getId()==id).findFirst().ifPresent(this::showPurchaseHistory);
        });
        btnRow.add(editBtn); btnRow.add(delBtn); btnRow.add(historyBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.bg());
        center.add(wrap, BorderLayout.CENTER); center.add(btnRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<List<Customer>,Void> w = new SwingWorker<>() {
            @Override protected List<Customer> doInBackground() throws Exception { return dao.findAll(); }
            @Override protected void done() {
                try { list=get(); populateTable(list); } catch(Exception ex){ ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void filter(String kw) {
        if (list==null) return;
        List<Customer> f = kw.isEmpty() ? list : list.stream()
                .filter(c->c.getName().toLowerCase().contains(kw.toLowerCase())||
                        (c.getPhone()!=null&&c.getPhone().contains(kw))||
                        (c.getEmail()!=null&&c.getEmail().toLowerCase().contains(kw.toLowerCase())))
                .toList();
        populateTable(f);
    }

    private void populateTable(List<Customer> data) {
        tablePanel.clearRows();
        for (Customer c : data)
            tablePanel.addRow(new Object[]{c.getId(),c.getName(),c.getPhone()!=null?c.getPhone():"–",
                c.getEmail()!=null?c.getEmail():"–", c.getAddress()!=null?c.getAddress():"–",
                c.getCreatedAt()!=null?c.getCreatedAt().toString().substring(0,10):"–"});
    }

    private void openDialog(Customer existing) {
        boolean isEdit = existing!=null;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),isEdit?"Edit Customer":"Add Customer",true);
        d.setSize(430,310); d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card()); panel.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(5,5,5,5);

        JTextField nameF = sf(isEdit?existing.getName():"");
        JTextField phoneF= sf(isEdit&&existing.getPhone()!=null?existing.getPhone():"");
        JTextField emailF= sf(isEdit&&existing.getEmail()!=null?existing.getEmail():"");
        JTextField addrF = sf(isEdit&&existing.getAddress()!=null?existing.getAddress():"");

        addRow(panel,gbc,0,"Name *",nameF); addRow(panel,gbc,1,"Phone",phoneF);
        addRow(panel,gbc,2,"Email",emailF); addRow(panel,gbc,3,"Address",addrF);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(ThemeManager.card());
        RoundedButton save=new RoundedButton(isEdit?"Update":"Save");
        RoundedButton cancel=RoundedButton.secondary("Cancel"); cancel.addActionListener(e->d.dispose());
        save.addActionListener(e->{
            String nm=nameF.getText().trim(); if(nm.isEmpty()){JOptionPane.showMessageDialog(d,"Name required");return;}
            try {
                Customer c=isEdit?existing:new Customer();
                c.setName(nm);c.setPhone(phoneF.getText().trim());c.setEmail(emailF.getText().trim());c.setAddress(addrF.getText().trim());
                if(isEdit){dao.update(c);ActivityLogger.log("UPDATE_CUSTOMER","Updated: "+nm);}
                else{dao.insert(c);ActivityLogger.log("ADD_CUSTOMER","Added: "+nm);}
                d.dispose(); loadData();
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx=0;gbc.gridy=4;gbc.gridwidth=2;panel.add(btns,gbc);
        d.setContentPane(panel); d.setVisible(true);
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Customers from Excel");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                List<Customer> customers = com.outrix.util.ExcelUtil.importCustomers(file);
                for (Customer c : customers) {
                    dao.insert(c);
                }
                ActivityLogger.log("IMPORT_CUSTOMERS", "Imported " + customers.size() + " customers from Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully imported " + customers.size() + " customers!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error importing Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Customers to Excel");
        fc.setSelectedFile(new File("customers_export.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                com.outrix.util.ExcelUtil.exportCustomers(list, file);
                ActivityLogger.log("EXPORT_CUSTOMERS", "Exported customers to Excel: " + file.getName());
                JOptionPane.showMessageDialog(this, "✅ Successfully exported customers to Excel!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPurchaseHistory(Customer c) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Purchase History – " + c.getName(), true);
        d.setSize(750, 500);
        d.setLocationRelativeTo(this);
        
        JPanel root = new JPanel(new GridLayout(2, 1, 0, 15));
        root.setBackground(ThemeManager.card());
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Master panel: invoices
        JPanel master = new JPanel(new BorderLayout(0, 5));
        master.setOpaque(false);
        JLabel masterTitle = new JLabel("Invoices Created for " + c.getName());
        masterTitle.setFont(ThemeManager.FONT_HEADER);
        masterTitle.setForeground(ThemeManager.text());
        master.add(masterTitle, BorderLayout.NORTH);

        TablePanel masterTable = new TablePanel(new String[]{"Sale ID", "Invoice #", "Payment Method", "Subtotal", "Discount", "Grand Total", "Date"});
        master.add(masterTable, BorderLayout.CENTER);
        root.add(master);

        // Detail panel: items in the invoice
        JPanel detail = new JPanel(new BorderLayout(0, 5));
        detail.setOpaque(false);
        JLabel detailTitle = new JLabel("Invoice Items (Select an invoice above)");
        detailTitle.setFont(ThemeManager.FONT_HEADER);
        detailTitle.setForeground(ThemeManager.text());
        detail.add(detailTitle, BorderLayout.NORTH);

        TablePanel detailTable = new TablePanel(new String[]{"Product Name", "Quantity", "Unit Price", "Subtotal"});
        detail.add(detailTable, BorderLayout.CENTER);
        root.add(detail);

        // Selection listener to update detail panel
        masterTable.getTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = masterTable.getSelectedRow();
                if (row >= 0) {
                    int saleId = (int) masterTable.getValueAt(row, 0);
                    detailTitle.setText("Invoice Items for Invoice: " + masterTable.getValueAt(row, 1));
                    loadInvoiceItems(saleId, detailTable);
                }
            }
        });

        // Load master data
        double totalSalesAmount = 0;
        int invoiceCount = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, invoice_number, payment_method, total_amount, discount, grand_total, sale_date " +
                     "FROM sales WHERE customer_id=? ORDER BY sale_date DESC")) {
            ps.setInt(1, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String invNum = rs.getString("invoice_number");
                    String pay = rs.getString("payment_method");
                    double sub = rs.getDouble("total_amount");
                    double disc = rs.getDouble("discount");
                    double grand = rs.getDouble("grand_total");
                    Timestamp date = rs.getTimestamp("sale_date");
                    
                    totalSalesAmount += grand;
                    invoiceCount++;

                    masterTable.addRow(new Object[]{
                        id, invNum, pay,
                        String.format("$%.2f", sub),
                        String.format("$%.2f", disc),
                        String.format("$%.2f", grand),
                        date.toString().substring(0, 16)
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Main layout wrapping root and close button
        JPanel mainWrap = new JPanel(new BorderLayout(0, 10));
        mainWrap.setBackground(ThemeManager.card());
        mainWrap.add(root, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 24, 15, 24));
        JLabel summaryLbl = new JLabel(String.format("Total Invoices: %d  |  Total Purchases: $%,.2f", invoiceCount, totalSalesAmount));
        summaryLbl.setFont(ThemeManager.FONT_SUBHEAD);
        summaryLbl.setForeground(ThemeManager.text());
        
        RoundedButton closeBtn = RoundedButton.secondary("Close");
        closeBtn.addActionListener(e -> d.dispose());
        
        footer.add(summaryLbl, BorderLayout.WEST);
        footer.add(closeBtn, BorderLayout.EAST);
        mainWrap.add(footer, BorderLayout.SOUTH);

        d.setContentPane(mainWrap);
        d.setVisible(true);
    }

    private void loadInvoiceItems(int saleId, TablePanel detailTable) {
        detailTable.clearRows();
        String sql = "SELECT si.quantity, si.unit_price, si.subtotal, p.product_name " +
                     "FROM sale_items si LEFT JOIN products p ON si.product_id=p.id WHERE si.sale_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pName = rs.getString("product_name");
                    int qty = rs.getInt("quantity");
                    double up = rs.getDouble("unit_price");
                    double sub = rs.getDouble("subtotal");
                    detailTable.addRow(new Object[]{
                        pName != null ? pName : "Unknown",
                        qty,
                        String.format("$%.2f", up),
                        String.format("$%.2f", sub)
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JTextField sf(String v){
        JTextField f=new JTextField(v);f.setFont(ThemeManager.FONT_BODY);
        f.setBackground(ThemeManager.surface());f.setForeground(ThemeManager.text());f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }
    private void addRow(JPanel p,GridBagConstraints gbc,int row,String lbl,Component comp){
        gbc.gridx=0;gbc.gridy=row;gbc.gridwidth=1;gbc.weightx=0.35;
        JLabel l=new JLabel(lbl);l.setFont(ThemeManager.FONT_BODY);l.setForeground(ThemeManager.text());p.add(l,gbc);
        gbc.gridx=1;gbc.weightx=0.65;p.add(comp,gbc);
    }
}
