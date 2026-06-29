package com.outrix.view.category;

import com.outrix.dao.CategoryDAO;
import com.outrix.model.Category;
import com.outrix.util.ActivityLogger;
import com.outrix.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Category Management panel. */
public class CategoryPanel extends JPanel {

    private final CategoryDAO dao = new CategoryDAO();
    private TablePanel tablePanel;
    private List<Category> list;

    public CategoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.bg());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(ThemeManager.bg());
        top.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JLabel title = new JLabel("🏷  Category Management");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.text());
        top.add(title, BorderLayout.WEST);

        RoundedButton addBtn = new RoundedButton("+ Add Category");
        addBtn.addActionListener(e -> openDialog(null));
        top.add(addBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tablePanel = new TablePanel(new String[]{"ID", "Name", "Description", "Created At"});
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(ThemeManager.bg());
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        wrap.add(tablePanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(ThemeManager.bg());
        btnRow.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        RoundedButton editBtn   = RoundedButton.secondary("✏ Edit");
        RoundedButton deleteBtn = RoundedButton.danger("🗑 Delete");
        editBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row, 0);
            list.stream().filter(c -> c.getId() == id).findFirst().ifPresent(this::openDialog);
        });
        deleteBtn.addActionListener(e -> {
            int row = tablePanel.getSelectedRow(); if (row < 0) return;
            int id = (int) tablePanel.getValueAt(row, 0);
            String name = (String) tablePanel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete category \""+name+"\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try { dao.delete(id); ActivityLogger.log("DELETE_CATEGORY","Deleted: "+name); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Cannot delete: "+ex.getMessage()); }
            }
        });
        btnRow.add(editBtn); btnRow.add(deleteBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.bg());
        center.add(wrap, BorderLayout.CENTER);
        center.add(btnRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<List<Category>, Void> w = new SwingWorker<>() {
            @Override protected List<Category> doInBackground() throws Exception { return dao.findAll(); }
            @Override protected void done() {
                try {
                    list = get(); tablePanel.clearRows();
                    for (Category c : list)
                        tablePanel.addRow(new Object[]{c.getId(), c.getName(), c.getDescription(),
                            c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0,19) : ""});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        w.execute();
    }

    private void openDialog(Category existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Category" : "Add Category", true);
        dialog.setSize(400, 250); dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.card());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6,6,6,6); gbc.weightx = 1;

        JTextField nameField = styledField(isEdit ? existing.getName() : "");
        JTextField descField = styledField(isEdit ? (existing.getDescription() != null ? existing.getDescription() : "") : "");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.35;
        panel.add(label("Name *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; panel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35; panel.add(label("Description"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; panel.add(descField, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(ThemeManager.card());
        RoundedButton save = new RoundedButton(isEdit ? "Update" : "Save");
        RoundedButton cancel = RoundedButton.secondary("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String nm = nameField.getText().trim();
            if (nm.isEmpty()) { JOptionPane.showMessageDialog(dialog,"Name required"); return; }
            try {
                Category c = isEdit ? existing : new Category();
                c.setName(nm); c.setDescription(descField.getText().trim());
                if (isEdit) { dao.update(c); ActivityLogger.log("UPDATE_CATEGORY","Updated: "+nm); }
                else        { dao.insert(c); ActivityLogger.log("ADD_CATEGORY","Added: "+nm); }
                dialog.dispose(); loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog,"Error: "+ex.getMessage()); }
        });
        btns.add(cancel); btns.add(save);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btns, gbc);
        dialog.setContentPane(panel); dialog.setVisible(true);
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(ThemeManager.FONT_BODY); f.setBackground(ThemeManager.surface()); f.setForeground(ThemeManager.text());
        f.setCaretColor(ThemeManager.text());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border(),1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }
    private JLabel label(String t) {
        JLabel l = new JLabel(t); l.setFont(ThemeManager.FONT_BODY); l.setForeground(ThemeManager.text()); return l;
    }
}
