package com.spk.presentation.components;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {
    
    public CardPanel() {
        setOpaque(false);
        setBackground(Theme.BG_CARD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 15));
        g2.fillRoundRect(3, 5, width - 6, height - 6, 20, 20);
        
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 2, height - 2, 20, 20);

        g2.setColor(Theme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, width - 2, height - 2, 20, 20);

        g2.dispose();
        super.paintComponent(g);
    }
}
