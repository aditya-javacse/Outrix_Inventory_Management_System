package com.outrix.view.components;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A styled search bar with placeholder text, rounded border, and icon.
 */
public class SearchBar extends JPanel {

    private JTextField field;
    private String     placeholder;

    public SearchBar(String placeholder) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(280, 36));
        setBorder(BorderFactory.createEmptyBorder());

        // Search icon label
        JLabel iconLabel = new JLabel("  🔍  ");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconLabel.setForeground(ThemeManager.textMuted());

        field = new JTextField();
        field.setFont(ThemeManager.FONT_BODY);
        field.setForeground(ThemeManager.text());
        field.setBackground(ThemeManager.surface());
        field.setCaretColor(ThemeManager.text());
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        field.setOpaque(false);

        // Placeholder behavior
        addPlaceholder();

        add(iconLabel, BorderLayout.WEST);
        add(field,     BorderLayout.CENTER);
    }

    private void addPlaceholder() {
        field.setText(placeholder);
        field.setForeground(ThemeManager.textMuted());
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeManager.text());
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ThemeManager.textMuted());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeManager.surface());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
        g2.setColor(ThemeManager.border());
        g2.setStroke(new BasicStroke(1));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 8, 8));
        g2.dispose();
        super.paintComponent(g);
    }

    /** Returns the actual search text (empty if placeholder is shown). */
    public String getSearchText() {
        String text = field.getText();
        return text.equals(placeholder) ? "" : text;
    }

    /** Registers a listener called on every keystroke. */
    public void addSearchListener(java.util.function.Consumer<String> onSearch) {
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                onSearch.accept(getSearchText());
            }
        });
    }

    /** Returns the underlying text field. */
    public JTextField getField() { return field; }
}
