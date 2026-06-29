package com.outrix.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A glassmorphism-inspired stats card for the dashboard.
 * Displays an icon, value, label, and optional trend indicator.
 */
public class StatsCard extends JPanel {

    private String  title;
    private String  value;
    private String  subtitle;
    private Color   accentColor;
    private String  iconText;     // Emoji or unicode icon

    private JLabel  valueLabel;
    private JLabel  subtitleLabel;

    public StatsCard(String title, String value, String iconText, Color accentColor) {
        this(title, value, iconText, accentColor, "");
    }

    public StatsCard(String title, String value, String iconText, Color accentColor, String subtitle) {
        this.title       = title;
        this.value       = value;
        this.iconText    = iconText;
        this.accentColor = accentColor;
        this.subtitle    = subtitle;

        setOpaque(false);
        setPreferredSize(new Dimension(200, 120));
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        buildUI();
    }

    private void buildUI() {
        // Icon + Title row
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_SMALL);
        titleLabel.setForeground(ThemeManager.textMuted());
        top.add(titleLabel, BorderLayout.CENTER);

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(accentColor);
        top.add(iconLabel, BorderLayout.EAST);

        // Value
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ThemeManager.text());

        // Subtitle
        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(ThemeManager.FONT_SMALL);
        subtitleLabel.setForeground(ThemeManager.textMuted());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(valueLabel,    BorderLayout.NORTH);
        bottom.add(subtitleLabel, BorderLayout.SOUTH);

        add(top,    BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        // Accent bottom bar
        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        accent.setOpaque(false);
        accent.setPreferredSize(new Dimension(0, 4));
        add(accent, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Card background
        g2.setColor(ThemeManager.card());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
        // Subtle border
        g2.setColor(ThemeManager.border());
        g2.setStroke(new BasicStroke(1));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14));
        // Accent left stripe
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
        g2.dispose();
        super.paintComponent(g);
    }

    /** Updates the value displayed on the card. */
    public void setValue(String newValue) {
        this.value = newValue;
        valueLabel.setText(newValue);
        repaint();
    }

    /** Updates the subtitle displayed on the card. */
    public void setSubtitleText(String text) {
        subtitleLabel.setText(text);
        repaint();
    }
}
