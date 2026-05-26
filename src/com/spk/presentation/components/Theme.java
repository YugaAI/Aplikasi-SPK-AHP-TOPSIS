package com.spk.presentation.components;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    // Backgrounds
    public static final Color BG_PRIMARY = Color.decode("#f5f7fb");
    public static final Color BG_SECONDARY = Color.decode("#eef3fb");
    public static final Color BG_CARD = Color.decode("#ffffff");
    public static final Color BG_CARD_HOVER = Color.decode("#f4f7ff");
    public static final Color BG_INPUT = Color.decode("#f5f8ff");
    public static final Color BG_SIDEBAR = Color.decode("#0f2248");

    // Accents
    public static final Color ACCENT_PRIMARY = Color.decode("#1e90ff");
    public static final Color ACCENT_SECONDARY = Color.decode("#6f79f0");
    public static final Color ACCENT_SUCCESS = Color.decode("#56b52e");
    public static final Color ACCENT_WARNING = Color.decode("#fc973f");
    public static final Color ACCENT_DANGER = Color.decode("#dc2626");

    // Text
    public static final Color TEXT_PRIMARY = Color.decode("#1f2937");
    public static final Color TEXT_SECONDARY = Color.decode("#475569");
    public static final Color TEXT_MUTED = Color.decode("#64748b");
    public static final Color TEXT_ON_ACCENT = Color.decode("#ffffff");

    // Borders
    public static final Color BORDER_COLOR = new Color(148, 163, 184, 40); // roughly 0.16 alpha
    
    // Fonts
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
}
