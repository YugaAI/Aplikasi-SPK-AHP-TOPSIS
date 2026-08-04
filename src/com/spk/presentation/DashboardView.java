package com.spk.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.Theme;
import com.spk.repository.CriteriaRepository;
import com.spk.repository.ResultRepository;
import com.spk.repository.ScoreRepository;
import com.spk.repository.UserRepository;
import com.spk.repository.VendorRepository;

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

        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.BORDER_COLOR);

        header.add(titleRow);
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

            kpiRow.add(createMetricCard("Total Kriteria", String.valueOf(criteriaCount), "", Theme.ACCENT_PRIMARY));
            kpiRow.add(createMetricCard("Total Vendor", String.valueOf(vendorCount), "", Theme.ACCENT_SECONDARY));
            kpiRow.add(createMetricCard("Total Skor", String.valueOf(scoreCount), "", Theme.ACCENT_WARNING));
            kpiRow.add(createMetricCard("Total User", String.valueOf(userCount), "", Theme.ACCENT_PRIMARY));

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
        card.setPreferredSize(new Dimension(260, 340));

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
        card.setPreferredSize(new Dimension(300, 340));

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
                int n = data.size();

                int leftMargin = 40;
                int rightMargin = 20;
                int bottomMargin = 40;
                int topMargin = 25;
                int chartWidth = Math.max(0, width - leftMargin - rightMargin);
                int slot = n > 0 ? chartWidth / n : chartWidth;
                int barWidth = Math.max(24, Math.min(50, (int) (slot * 0.5)));

                g2.setColor(Theme.BORDER_COLOR);
                g2.drawLine(leftMargin, height - bottomMargin, width - rightMargin, height - bottomMargin);

                int i = 0;
                FontMetrics fm = g2.getFontMetrics(Theme.FONT_REGULAR.deriveFont(10f));
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    int barHeight = (int) (((double) entry.getValue() / max) * (height - bottomMargin - topMargin));
                    int slotCenter = leftMargin + (i * slot) + slot / 2;
                    int x = slotCenter - barWidth / 2;
                    int y = height - bottomMargin - barHeight;

                    g2.setColor(Theme.ACCENT_PRIMARY);
                    g2.fillRoundRect(x, y, barWidth, barHeight, 5, 5);

                    // Label (centered under the bar)
                    g2.setColor(Theme.TEXT_SECONDARY);
                    g2.setFont(Theme.FONT_REGULAR.deriveFont(10f));
                    String label = entry.getKey();
                    int labelWidth = fm.stringWidth(label);
                    g2.drawString(label, slotCenter - labelWidth / 2, height - bottomMargin + 15);

                    // Value (centered above the bar)
                    String valueStr = String.valueOf(entry.getValue());
                    int valueWidth = fm.stringWidth(valueStr);
                    g2.drawString(valueStr, slotCenter - valueWidth / 2, y - 6);
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
        card.setPreferredSize(new Dimension(420, 340));

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

                Color[] colors = {Theme.ACCENT_PRIMARY, Theme.ACCENT_SECONDARY, Theme.ACCENT_SUCCESS, Theme.ACCENT_WARNING, Theme.ACCENT_DANGER};

                double total = data.values().stream().mapToDouble(Double::doubleValue).sum();

                Font legendFont = Theme.FONT_REGULAR.deriveFont(11f);
                g2.setFont(legendFont);
                FontMetrics legendFm = g2.getFontMetrics();
                int swatch = 10;
                int itemGap = 16;
                int lineHeight = 18;
                int legendPadding = 10;
                int availWidth = Math.max(80, width - 2 * legendPadding);

                List<String> labels = new ArrayList<>();
                List<Integer> itemWidths = new ArrayList<>();
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double pct = (entry.getValue() / total) * 100;
                    String label = entry.getKey() + String.format(" (%.1f%%)", pct);
                    labels.add(label);
                    itemWidths.add(swatch + 6 + legendFm.stringWidth(label));
                }

                List<List<Integer>> legendRows = new ArrayList<>();
                List<Integer> currentRow = new ArrayList<>();
                int rowWidth = 0;
                for (int idx = 0; idx < labels.size(); idx++) {
                    int w = itemWidths.get(idx);
                    if (!currentRow.isEmpty() && rowWidth + itemGap + w > availWidth) {
                        legendRows.add(currentRow);
                        currentRow = new ArrayList<>();
                        rowWidth = 0;
                    }
                    if (!currentRow.isEmpty()) rowWidth += itemGap;
                    currentRow.add(idx);
                    rowWidth += w;
                }
                if (!currentRow.isEmpty()) legendRows.add(currentRow);

                int legendHeight = legendRows.size() * lineHeight + legendPadding;

                int topPadding = 10;
                int pieAreaHeight = Math.max(60, height - legendHeight - topPadding);
                int size = Math.min(width - 40, pieAreaHeight);
                int x = (width - size) / 2;
                int y = topPadding + (pieAreaHeight - size) / 2;
                double cx = x + size / 2.0;
                double cy = y + size / 2.0;
                double outerRadius = size / 2.0;
                double innerRadius = outerRadius * 0.5;
                double labelRadius = (outerRadius + innerRadius) / 2.0;

                double currentAngle = 0;
                int i = 0;
                double[] midAngles = new double[data.size()];
                double[] sliceAngles = new double[data.size()];
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double angle = (entry.getValue() / total) * 360;
                    g2.setColor(colors[i % colors.length]);
                    g2.fill(new Arc2D.Double(x, y, size, size, currentAngle, angle, Arc2D.PIE));
                    midAngles[i] = currentAngle + angle / 2.0;
                    sliceAngles[i] = angle;
                    currentAngle += angle;
                    i++;
                }

                g2.setColor(Theme.BG_CARD);
                int innerSize = (int) (innerRadius * 2);
                g2.fillOval((int) (cx - innerRadius), (int) (cy - innerRadius), innerSize, innerSize);

                Font pctFont = Theme.FONT_BOLD.deriveFont(11f);
                g2.setFont(pctFont);
                FontMetrics pctFm = g2.getFontMetrics();
                i = 0;
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double pct = (entry.getValue() / total) * 100;
                    if (sliceAngles[i] >= 12) {
                        String pctLabel = String.format("%.0f%%", pct);
                        double rad = Math.toRadians(midAngles[i]);
                        double lx = cx + labelRadius * Math.cos(rad);
                        double ly = cy - labelRadius * Math.sin(rad);
                        int lw = pctFm.stringWidth(pctLabel);
                        g2.setColor(Theme.TEXT_ON_ACCENT);
                        g2.drawString(pctLabel, (float) (lx - lw / 2.0), (float) (ly + pctFm.getAscent() / 2.5));
                    }
                    i++;
                }

                g2.setFont(legendFont);
                int legendY = height - legendRows.size() * lineHeight;
                for (List<Integer> row : legendRows) {
                    int legendX = legendPadding;
                    for (int idx : row) {
                        g2.setColor(colors[idx % colors.length]);
                        g2.fillRect(legendX, legendY + 4, swatch, swatch);
                        g2.setColor(Theme.TEXT_SECONDARY);
                        g2.drawString(labels.get(idx), legendX + swatch + 6, legendY + swatch + 2);
                        legendX += itemWidths.get(idx) + itemGap;
                    }
                    legendY += lineHeight;
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
