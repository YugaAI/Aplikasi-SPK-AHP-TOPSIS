package com.spk.presentation;

import com.spk.domain.TOPSISResult;
import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.repository.ResultRepository;
import com.spk.usecase.AuthUseCase;
import com.spk.usecase.CalculateAHPUseCase;
import com.spk.usecase.CalculateTOPSISUseCase;
import com.spk.usecase.ScoreUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResultView extends JPanel {

    private final CalculateTOPSISUseCase topsisUseCase = new CalculateTOPSISUseCase();
    private final CalculateAHPUseCase ahpUseCase = new CalculateAHPUseCase();
    private final ScoreUseCase scoreUseCase = new ScoreUseCase();
    private final ResultRepository resultRepository = new ResultRepository();

    private JPanel resultContainer;

    public ResultView() {
        setLayout(new BorderLayout());
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
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Hasil Perhitungan TOPSIS");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Ranking vendor berdasarkan metode TOPSIS");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));

        if (AuthUseCase.isAdmin()) {
            CustomButton calcBtn = new CustomButton("⚡ Hitung TOPSIS");
            calcBtn.setSuccess();
            calcBtn.addActionListener(e -> calculateTOPSIS());

            CustomButton refreshBtn = new CustomButton("↻ Refresh");
            refreshBtn.addActionListener(e -> loadSavedResults());

            toolbar.add(calcBtn);
            toolbar.add(refreshBtn);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        resultContainer = new JPanel();
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(resultContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadSavedResults();
    }

    private void loadSavedResults() {
        resultContainer.removeAll();

        try {
            if (!resultRepository.hasResults()) {
                CardPanel emptyCard = new CardPanel();
                emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
                emptyCard.setBorder(new EmptyBorder(40, 20, 40, 20));

                JLabel emptyIcon = new JLabel("◎");
                emptyIcon.setFont(Theme.FONT_TITLE.deriveFont(48f));
                emptyIcon.setForeground(Theme.TEXT_MUTED);
                emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel emptyText = new JLabel("Belum ada hasil perhitungan TOPSIS");
                emptyText.setFont(Theme.FONT_BOLD.deriveFont(14f));
                emptyText.setForeground(Theme.TEXT_MUTED);
                emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel emptyHint = new JLabel(AuthUseCase.isAdmin() ?
                        "Klik tombol 'Hitung TOPSIS' untuk memulai perhitungan" :
                        "Admin belum melakukan perhitungan");
                emptyHint.setFont(Theme.FONT_REGULAR.deriveFont(12f));
                emptyHint.setForeground(Theme.TEXT_MUTED);
                emptyHint.setAlignmentX(Component.CENTER_ALIGNMENT);

                emptyCard.add(emptyIcon);
                emptyCard.add(Box.createRigidArea(new Dimension(0, 10)));
                emptyCard.add(emptyText);
                emptyCard.add(Box.createRigidArea(new Dimension(0, 5)));
                emptyCard.add(emptyHint);

                resultContainer.add(emptyCard);
                resultContainer.revalidate();
                resultContainer.repaint();
                return;
            }

            List<TOPSISResult> results = topsisUseCase.getSavedResults();
            displayResults(results);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateTOPSIS() {
        try {
            if (!ahpUseCase.hasWeights()) {
                JOptionPane.showMessageDialog(this, "Bobot AHP belum dihitung. Lakukan perhitungan AHP terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!scoreUseCase.isScoreComplete()) {
                JOptionPane.showMessageDialog(this, "Penilaian vendor belum lengkap. Lengkapi penilaian untuk semua vendor terhadap semua kriteria.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<TOPSISResult> results = topsisUseCase.calculate();
            resultContainer.removeAll();
            displayResults(results);

            JOptionPane.showMessageDialog(this, "Perhitungan TOPSIS berhasil! Ranking telah disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error perhitungan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults(List<TOPSISResult> results) {
        if (results == null || results.isEmpty()) return;

        // Winner card
        TOPSISResult winner = results.get(0);
        CardPanel winnerCard = new CardPanel();
        winnerCard.setLayout(new BoxLayout(winnerCard, BoxLayout.Y_AXIS));
        winnerCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        winnerCard.setBackground(new Color(102, 187, 106, 15));

        JLabel trophy = new JLabel("🏆");
        trophy.setFont(Theme.FONT_TITLE.deriveFont(42f));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerTitle = new JLabel("Vendor Terbaik");
        winnerTitle.setFont(Theme.FONT_BOLD.deriveFont(14f));
        winnerTitle.setForeground(Theme.ACCENT_SUCCESS);
        winnerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerName = new JLabel(winner.getVendorName());
        winnerName.setFont(Theme.FONT_BOLD.deriveFont(28f));
        winnerName.setForeground(Theme.TEXT_PRIMARY);
        winnerName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerScore = new JLabel("Skor Preferensi: " + String.format("%.6f", winner.getSkorPreferensi()));
        winnerScore.setFont(Theme.FONT_REGULAR.deriveFont(14f));
        winnerScore.setForeground(Theme.TEXT_SECONDARY);
        winnerScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        winnerCard.add(trophy);
        winnerCard.add(Box.createRigidArea(new Dimension(0, 10)));
        winnerCard.add(winnerTitle);
        winnerCard.add(Box.createRigidArea(new Dimension(0, 5)));
        winnerCard.add(winnerName);
        winnerCard.add(Box.createRigidArea(new Dimension(0, 5)));
        winnerCard.add(winnerScore);

        // Table card
        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel tableTitle = new JLabel("📊 Tabel Ranking");
        tableTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        tableTitle.setForeground(Theme.ACCENT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columnNames = {"Ranking", "Nama Vendor", "Skor Preferensi", "D+ (Jarak Ideal +)", "D- (Jarak Ideal -)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TOPSISResult r : results) {
            tableModel.addRow(new Object[]{
                    r.getRanking(),
                    r.getVendorName(),
                    String.format("%.6f", r.getSkorPreferensi()),
                    String.format("%.6f", r.getJarakIdealPositif()),
                    String.format("%.6f", r.getJarakIdealNegatif())
            });
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(Theme.FONT_REGULAR);
        table.getTableHeader().setFont(Theme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(37, 52, 85));
        table.getTableHeader().setForeground(Theme.ACCENT_PRIMARY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(Theme.BG_CARD);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableScroll.setPreferredSize(new Dimension(0, Math.min(400, 60 + results.size() * 30)));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // CR Card
        CardPanel crCard = new CardPanel();
        crCard.setLayout(new BoxLayout(crCard, BoxLayout.Y_AXIS));
        crCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        crCard.setBackground(new Color(79, 195, 247, 15));

        try {
            double cr = ahpUseCase.getSavedConsistencyRatio();
            JLabel crTitleInfo = new JLabel("ℹ Informasi Bobot AHP");
            crTitleInfo.setFont(Theme.FONT_BOLD.deriveFont(14f));
            crTitleInfo.setForeground(Theme.ACCENT_PRIMARY);
            
            JLabel crValue = new JLabel("Consistency Ratio (CR): " + String.format("%.4f", cr) +
                    (cr <= 0.1 ? " ✓ Konsisten" : " ✗ Tidak Konsisten"));
            crValue.setFont(Theme.FONT_REGULAR.deriveFont(12f));
            crValue.setForeground(cr <= 0.1 ? Theme.ACCENT_SUCCESS : Theme.ACCENT_DANGER);
            
            crCard.add(crTitleInfo);
            crCard.add(Box.createRigidArea(new Dimension(0, 5)));
            crCard.add(crValue);
        } catch (Exception ignored) {
        }

        resultContainer.add(winnerCard);
        resultContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        resultContainer.add(tableCard);
        resultContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        resultContainer.add(crCard);

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    public void refresh() {
        loadSavedResults();
    }
}
