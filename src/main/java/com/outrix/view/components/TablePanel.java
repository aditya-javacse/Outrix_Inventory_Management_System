package com.outrix.view.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Reusable panel containing a styled JTable with alternating row colors,
 * custom header rendering, and embedded scroll pane.
 */
public class TablePanel extends JPanel {

    private JTable       table;
    private DefaultTableModel model;

    public TablePanel(String[] columnNames) {
        setLayout(new BorderLayout());
        setOpaque(false);

        model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scroll, BorderLayout.CENTER);
    }

    private void styleTable() {
        table.setFont(ThemeManager.FONT_BODY);
        table.setForeground(ThemeManager.text());
        table.setBackground(ThemeManager.card());
        table.setSelectionBackground(ThemeManager.ACCENT_BLUE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom header renderer
        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeManager.FONT_SUBHEAD);
        header.setForeground(ThemeManager.textMuted());
        header.setBackground(ThemeManager.surface());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.border()));

        // Custom row renderer (alternating rows)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setFont(ThemeManager.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (isSelected) {
                    setBackground(ThemeManager.ACCENT_BLUE.darker());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? ThemeManager.card() : ThemeManager.surface());
                    setForeground(ThemeManager.text());
                }
                return this;
            }
        });
    }

    /** Clears all rows from the table. */
    public void clearRows() { model.setRowCount(0); }

    /** Adds a row to the table. */
    public void addRow(Object[] rowData) { model.addRow(rowData); }

    /** Returns the underlying JTable. */
    public JTable getTable() { return table; }

    /** Returns the table model. */
    public DefaultTableModel getModel() { return model; }

    /** Returns the selected row index (-1 if none). */
    public int getSelectedRow() { return table.getSelectedRow(); }

    /** Returns the value at the given row/col. */
    public Object getValueAt(int row, int col) { return model.getValueAt(row, col); }

    /** Returns row count. */
    public int getRowCount() { return model.getRowCount(); }
}
