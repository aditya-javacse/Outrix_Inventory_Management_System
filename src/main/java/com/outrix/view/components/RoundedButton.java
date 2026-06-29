package com.outrix.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A modern rounded button with smooth hover and press animations.
 */
public class RoundedButton extends JButton {

    private Color normalBg;
    private Color hoverBg;
    private Color pressBg;
    private Color fgColor;
    private int   arcSize;
    private boolean isHovered  = false;
    private boolean isPressed  = false;
    private float   alpha      = 1.0f;

    // ── Constructors ──────────────────────────────────────────────────────────

    public RoundedButton(String text) {
        this(text, ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_BLUE_HOVER, Color.WHITE);
    }

    public RoundedButton(String text, Color bg, Color hoverBg, Color fg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg  = hoverBg;
        this.pressBg  = hoverBg.darker();
        this.fgColor  = fg;
        this.arcSize  = 10;
        initStyle();
    }

    /** Danger/red variant. */
    public static RoundedButton danger(String text) {
        return new RoundedButton(text, ThemeManager.ACCENT_RED,
                ThemeManager.ACCENT_RED.darker(), Color.WHITE);
    }

    /** Success/green variant. */
    public static RoundedButton success(String text) {
        return new RoundedButton(text, ThemeManager.ACCENT_GREEN,
                ThemeManager.ACCENT_GREEN.darker(), Color.WHITE);
    }

    /** Secondary (surface) variant. */
    public static RoundedButton secondary(String text) {
        return new RoundedButton(text, ThemeManager.card(),
                ThemeManager.hover(), ThemeManager.text());
    }

    /** Ghost (transparent with border) variant. */
    public static RoundedButton ghost(String text) {
        RoundedButton btn = new RoundedButton(text,
                new Color(0,0,0,0), ThemeManager.hover(), ThemeManager.text());
        btn.setBorderPainted(true);
        return btn;
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void initStyle() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(ThemeManager.FONT_SUBHEAD);
        setForeground(fgColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 36));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { isHovered = false; isPressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { isPressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ isPressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = isPressed ? pressBg : (isHovered ? hoverBg : normalBg);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arcSize, arcSize));

        // Subtle top highlight
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() / 2, arcSize, arcSize));

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void setBackground(Color bg) {
        this.normalBg = bg;
        this.hoverBg  = bg.darker();
        this.pressBg  = bg.darker().darker();
        repaint();
    }

    public void setColors(Color bg, Color hover, Color fg) {
        this.normalBg = bg; this.hoverBg = hover; this.pressBg = hover.darker();
        this.fgColor = fg; setForeground(fg); repaint();
    }
}
