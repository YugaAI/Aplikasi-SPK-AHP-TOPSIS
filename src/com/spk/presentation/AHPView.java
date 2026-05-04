package com.spk.presentation;

import com.spk.domain.AHPResult;
import com.spk.domain.Criteria;
import com.spk.domain.PairwiseComparison;
import com.spk.usecase.CalculateAHPUseCase;
import com.spk.usecase.CriteriaUseCase;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * View for AHP pairwise comparison input and calculation.
 */
public class AHPView extends VBox {

    private final CalculateAHPUseCase ahpUseCase = new CalculateAHPUseCase();
    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();

    private VBox comparisonContainer;
    private VBox resultContainer;
    private List<Criteria> criteriaList;
    private final List<ComboBox<String>> comparisonCombos = new ArrayList<>();
    private final List<int[]> comparisonPairs = new ArrayList<>(); // pairs of criteria IDs

    public AHPView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Perhitungan AHP");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Perbandingan berpasangan antar kriteria untuk menentukan bobot");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Info card
        VBox infoCard = new VBox(6);
        infoCard.getStyleClass().add("card");
        infoCard.setStyle("-fx-background-color: rgba(79, 195, 247, 0.05); -fx-border-color: rgba(79, 195, 247, 0.2);");
        Label infoTitle = new Label("ℹ Skala Saaty (1-9)");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
        Label infoText = new Label(
                "1 = Sama penting  |  3 = Sedikit lebih penting  |  5 = Lebih penting\n" +
                "7 = Sangat penting  |  9 = Mutlak penting  |  2,4,6,8 = Nilai antara"
        );
        infoText.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");
        infoCard.getChildren().addAll(infoTitle, infoText);

        // Comparison form
        comparisonContainer = new VBox(12);
        comparisonContainer.getStyleClass().add("card");

        // Result display
        resultContainer = new VBox(12);

        ScrollPane scrollPane = new ScrollPane(new VBox(20, comparisonContainer, resultContainer));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, infoCard, scrollPane);

        loadComparisons();
    }

    private void loadComparisons() {
        comparisonContainer.getChildren().clear();
        comparisonCombos.clear();
        comparisonPairs.clear();

        try {
            criteriaList = criteriaUseCase.getAllCriteria();

            if (criteriaList.size() < 2) {
                Label warn = new Label("⚠ Minimal 2 kriteria diperlukan untuk perbandingan AHP");
                warn.setStyle("-fx-text-fill: -accent-warning;");
                comparisonContainer.getChildren().add(warn);
                return;
            }

            Label formTitle = new Label("Perbandingan Berpasangan");
            formTitle.getStyleClass().add("label-section");
            comparisonContainer.getChildren().add(formTitle);

            // Load existing comparisons
            List<PairwiseComparison> existing = ahpUseCase.getPairwiseComparisons();
            Map<String, Double> existingMap = new java.util.HashMap<>();
            for (PairwiseComparison pc : existing) {
                existingMap.put(pc.getKriteriaId1() + "-" + pc.getKriteriaId2(), pc.getNilai());
            }

            // Build comparison scale options
            Map<String, Double> scaleMap = CalculateAHPUseCase.getIntensityScale();
            List<String> scaleOptions = new ArrayList<>(scaleMap.keySet());

            // Also add reciprocal options (1/2 to 1/9) for when criterion 2 is more important
            List<String> allOptions = new ArrayList<>();
            allOptions.add("1/9 - Mutlak Kurang Penting");
            allOptions.add("1/8 - Mendekati Mutlak Kurang Penting");
            allOptions.add("1/7 - Sangat Kurang Penting");
            allOptions.add("1/6 - Mendekati Sangat Kurang Penting");
            allOptions.add("1/5 - Kurang Penting");
            allOptions.add("1/4 - Mendekati Kurang Penting");
            allOptions.add("1/3 - Sedikit Kurang Penting");
            allOptions.add("1/2 - Mendekati Sedikit Kurang Penting");
            allOptions.addAll(scaleOptions);

            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 0, 10, 0));

            // Header
            Label hLeft = new Label("Kriteria A");
            hLeft.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-font-size: 12px;");
            Label hVs = new Label("vs");
            hVs.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-font-size: 12px;");
            Label hRight = new Label("Kriteria B");
            hRight.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-font-size: 12px;");
            Label hValue = new Label("Intensitas Kepentingan");
            hValue.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-font-size: 12px;");
            grid.add(hLeft, 0, 0);
            grid.add(hVs, 1, 0);
            grid.add(hRight, 2, 0);
            grid.add(hValue, 3, 0);

            int row = 1;
            for (int i = 0; i < criteriaList.size(); i++) {
                for (int j = i + 1; j < criteriaList.size(); j++) {
                    Criteria c1 = criteriaList.get(i);
                    Criteria c2 = criteriaList.get(j);

                    Label leftLabel = new Label(c1.getNamaKriteria());
                    leftLabel.setStyle("-fx-text-fill: -text-primary;");
                    Label vsLabel = new Label("vs");
                    vsLabel.setStyle("-fx-text-fill: -text-muted;");
                    Label rightLabel = new Label(c2.getNamaKriteria());
                    rightLabel.setStyle("-fx-text-fill: -text-primary;");

                    ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(allOptions));
                    combo.setPrefWidth(320);
                    combo.setPromptText("Pilih intensitas");

                    // Set existing value
                    String key = c1.getId() + "-" + c2.getId();
                    if (existingMap.containsKey(key)) {
                        double val = existingMap.get(key);
                        combo.setValue(findScaleLabel(val, allOptions));
                    }

                    comparisonCombos.add(combo);
                    comparisonPairs.add(new int[]{c1.getId(), c2.getId()});

                    grid.add(leftLabel, 0, row);
                    grid.add(vsLabel, 1, row);
                    grid.add(rightLabel, 2, row);
                    grid.add(combo, 3, row);
                    row++;
                }
            }

            comparisonContainer.getChildren().add(grid);

            // Buttons
            HBox btnBar = new HBox(12);
            btnBar.setAlignment(Pos.CENTER_LEFT);
            btnBar.setPadding(new Insets(10, 0, 0, 0));

            Button saveBtn = new Button("💾 Simpan Perbandingan");
            saveBtn.getStyleClass().add("btn-primary");
            saveBtn.setOnAction(e -> saveComparisons());

            Button calcBtn = new Button("⚡ Hitung AHP");
            calcBtn.getStyleClass().add("btn-success");
            calcBtn.setOnAction(e -> calculateAHP());

            btnBar.getChildren().addAll(saveBtn, calcBtn);
            comparisonContainer.getChildren().add(btnBar);

        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private String findScaleLabel(double value, List<String> options) {
        // Map value to label
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
                ComboBox<String> combo = comparisonCombos.get(idx);
                if (combo.getValue() == null) {
                    showAlert("Semua perbandingan harus diisi");
                    return;
                }
                double value = parseScaleValue(combo.getValue());
                int[] pair = comparisonPairs.get(idx);
                comparisons.add(new PairwiseComparison(pair[0], pair[1], value));
            }

            ahpUseCase.savePairwiseComparisons(comparisons);
            showInfo("Perbandingan berhasil disimpan!");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void calculateAHP() {
        // Save first
        try {
            List<PairwiseComparison> comparisons = new ArrayList<>();
            for (int idx = 0; idx < comparisonCombos.size(); idx++) {
                ComboBox<String> combo = comparisonCombos.get(idx);
                if (combo.getValue() == null) {
                    showAlert("Semua perbandingan harus diisi sebelum perhitungan");
                    return;
                }
                double value = parseScaleValue(combo.getValue());
                int[] pair = comparisonPairs.get(idx);
                comparisons.add(new PairwiseComparison(pair[0], pair[1], value));
            }
            ahpUseCase.savePairwiseComparisons(comparisons);
        } catch (Exception e) {
            showAlert("Error saving: " + e.getMessage());
            return;
        }

        try {
            AHPResult result = ahpUseCase.calculate();
            displayResult(result);
        } catch (Exception e) {
            showAlert("Error perhitungan: " + e.getMessage());
        }
    }

    private void displayResult(AHPResult result) {
        resultContainer.getChildren().clear();

        // Pairwise matrix card
        VBox matrixCard = new VBox(10);
        matrixCard.getStyleClass().add("card");
        Label matrixTitle = new Label("📊 Matriks Perbandingan Berpasangan");
        matrixTitle.getStyleClass().add("label-section");
        matrixCard.getChildren().add(matrixTitle);
        matrixCard.getChildren().add(createMatrixGrid(result.getPairwiseMatrix(), "Pairwise"));

        // Normalized matrix card
        VBox normCard = new VBox(10);
        normCard.getStyleClass().add("card");
        Label normTitle = new Label("📊 Matriks Normalisasi");
        normTitle.getStyleClass().add("label-section");
        normCard.getChildren().add(normTitle);
        normCard.getChildren().add(createMatrixGrid(result.getNormalizedMatrix(), "Normalized"));

        // Weights card
        VBox weightsCard = new VBox(10);
        weightsCard.getStyleClass().add("card");
        Label weightsTitle = new Label("⚖ Bobot Kriteria (Priority Vector)");
        weightsTitle.getStyleClass().add("label-section");

        GridPane weightsGrid = new GridPane();
        weightsGrid.setHgap(16);
        weightsGrid.setVgap(8);
        weightsGrid.setPadding(new Insets(10, 0, 10, 0));

        Label whKriteria = new Label("Kriteria");
        whKriteria.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
        Label whBobot = new Label("Bobot");
        whBobot.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
        Label whPersen = new Label("Persentase");
        whPersen.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
        weightsGrid.add(whKriteria, 0, 0);
        weightsGrid.add(whBobot, 1, 0);
        weightsGrid.add(whPersen, 2, 0);

        int row = 1;
        for (Map.Entry<Integer, Double> entry : result.getWeights().entrySet()) {
            String criteriaName = getCriteriaName(entry.getKey());
            Label nameLabel = new Label(criteriaName);
            nameLabel.setStyle("-fx-text-fill: -text-primary;");
            Label bobotLabel = new Label(String.format("%.4f", entry.getValue()));
            bobotLabel.setStyle("-fx-text-fill: -text-primary;");
            Label pctLabel = new Label(String.format("%.2f%%", entry.getValue() * 100));
            pctLabel.setStyle("-fx-text-fill: -accent-success; -fx-font-weight: bold;");
            weightsGrid.add(nameLabel, 0, row);
            weightsGrid.add(bobotLabel, 1, row);
            weightsGrid.add(pctLabel, 2, row);
            row++;
        }

        weightsCard.getChildren().add(weightsGrid);

        // Consistency card
        VBox crCard = new VBox(10);
        crCard.getStyleClass().add("card");

        boolean consistent = result.isConsistent();
        crCard.setStyle("-fx-border-color: " + (consistent ? "rgba(102,187,106,0.3)" : "rgba(239,83,80,0.3)") + ";");

        Label crTitle = new Label(consistent ? "✓ Konsistensi: VALID" : "✗ Konsistensi: TIDAK VALID");
        crTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                (consistent ? "-accent-success" : "-accent-danger") + ";");

        GridPane crGrid = new GridPane();
        crGrid.setHgap(20);
        crGrid.setVgap(6);
        crGrid.add(new Label("λ max"), 0, 0);
        crGrid.add(createValueLabel(String.format("%.4f", result.getLambdaMax())), 1, 0);
        crGrid.add(new Label("CI (Consistency Index)"), 0, 1);
        crGrid.add(createValueLabel(String.format("%.4f", result.getConsistencyIndex())), 1, 1);
        crGrid.add(new Label("CR (Consistency Ratio)"), 0, 2);
        Label crValue = createValueLabel(String.format("%.4f", result.getConsistencyRatio()));
        crValue.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (consistent ? "-accent-success" : "-accent-danger") + ";");
        crGrid.add(crValue, 1, 2);
        crGrid.add(new Label("Batas CR"), 0, 3);
        crGrid.add(createValueLabel("≤ 0.1"), 1, 3);

        crCard.getChildren().addAll(crTitle, crGrid);

        if (!consistent) {
            Label warn = new Label("⚠ Perbandingan tidak konsisten! Ubah nilai perbandingan agar CR ≤ 0.1");
            warn.setStyle("-fx-text-fill: -accent-danger; -fx-font-size: 12px;");
            warn.setWrapText(true);
            crCard.getChildren().add(warn);
        }

        resultContainer.getChildren().addAll(matrixCard, normCard, weightsCard, crCard);
    }

    private GridPane createMatrixGrid(double[][] matrix, String type) {
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(10, 0, 10, 0));

        int n = matrix.length;

        // Column headers
        grid.add(new Label(""), 0, 0);
        for (int j = 0; j < n && j < criteriaList.size(); j++) {
            Label header = new Label(criteriaList.get(j).getNamaKriteria());
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
            grid.add(header, j + 1, 0);
        }

        // Rows
        for (int i = 0; i < n && i < criteriaList.size(); i++) {
            Label rowHeader = new Label(criteriaList.get(i).getNamaKriteria());
            rowHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
            grid.add(rowHeader, 0, i + 1);

            for (int j = 0; j < n; j++) {
                Label cell = new Label(String.format("%.4f", matrix[i][j]));
                cell.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 11px; -fx-padding: 4 8 4 8; " +
                        "-fx-background-color: " + (i == j ? "rgba(79,195,247,0.08)" : "transparent") + "; " +
                        "-fx-background-radius: 4;");
                grid.add(cell, j + 1, i + 1);
            }
        }

        return grid;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -text-primary;");
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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle("Sukses");
        alert.setHeaderText("Sukses");
        alert.showAndWait();
    }

    public void refresh() {
        resultContainer.getChildren().clear();
        loadComparisons();
    }
}
