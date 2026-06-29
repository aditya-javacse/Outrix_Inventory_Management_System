package com.outrix.view.employee;

import com.outrix.dao.EmployeeDAO;
import com.outrix.dao.UserDAO;
import com.outrix.model.Employee;
import com.outrix.model.User;
import com.outrix.util.ActivityLogger;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

/** Employee Management panel (Admin only). */
public class EmployeePanel extends JPanel {

    private final EmployeeDAO empDAO  = new EmployeeDAO();
    private final UserDAO     userDAO = new UserDAO();
    private TablePanel tablePanel;
    private SearchBar  searchBar;
    private List<Employee> list;

    public EmployeePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = new JLabel("👥  Employee Management");
        title.setFont(ThemeManager.FONT_TITLE); title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchBar = new SearchBar("Search employees...");
        searchBar.addSearchListener(kw -> filter(kw));
        actions.add(searchBar);
        RoundedButton addBtn = new RoundedButton("+ Add Employee");
        addBtn.addActionListener(e -> openDialog(null));
        actions.add(addBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tablePanel = new TablePanel(new String[]{"ID","Name","Email","Phone","Role","Username","System Role","Hire Date"});
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
            list.stream().filter(emp -> emp.getId()==id).findFirst().ifPresent(this::openDialog);
        });
        delBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row,0);
            String nm = (String) tablePanel.getValueAt(row,1);
            if (JOptionPane.showConfirmDialog(this,"Delete \""+nm+"\"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                try { empDAO.delete(id); ActivityLogger.log("DELETE_EMPLOYEE","Deleted: "+nm); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage()); }
            }
        });
        btnRow.add(editBtn); btnRow.add(delBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.bg());
        center.add(wrap, BorderLayout.CENTER);
        center.add(btnRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<List<Employee>,Void> w = new SwingWorker<>() {
            @Override protected List<Employee> doInBackground() throws Exception { return empDAO.findAll(); }
            @Override protected void done() {
                try { list = get(); populateTable(list); } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void filter(String kw) {
        if (list == null) return;
        List<Employee> f = kw.isEmpty() ? list : list.stream()
                .filter(e -> e.getName().toLowerCase().contains(kw.toLowerCase()) ||
                        (e.getEmail()!=null && e.getEmail().toLowerCase().contains(kw.toLowerCase())))
                .toList();
        populateTable(f);
    }

    private void populateTable(List<Employee> data) {
        tablePanel.clearRows();
        for (Employee e : data)
            tablePanel.addRow(new Object[]{e.getId(), e.getName(), e.getEmail(), e.getPhone(), e.getRole(),
                e.getUsername()!=null?e.getUsername():"–", e.getUserRole()!=null?e.getUserRole():"–",
                e.getHireDate()!=null?e.getHireDate().toString():"–"});
    }

    private void openDialog(Employee existing) {
        boolean isEdit = existing != null;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit?"Edit Employee":"Add Employee", true);
        d.setSize(480, 460); d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card()); panel.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,5,5,5);

        JTextField nameF     = sf(isEdit?existing.getName():"");
        JTextField emailF    = sf(isEdit&&existing.getEmail()!=null?existing.getEmail():"");
        JTextField phoneF    = sf(isEdit&&existing.getPhone()!=null?existing.getPhone():"");
        JTextField roleF     = sf(isEdit&&existing.getRole()!=null?existing.getRole():"");
        JTextField hireDateF = sf(isEdit&&existing.getHireDate()!=null?existing.getHireDate().toString():java.time.LocalDate.now().toString());
        // Login fields (only for new employees)
        JTextField usernameF = sf("");
        JPasswordField passF = new JPasswordField();
        stylePass(passF);
        JComboBox<String> sysRoleBox = new JComboBox<>(new String[]{"EMPLOYEE","ADMIN"});

        int r=0;
        addRow(panel,gbc,r++,"Name *",nameF);
        addRow(panel,gbc,r++,"Email",emailF);
        addRow(panel,gbc,r++,"Phone",phoneF);
        addRow(panel,gbc,r++,"Job Role",roleF);
        addRow(panel,gbc,r++,"Hire Date (YYYY-MM-DD)",hireDateF);
        if (!isEdit) {
            addRow(panel,gbc,r++,"Username *",usernameF);
            addRow(panel,gbc,r++,"Password *",passF);
            addRow(panel,gbc,r++,"System Role",sysRoleBox);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(ThemeManager.card());
        RoundedButton save = new RoundedButton(isEdit?"Update":"Save");
        RoundedButton cancel = RoundedButton.secondary("Cancel");
        cancel.addActionListener(e->d.dispose());
        save.addActionListener(e->{
            String nm = nameF.getText().trim(); if(nm.isEmpty()){JOptionPane.showMessageDialog(d,"Name required");return;}
            try {
                Employee emp = isEdit?existing:new Employee();
                emp.setName(nm); emp.setEmail(emailF.getText().trim());
                emp.setPhone(phoneF.getText().trim()); emp.setRole(roleF.getText().trim());
                String hd = hireDateF.getText().trim();
                try { emp.setHireDate(Date.valueOf(hd)); } catch (Exception ignored) {}
                if (!isEdit) {
                    // Create user account first
                    String uname = usernameF.getText().trim();
                    String pass  = new String(passF.getPassword());
                    if (uname.isEmpty()||pass.isEmpty()) { JOptionPane.showMessageDialog(d,"Username and password required"); return; }
                    User u = new User(); u.setUsername(uname); u.setPassword(pass);
                    u.setRole((String)sysRoleBox.getSelectedItem());
                    u.setFullName(nm); u.setEmail(emailF.getText().trim()); u.setActive(true);
                    int uid = userDAO.insert(u); emp.setUserId(uid);
                    empDAO.insert(emp); ActivityLogger.log("ADD_EMPLOYEE","Added: "+nm);
                } else {
                    empDAO.update(emp); ActivityLogger.log("UPDATE_EMPLOYEE","Updated: "+nm);
                }
                d.dispose(); loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx=0;gbc.gridy=r;gbc.gridwidth=2;panel.add(btns,gbc);
        d.setContentPane(panel); d.setVisible(true);
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
    private void stylePass(JPasswordField f){
        f.setFont(ThemeManager.FONT_BODY); f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
    }
    private void addRow(JPanel p,GridBagConstraints gbc,int row,String lbl,Component comp){
        gbc.gridx=0;gbc.gridy=row;gbc.gridwidth=1;gbc.weightx=0.4;
        JLabel l=new JLabel(lbl);l.setFont(ThemeManager.FONT_BODY);l.setForeground(ThemeManager.text());p.add(l,gbc);
        gbc.gridx=1;gbc.weightx=0.6;p.add(comp,gbc);
    }
}
