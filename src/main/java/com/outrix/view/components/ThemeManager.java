package com.outrix.view.components;

import java.awt.*;

/**
 * Application-wide theme and color palette manager.
 * Supports Dark and Light modes via static color constants.
 */
public class ThemeManager {

    public enum Mode { DARK, LIGHT }

    private static Mode currentMode = Mode.DARK;

    // ── Dark Mode Palette ─────────────────────────────────────────────────────
    public static final Color DARK_BG           = new Color(0x0D1117);
    public static final Color DARK_SIDEBAR      = new Color(0x161B22);
    public static final Color DARK_CARD         = new Color(0x1C2128);
    public static final Color DARK_SURFACE      = new Color(0x21262D);
    public static final Color DARK_BORDER       = new Color(0x30363D);
    public static final Color DARK_TEXT         = new Color(0xE6EDF3);
    public static final Color DARK_TEXT_MUTED   = new Color(0x8B949E);
    public static final Color DARK_HOVER        = new Color(0x2D333B);

    // ── Light Mode Palette ────────────────────────────────────────────────────
    public static final Color LIGHT_BG          = new Color(0xF0F2F5);
    public static final Color LIGHT_SIDEBAR     = new Color(0xFFFFFF);
    public static final Color LIGHT_CARD        = new Color(0xFFFFFF);
    public static final Color LIGHT_SURFACE     = new Color(0xF6F8FA);
    public static final Color LIGHT_BORDER      = new Color(0xD0D7DE);
    public static final Color LIGHT_TEXT        = new Color(0x1F2328);
    public static final Color LIGHT_TEXT_MUTED  = new Color(0x656D76);
    public static final Color LIGHT_HOVER       = new Color(0xEAEEF2);

    // ── Accent colors (same in both modes) ───────────────────────────────────
    public static final Color ACCENT_BLUE       = new Color(0x2563EB);
    public static final Color ACCENT_BLUE_HOVER = new Color(0x1D4ED8);
    public static final Color ACCENT_GREEN      = new Color(0x10B981);
    public static final Color ACCENT_ORANGE     = new Color(0xF59E0B);
    public static final Color ACCENT_RED        = new Color(0xEF4444);
    public static final Color ACCENT_PURPLE     = new Color(0x8B5CF6);
    public static final Color ACCENT_CYAN       = new Color(0x06B6D4);
    public static final Color ACCENT_PINK       = new Color(0xEC4899);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 12);

    private static final java.util.List<Runnable> listeners = new java.util.ArrayList<>();

    public static Mode getMode()            { return currentMode; }
    public static boolean isDark()          { return currentMode == Mode.DARK; }

    public static void toggle() {
        currentMode = (currentMode == Mode.DARK) ? Mode.LIGHT : Mode.DARK;
        listeners.forEach(Runnable::run);
    }

    public static void addChangeListener(Runnable r) { listeners.add(r); }

    // ── Dynamic accessors ─────────────────────────────────────────────────────

    public static Color bg()        { return isDark() ? DARK_BG       : LIGHT_BG; }
    public static Color sidebar()   { return isDark() ? DARK_SIDEBAR   : LIGHT_SIDEBAR; }
    public static Color card()      { return isDark() ? DARK_CARD      : LIGHT_CARD; }
    public static Color surface()   { return isDark() ? DARK_SURFACE   : LIGHT_SURFACE; }
    public static Color border()    { return isDark() ? DARK_BORDER    : LIGHT_BORDER; }
    public static Color text()      { return isDark() ? DARK_TEXT      : LIGHT_TEXT; }
    public static Color textMuted() { return isDark() ? DARK_TEXT_MUTED: LIGHT_TEXT_MUTED; }
    public static Color hover()     { return isDark() ? DARK_HOVER     : LIGHT_HOVER; }
}
