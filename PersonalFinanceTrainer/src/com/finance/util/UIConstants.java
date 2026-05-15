package com.finance.util;

import java.awt.*;

public class UIConstants {

    // ─── Palette ─────────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(0x0F, 0x14, 0x23);
    public static final Color BG_CARD       = new Color(0x1A, 0x21, 0x35);
    public static final Color BG_CARD2      = new Color(0x1E, 0x27, 0x3E);
    public static final Color ACCENT_GREEN  = new Color(0x00, 0xD4, 0x8A);
    public static final Color ACCENT_BLUE   = new Color(0x4A, 0x9E, 0xFF);
    public static final Color ACCENT_PURPLE = new Color(0x8B, 0x5C, 0xF6);
    public static final Color ACCENT_ORANGE = new Color(0xFF, 0x7B, 0x2C);
    public static final Color ACCENT_RED    = new Color(0xFF, 0x4D, 0x4D);
    public static final Color TEXT_PRIMARY  = new Color(0xF0, 0xF4, 0xFF);
    public static final Color TEXT_MUTED    = new Color(0x7A, 0x8A, 0xAA);
    public static final Color BORDER        = new Color(0x2A, 0x35, 0x50);

    // ─── Fonts ───────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_NUM     = new Font("SansSerif", Font.BOLD,  18);
    public static final Font FONT_NUM_LG  = new Font("SansSerif", Font.BOLD,  26);

    // ─── Sizes ───────────────────────────────────────────────────────────────
    public static final int  SIDEBAR_W    = 210;
    public static final int  RADIUS       = 12;

    // ─── Helper: styled button ───────────────────────────────────────────────
    public static javax.swing.JButton primaryButton(String text, Color bg) {
        javax.swing.JButton btn = new javax.swing.JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SUBHEAD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    public static javax.swing.JButton ghostButton(String text) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MUTED);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static javax.swing.JLabel label(String text, Font font, Color color) {
        javax.swing.JLabel lbl = new javax.swing.JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }
}
