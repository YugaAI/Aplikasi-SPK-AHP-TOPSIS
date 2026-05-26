package com.spk.presentation;

import com.spk.domain.AHPResult;
import com.spk.domain.Criteria;
import com.spk.domain.PairwiseComparison;
import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.CalculateAHPUseCase;
import com.spk.usecase.CriteriaUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AHPView extends JPanel {

    private final CalculateAHPUseCase ahpUseCase = new CalculateAHPUseCase();
    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();

    private JPanel comparisonContainer;
    private JPanel resultContainer;
    private List<Criteria> criteriaList;
    private final List<JComboBox<String>> comparisonCombos = new ArrayList<>();
    private final List<int[]> comparisonPairs = new ArrayList<>();

    public AHPView() {
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

        JLabel title = new JLabel("Perhitungan AHP");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Perbandingan berpasangan antar kriteria untuk menentukan bobot");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Info card
        CardPanel infoCard = new CardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(new EmptyBorder(10, 15, 10, 15));
        infoCard.setBackground(new Color(79, 195, 247, 15));

        JLabel infoTitle = new JLabel("Skala Saaty (1-9)");
        infoTitle.setFont(Theme.FONT_BOLD);
        infoTitle.setForeground(Theme.ACCENT_PRIMARY);

        JLabel infoText = new JLabel("|  1 = Sama penting  |  3 = Sedikit lebih penting  |  5 = Lebih penting  |  7 = Sangat penting  |  9 = Mutlak penting  |  2,4,6,8 = Nilai antara  |");
        infoText.setFont(Theme.FONT_REGULAR.deriveFont(12f));
        infoText.setForeground(Theme.TEXT_SECONDARY);

        infoCard.add(infoTitle);
        infoCard.add(Box.createRigidArea(new Dimension(0, 5)));
        infoCard.add(infoText);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(infoCard, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Scrollable content
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setBorder(new EmptyBorder(20, 0, 20, 0));

        comparisonContainer = new JPanel();
        comparisonContainer.setLayout(new BoxLayout(comparisonContainer, BoxLayout.Y_AXIS));
        comparisonContainer.setOpaque(false);

        resultContainer = new JPanel();
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setOpaque(false);

        scrollContent.add(comparisonContainer);
        scrollContent.add(Box.createRigidArea(new Dimension(0, 20)));
        scrollContent.add(resultContainer);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadComparisons();
    }

    private void loadComparisons() {
        comparisonContainer.removeAll();
        comparisonCombos.clear();
        comparisonPairs.clear();

        try {
            criteriaList = criteriaUseCase.getAllCriteria();

            if (criteriaList.size() < 2) {
                JLabel warn = new JLabel("⚠ Minimal 2 kriteria diperlukan untuk perbandingan AHP");
                warn.setForeground(Theme.ACCENT_WARNING);
                warn.setFont(Theme.FONT_BOLD);
                comparisonContainer.add(warn);
                return;
            }

            CardPanel compCard = new CardPanel();
            compCard.setLayout(new BorderLayout(0, 15));
            compCard.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel formTitle = new JLabel("Perbandingan Berpasangan");
            formTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
            formTitle.setForeground(Theme.ACCENT_PRIMARY);
            compCard.add(formTitle, BorderLayout.NORTH);

            // Load existing
            List<PairwiseComparison> existing = ahpUseCase.getPairwiseComparisons();
            Map<String, Double> existingMap = new HashMap<>();
            for (PairwiseComparison pc : existing) {
                existingMap.put(pc.getKriteriaId1() + "-" + pc.getKriteriaId2(), pc.getNilai());
            }

            Map<String, Double> scaleMap = CalculateAHPUseCase.getIntensityScale();
            List<String> allOptions = new ArrayList<>();
            allOptions.add("1/9 - Mutlak Kurang Penting");
            allOptions.add("1/8 - Mendekati Mutlak Kurang Penting");
            allOptions.add("1/7 - Sangat Kurang Penting");
            allOptions.add("1/6 - Mendekati Sangat Kurang Penting");
            allOptions.add("1/5 - Kurang Penting");
            allOptions.add("1/4 - Mendekati Kurang Penting");
            allOptions.add("1/3 - Sedikit Kurang Penting");
            allOptions.add("1/2 - Mendekati Sedikit Kurang Penting");
            allOptions.addAll(scaleMap.keySet());

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;

            // Headers
            gbc.gridy = 0;
            String[] headers = {"Kriteria A", "vs", "Kriteria B", "Intensitas Kepentingan"};
            for (int i = 0; i < headers.length; i++) {
                gbc.gridx = i;
                JLabel h = new JLabel(headers[i]);
                h.setFont(Theme.FONT_BOLD.deriveFont(12f));
                h.setForeground(i == 1 ? Theme.TEXT_MUTED : Theme.ACCENT_PRIMARY);
                grid.add(h, gbc);
            }

            int row = 1;
            for (int i = 0; i < criteriaList.size(); i++) {
                for (int j = i + 1; j < criteriaList.size(); j++) {
                    Criteria c1 = criteriaList.get(i);
                    Criteria c2 = criteriaList.get(j);

                    gbc.gridy = row;
                    
                    gbc.gridx = 0;
                    JLabel leftLabel = new JLabel(c1.getNamaKriteria());
                    leftLabel.setForeground(Theme.TEXT_PRIMARY);
                    grid.add(leftLabel, gbc);

                    gbc.gridx = 1;
                    JLabel vsLabel = new JLabel("vs");
                    vsLabel.setForeground(Theme.TEXT_MUTED);
                    grid.add(vsLabel, gbc);

                    gbc.gridx = 2;
                    JLabel rightLabel = new JLabel(c2.getNamaKriteria());
                    rightLabel.setForeground(Theme.TEXT_PRIMARY);
                    grid.add(rightLabel, gbc);

                    gbc.gridx = 3;
                    JComboBox<String> combo = new JComboBox<>(allOptions.toArray(new String[0]));
                    combo.setPreferredSize(new Dimension(280, 30));
                    
                    String key = c1.getId() + "-" + c2.getId();
                    if (existingMap.containsKey(key)) {
                        combo.setSelectedItem(findScaleLabel(existingMap.get(key), allOptions));
                    }
                    
                    comparisonCombos.add(combo);
                    comparisonPairs.add(new int[]{c1.getId(), c2.getId()});
                    grid.add(combo, gbc);
                    
                    row++;
                }
            }
            
            compCard.add(grid, BorderLayout.CENTER);

            // Buttons
            JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            btnBar.setOpaque(false);
            btnBar.setBorder(new EmptyBorder(10, 0, 0, 0));

            CustomButton saveBtn = new CustomButton("💾 Simpan Perbandingan");
            saveBtn.setPrimary();
            saveBtn.addActionListener(e -> saveComparisons());

            CustomButton calcBtn = new CustomButton("⚡ Hitung AHP");
            calcBtn.addActionListener(e -> calculateAHP());

            btnBar.add(saveBtn);
            btnBar.add(calcBtn);
            
            compCard.add(btnBar, BorderLayout.SOUTH);
            comparisonContainer.add(compCard);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String findScaleLabel(double value, List<String> options) {
        if (value == 1.0) return "1 - Sama Penting";
        if (value == 2.0) return "2 - Mendekati Sedikit Lebih Penting";
        if (value == 3.0) return "3 - Sedikit Lebih Penting";
        if (value == 4.0) return "4 - Mendekati Lebih Penting";
        if (value == 5.0) return "5 - Lebih Penting";
        if (value == 6.0) return "6 - Mendekati Sangat Penting";
        if (value == 7.0) return "7 - Sangat Penting";
        if (value == 8.0) return "8 - Mendekati Mutlak Penting";
        if (value == 9.0) return "9 - Mutlak Penting";
        if (Math.abs(value - 1.0/2) < 0.01) return "1/2 - Mendekati Sedikit Kurang Penting";
        if (Math.abs(value - 1.0/3) < 0.01) return "1/3 - Sedikit Kurang Penting";
        if (Math.abs(value - 1.0/4) < 0.01) return "1/4 - Mendekati Kurang Penting";
        if (Math.abs(value - 1.0/5) < 0.01) return "1/5 - Kurang Penting";
        if (Math.abs(value - 1.0/6) < 0.01) return "1/6 - Mendekati Sangat Kurang Penting";
        if (Math.abs(value - 1.0/7) < 0.01) return "1/7 - Sangat Kurang Penting";
        if (Math.abs(value - 1.0/8) < 0.01) return "1/8 - Mendekati Mutlak Kurang Penting";
        if (Math.abs(value - 1.0/9) < 0.01) return "1/9 - Mutlak Kurang Penting";
        return "1 - Sama Penting";
    }

    private double parseScaleValue(String label) {
        if (label == null) return 1.0;
        if (label.startsWith("1/9")) return 1.0/9;
        if (label.startsWith("1/8")) return 1.0/8;
        if (label.startsWith("1/7")) return 1.0/7;
        if (label.startsWith("1/6")) return 1.0/6;
        if (label.startsWith("1/5")) return 1.0/5;
        if (label.startsWith("1/4")) return 1.0/4;
        if (label.startsWith("1/3")) return 1.0/3;
        if (label.startsWith("1/2")) return 1.0/2;
        if (label.startsWith("1 ")) return 1.0;
        if (label.startsWith("2 ")) return 2.0;
        if (label.startsWith("3 ")) return 3.0;
        if (label.startsWith("4 ")) return 4.0;
        if (label.startsWith("5 ")) return 5.0;
        if (label.startsWith("6 ")) return 6.0;
        if (label.startsWith("7 ")) return 7.0;
        if (label.startsWith("8 ")) return 8.0;
        if (label.startsWith("9 ")) return 9.0;
        return 1.0;
    }

    private void saveComparisons() {
        try {
            List<PairwiseComparison> comparisons = new ArrayList<>();
            for (int idx = 0; idx < comparisonCombos.size(); idx++) {
                JComboBox<String> combo = comparisonCombos.get(idx);
                if (combo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, "Semua perbandingan harus diisi", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double value = parseScaleValue((String) combo.getSelectedItem());
                int[] pair = comparisonPairs.get(idx);
                comparisons.add(new PairwiseComparison(pair[0], pair[1], value));
            }
            ahpUseCase.savePairwiseComparisons(comparisons);
            JOptionPane.showMessageDialog(this, "Perbandingan berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateAHP() {
        try {
            List<PairwiseComparison> comparisons = new ArrayList<>();
            for (int idx = 0; idx < comparisonCombos.size(); idx++) {
                JComboBox<String> combo = comparisonCombos.get(idx);
                if (combo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, "Semua perbandingan harus diisi sebelum perhitungan", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double value = parseScaleValue((String) combo.getSelectedItem());
                int[] pair = comparisonPairs.get(idx);
                comparisons.add(new PairwiseComparison(pair[0], pair[1], value));
            }
            ahpUseCase.savePairwiseComparisons(comparisons);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            AHPResult result = ahpUseCase.calculate();
            displayResult(result);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error perhitungan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResult(AHPResult result) {
        resultContainer.removeAll();

        // Pairwise matrix
        CardPanel matrixCard = new CardPanel();
        matrixCard.setLayout(new BorderLayout(0, 10));
        matrixCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel matrixTitle = new JLabel("📊 Matriks Perbandingan Berpasangan");
        matrixTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        matrixTitle.setForeground(Theme.ACCENT_PRIMARY);
        matrixCard.add(matrixTitle, BorderLayout.NORTH);
        matrixCard.add(createMatrixGrid(result.getPairwiseMatrix()), BorderLayout.CENTER);

        // Normalized matrix
        CardPanel normCard = new CardPanel();
        normCard.setLayout(new BorderLayout(0, 10));
        normCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel normTitle = new JLabel("📊 Matriks Normalisasi");
        normTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        normTitle.setForeground(Theme.ACCENT_PRIMARY);
        normCard.add(normTitle, BorderLayout.NORTH);
        normCard.add(createMatrixGrid(result.getNormalizedMatrix()), BorderLayout.CENTER);

        // Weights
        CardPanel weightsCard = new CardPanel();
        weightsCard.setLayout(new BorderLayout(0, 10));
        weightsCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel weightsTitle = new JLabel("⚖ Bobot Kriteria (Priority Vector)");
        weightsTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        weightsTitle.setForeground(Theme.ACCENT_PRIMARY);
        weightsCard.add(weightsTitle, BorderLayout.NORTH);

        JPanel weightsGrid = new JPanel(new GridLayout(0, 3, 10, 5));
        weightsGrid.setOpaque(false);
        weightsGrid.add(createBoldLabel("Kriteria"));
        weightsGrid.add(createBoldLabel("Bobot"));
        weightsGrid.add(createBoldLabel("Persentase"));

        for (Map.Entry<Integer, Double> entry : result.getWeights().entrySet()) {
            weightsGrid.add(new JLabel(getCriteriaName(entry.getKey())));
            weightsGrid.add(new JLabel(String.format("%.4f", entry.getValue())));
            JLabel pct = new JLabel(String.format("%.2f%%", entry.getValue() * 100));
            pct.setForeground(Theme.ACCENT_SUCCESS);
            pct.setFont(Theme.FONT_BOLD);
            weightsGrid.add(pct);
        }
        weightsCard.add(weightsGrid, BorderLayout.CENTER);

        // Consistency
        CardPanel crCard = new CardPanel();
        crCard.setLayout(new BoxLayout(crCard, BoxLayout.Y_AXIS));
        crCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        boolean consistent = result.isConsistent();

        JLabel crTitle = new JLabel(consistent ? "✓ Konsistensi: VALID" : "✗ Konsistensi: TIDAK VALID");
        crTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        crTitle.setForeground(consistent ? Theme.ACCENT_SUCCESS : Theme.ACCENT_DANGER);
        crCard.add(crTitle);
        crCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel crGrid = new JPanel(new GridLayout(0, 2, 10, 5));
        crGrid.setOpaque(false);
        crGrid.add(new JLabel("λ max"));
        crGrid.add(new JLabel(String.format("%.4f", result.getLambdaMax())));
        crGrid.add(new JLabel("CI (Consistency Index)"));
        crGrid.add(new JLabel(String.format("%.4f", result.getConsistencyIndex())));
        crGrid.add(new JLabel("CR (Consistency Ratio)"));
        JLabel crValue = new JLabel(String.format("%.4f", result.getConsistencyRatio()));
        crValue.setFont(Theme.FONT_BOLD);
        crValue.setForeground(consistent ? Theme.ACCENT_SUCCESS : Theme.ACCENT_DANGER);
        crGrid.add(crValue);
        crGrid.add(new JLabel("Batas CR"));
        crGrid.add(new JLabel("≤ 0.1"));
        
        crCard.add(crGrid);

        if (!consistent) {
            crCard.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel warn = new JLabel("<html>⚠ Perbandingan tidak konsisten! Ubah nilai perbandingan agar CR ≤ 0.1</html>");
            warn.setForeground(Theme.ACCENT_DANGER);
            crCard.add(warn);
        }

        resultContainer.add(matrixCard);
        resultContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        resultContainer.add(normCard);
        resultContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        resultContainer.add(weightsCard);
        resultContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        resultContainer.add(crCard);

        revalidate();
        repaint();
    }

    private JPanel createMatrixGrid(double[][] matrix) {
        int n = matrix.length;
        JPanel grid = new JPanel(new GridLayout(n + 1, n + 1, 4, 4));
        grid.setOpaque(false);

        grid.add(new JLabel(""));
        for (int j = 0; j < n && j < criteriaList.size(); j++) {
            grid.add(createBoldLabel(criteriaList.get(j).getNamaKriteria()));
        }

        for (int i = 0; i < n && i < criteriaList.size(); i++) {
            grid.add(createBoldLabel(criteriaList.get(i).getNamaKriteria()));
            for (int j = 0; j < n; j++) {
                JLabel cell = new JLabel(String.format("%.4f", matrix[i][j]));
                cell.setHorizontalAlignment(SwingConstants.CENTER);
                if (i == j) {
                    cell.setOpaque(true);
                    cell.setBackground(new Color(79, 195, 247, 20));
                }
                grid.add(cell);
            }
        }
        return grid;
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD.deriveFont(11f));
        label.setForeground(Theme.ACCENT_PRIMARY);
        return label;
    }

    private String getCriteriaName(int criteriaId) {
        if (criteriaList != null) {
            for (Criteria c : criteriaList) {
                if (c.getId() == criteriaId) return c.getNamaKriteria();
            }
        }
        return "Kriteria " + criteriaId;
    }

    public void refresh() {
        resultContainer.removeAll();
        loadComparisons();
    }
}
