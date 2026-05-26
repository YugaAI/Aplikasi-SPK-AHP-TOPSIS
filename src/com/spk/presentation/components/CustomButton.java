package com.spk.presentation.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton extends JButton {

    private boolean isHovered = false;
    private Color normalBg = Theme.BG_CARD;
    private Color hoverBg = Theme.BG_CARD_HOVER;
    private Color textColor = Theme.TEXT_PRIMARY;
    private boolean isPrimary = false;

    public CustomButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(Theme.FONT_REGULAR);
        setForeground(textColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setPrimary() {
        this.isPrimary = true;
        this.normalBg = Theme.ACCENT_PRIMARY;
        this.hoverBg = Theme.ACCENT_PRIMARY.brighter();
        this.textColor = Theme.TEXT_ON_ACCENT;
        setForeground(textColor);
        setFont(Theme.FONT_BOLD);
    }
    
    public void setDanger() {
        this.normalBg = Theme.ACCENT_DANGER;
        this.hoverBg = Theme.ACCENT_DANGER.brighter();
        this.textColor = Theme.TEXT_ON_ACCENT;
        setForeground(textColor);
        setFont(Theme.FONT_BOLD);
    }

    public void setSuccess() {
        this.normalBg = Theme.ACCENT_SUCCESS;
        this.hoverBg = Theme.ACCENT_SUCCESS.brighter();
        this.textColor = Theme.TEXT_ON_ACCENT;
        setForeground(textColor);
        setFont(Theme.FONT_BOLD);
    }

    public void setWarning() {
        this.normalBg = Theme.ACCENT_WARNING;
        this.hoverBg = Theme.ACCENT_WARNING.brighter();
        this.textColor = Theme.TEXT_ON_ACCENT;
        setForeground(textColor);
        setFont(Theme.FONT_BOLD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Shadow simple implementation
        if (isPrimary && !isHovered) {
            g2.setColor(new Color(Theme.ACCENT_PRIMARY.getRed(), Theme.ACCENT_PRIMARY.getGreen(), Theme.ACCENT_PRIMARY.getBlue(), 60));
            g2.fillRoundRect(2, 4, width - 4, height - 4, 15, 15);
        }

        if (isHovered) {
            g2.setColor(hoverBg);
        } else {
            g2.setColor(normalBg);
        }

        g2.fillRoundRect(0, 0, width, height - (isPrimary && !isHovered ? 2 : 0), 10, 10);
        
        if (!isPrimary) {
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
