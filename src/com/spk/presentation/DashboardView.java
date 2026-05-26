package com.spk.presentation;

import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.Theme;
import com.spk.repository.CriteriaRepository;
import com.spk.repository.ResultRepository;
import com.spk.repository.ScoreRepository;
import com.spk.repository.UserRepository;
import com.spk.repository.VendorRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.SQLException;
import java.util.Map;

public class DashboardView extends JPanel {

    public DashboardView() {
        setLayout(new BorderLayout(24, 24));
        setBackground(Theme.BG_PRIMARY);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Home  >  Dashboard");
        breadcrumb.setFont(Theme.FONT_REGULAR.deriveFont(12f));
        breadcrumb.setForeground(Theme.TEXT_MUTED);

        titleRow.add(title);
        titleRow.add(breadcrumb);

        JLabel subtitle = new JLabel("Ringkasan sistem pilihan vendor IT dalam tampilan analytics modern");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(5, 12, 10, 0));

        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.BORDER_COLOR);

        header.add(titleRow);
        header.add(subtitle);
        header.add(separator);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        try {
            CriteriaRepository cr = new CriteriaRepository();
            VendorRepository vr = new VendorRepository();
            ResultRepository rr = new ResultRepository();
            ScoreRepository sr = new ScoreRepository();
            UserRepository ur = new UserRepository();

            int criteriaCount = cr.count();
            int vendorCount = vr.count();
            int scoreCount = sr.countScores();
            int userCount = ur.findAll().size();
            Map<String, Integer> criteriaTypeCounts = cr.countByType();
            Map<String, Double> weightsByCriteria = rr.getWeightsByCriteria();

            // KPI Row
            JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            kpiRow.setOpaque(false);
            kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            kpiRow.add(createMetricCard("Total Kriteria", String.valueOf(criteriaCount), "◈", Theme.ACCENT_PRIMARY));
            kpiRow.add(createMetricCard("Total Vendor", String.valueOf(vendorCount), "◉", Theme.ACCENT_SECONDARY));
            kpiRow.add(createMetricCard("Total Skor", String.valueOf(scoreCount), "▣", Theme.ACCENT_WARNING));
            kpiRow.add(createMetricCard("Total User", String.valueOf(userCount), "☷", Theme.ACCENT_PRIMARY));

            contentContainer.add(kpiRow);
            contentContainer.add(Box.createRigidArea(new Dimension(0, 20)));

            // Charts Row
            JPanel chartsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            chartsRow.setOpaque(false);
            chartsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            chartsRow.add(createFlowUsageCard());
            chartsRow.add(createCriteriaTypeBarChartCard(criteriaTypeCounts));
            chartsRow.add(createWeightPieChartCard(weightsByCriteria));

            contentContainer.add(chartsRow);

        } catch (SQLException e) {
            JLabel errLabel = new JLabel("Error loading stats: " + e.getMessage());
            errLabel.setForeground(Theme.ACCENT_DANGER);
            contentContainer.add(errLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createMetricCard(String label, String value, String icon, Color iconColor) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.setPreferredSize(new Dimension(180, 130));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(Theme.FONT_TITLE.deriveFont(24f));
        iconLabel.setForeground(iconColor);

        JLabel nameLabel = new JLabel(label.toUpperCase());
        nameLabel.setFont(Theme.FONT_BOLD.deriveFont(10f));
        nameLabel.setForeground(Theme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_BOLD.deriveFont(28f));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);

        return card;
    }

    private JPanel createFlowUsageCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, 320));

        JLabel title = new JLabel("Flow Penggunaan");
        title.setFont(Theme.FONT_BOLD.deriveFont(16f));
        title.setForeground(Theme.ACCENT_PRIMARY);

        String[] activities = {
                "1. Login dengan akun valid",
                "2. Kelola kriteria dan vendor",
                "3. Hitung bobot AHP",
                "4. Jalankan TOPSIS untuk ranking"
        };

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        for (String activity : activities) {
            JLabel row = new JLabel(activity);
            row.setFont(Theme.FONT_REGULAR);
            row.setForeground(Theme.TEXT_SECONDARY);
            card.add(row);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return card;
    }

    private JPanel createCriteriaTypeBarChartCard(Map<String, Integer> data) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(320, 320));

        JLabel title = new JLabel("Jumlah Tipe Kriteria");
        title.setFont(Theme.FONT_BOLD.deriveFont(16f));
        title.setForeground(Theme.ACCENT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (data.isEmpty()) return;

                int max = data.values().stream().max(Integer::compareTo).orElse(1);
                int width = getWidth();
                int height = getHeight();
                int barWidth = 40;
                int spacing = 60;
                int startX = 50;

                g2.setColor(Theme.BORDER_COLOR);
                g2.drawLine(40, height - 30, width - 20, height - 30);
                
                int i = 0;
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    int barHeight = (int) (((double) entry.getValue() / max) * (height - 60));
                    int x = startX + (i * spacing);
                    int y = height - 30 - barHeight;

                    g2.setColor(Theme.ACCENT_PRIMARY);
                    g2.fillRoundRect(x, y, barWidth, barHeight, 5, 5);
                    
                    // Label
                    g2.setColor(Theme.TEXT_SECONDARY);
                    g2.setFont(Theme.FONT_REGULAR.deriveFont(10f));
                    g2.drawString(entry.getKey(), x, height - 15);
                    
                    // Value
                    g2.drawString(String.valueOf(entry.getValue()), x + 15, y - 5);
                    i++;
                }
            }
        };
        chartPanel.setOpaque(false);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createWeightPieChartCard(Map<String, Double> data) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(320, 320));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Bobot Kriteria");
        title.setFont(Theme.FONT_BOLD.deriveFont(16f));
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Distribusi bobot kriteria AHP");
        subtitle.setFont(Theme.FONT_REGULAR.deriveFont(12f));
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBox.add(title);
        titleBox.add(subtitle);
        card.add(titleBox, BorderLayout.NORTH);

        if (data.isEmpty()) {
            JLabel empty = new JLabel("Bobot AHP belum dihitung", SwingConstants.CENTER);
            empty.setForeground(Theme.TEXT_MUTED);
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 60;
                int x = (width - size) / 2;
                int y = (height - size) / 2;

                Color[] colors = {Theme.ACCENT_PRIMARY, Theme.ACCENT_SECONDARY, Theme.ACCENT_SUCCESS, Theme.ACCENT_WARNING, Theme.ACCENT_DANGER};
                
                double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
                double currentAngle = 0;
                int i = 0;

                // Draw pie
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double angle = (entry.getValue() / total) * 360;
                    g2.setColor(colors[i % colors.length]);
                    g2.fill(new Arc2D.Double(x, y, size, size, currentAngle, angle, Arc2D.PIE));
                    currentAngle += angle;
                    i++;
                }
                
                // Draw inner circle for donut chart look
                g2.setColor(Theme.BG_CARD);
                int innerSize = size / 2;
                g2.fillOval(x + (size - innerSize)/2, y + (size - innerSize)/2, innerSize, innerSize);
                
                // Legend
                int legendY = height - 20;
                int legendX = 10;
                i = 0;
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    g2.setColor(colors[i % colors.length]);
                    g2.fillRect(legendX, legendY, 10, 10);
                    g2.setColor(Theme.TEXT_SECONDARY);
                    g2.setFont(Theme.FONT_REGULAR.deriveFont(10f));
                    double pct = (entry.getValue() / total) * 100;
                    g2.drawString(entry.getKey() + String.format(" (%.1f%%)", pct), legendX + 15, legendY + 9);
                    legendX += 80;
                    if (legendX > width - 80) {
                        legendX = 10;
                        legendY += 15;
                    }
                    i++;
                }
            }
        };
        chartPanel.setOpaque(false);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    public void refresh() {
        buildUI();
    }
}
