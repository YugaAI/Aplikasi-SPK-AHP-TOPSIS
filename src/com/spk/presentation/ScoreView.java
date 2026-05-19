package com.spk.presentation;

import com.spk.domain.Criteria;
import com.spk.domain.Score;
import com.spk.domain.Vendor;
import com.spk.usecase.CriteriaUseCase;
import com.spk.usecase.ScoreUseCase;
import com.spk.usecase.VendorUseCase;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View for inputting vendor scores against each criterion.
 */
public class ScoreView extends VBox {

    private final ScoreUseCase scoreUseCase = new ScoreUseCase();
    private final VendorUseCase vendorUseCase = new VendorUseCase();
    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();

    private ComboBox<Vendor> vendorCombo;
    private VBox formContainer;
    private final Map<Integer, TextField> scoreFields = new HashMap<>();
    private Label statusLabel;

    public ScoreView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Penilaian Vendor");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Input nilai vendor terhadap setiap kriteria");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Status
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");
        checkCompleteness();

        // Vendor selector
        HBox selectorBar = new HBox(12);
        selectorBar.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("Pilih Vendor:");
        selectLabel.getStyleClass().add("form-label");

        vendorCombo = new ComboBox<>();
        vendorCombo.setPromptText("-- Pilih Vendor --");
        vendorCombo.setPrefWidth(300);
        vendorCombo.setCellFactory(lv -> new ListCell<Vendor>() {
            @Override
            protected void updateItem(Vendor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaVendor());
            }
        });
        vendorCombo.setButtonCell(new ListCell<Vendor>() {
            @Override
            protected void updateItem(Vendor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaVendor());
            }
        });
        vendorCombo.setOnAction(e -> loadScoresForVendor());

        selectorBar.getChildren().addAll(selectLabel, vendorCombo, statusLabel);

        // Form container
        formContainer = new VBox(10);
        formContainer.getStyleClass().add("card");

        ScrollPane scrollPane = new ScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, selectorBar, scrollPane);

        loadVendors();
    }

    private void loadVendors() {
        try {
            List<Vendor> vendors = vendorUseCase.getAllVendors();
            vendorCombo.setItems(FXCollections.observableArrayList(vendors));
        } catch (Exception e) {
            showAlert("Error memuat vendor: " + e.getMessage());
        }
    }

    private void loadScoresForVendor() {
        Vendor selected = vendorCombo.getValue();
        if (selected == null)
            return;

        formContainer.getChildren().clear();
        scoreFields.clear();

        try {
            List<Criteria> criteriaList = criteriaUseCase.getAllCriteria();
            List<Score> existingScores = scoreUseCase.getScoresByVendor(selected.getId());

            // Map existing scores by criteria ID
            Map<Integer, Double> existingMap = new HashMap<>();
            for (Score s : existingScores) {
                existingMap.put(s.getKriteriaId(), s.getNilai());
            }

            if (criteriaList.isEmpty()) {
                Label emptyLabel = new Label("Belum ada kriteria. Tambahkan kriteria terlebih dahulu.");
                emptyLabel.setStyle("-fx-text-fill: -accent-warning;");
                formContainer.getChildren().add(emptyLabel);
                return;
            }

            Label formTitle = new Label("Penilaian untuk: " + selected.getNamaVendor());
            formTitle.getStyleClass().add("label-section");
            formContainer.getChildren().add(formTitle);

            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(12);
            grid.setPadding(new Insets(10, 0, 10, 0));

            // Header row
            Label hKriteria = new Label("Kriteria");
            hKriteria.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
            Label hTipe = new Label("Tipe");
            hTipe.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
            Label hNilai = new Label("Nilai");
            hNilai.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
            grid.add(hKriteria, 0, 0);
            grid.add(hTipe, 1, 0);
            grid.add(hNilai, 2, 0);

            int row = 1;
            for (Criteria c : criteriaList) {
                Label nameLabel = new Label(c.getNamaKriteria());
                nameLabel.setStyle("-fx-text-fill: -text-primary;");

                Label tipeLabel = new Label(c.isBenefit() ? "▲ Benefit" : "▼ Cost");
                tipeLabel.setStyle("-fx-text-fill: " + (c.isBenefit() ? "-accent-success" : "-accent-danger")
                        + "; -fx-font-size: 11px;");

                TextField valueField = new TextField();
                valueField.setPromptText("Masukkan nilai");
                valueField.setPrefWidth(150);
                if (existingMap.containsKey(c.getId())) {
                    valueField.setText(String.valueOf(existingMap.get(c.getId())));
                }

                scoreFields.put(c.getId(), valueField);

                grid.add(nameLabel, 0, row);
                grid.add(tipeLabel, 1, row);
                grid.add(valueField, 2, row);
                row++;
            }

            formContainer.getChildren().add(grid);

            // Save button
            HBox btnBar = new HBox(10);
            btnBar.setAlignment(Pos.CENTER_LEFT);
            btnBar.setPadding(new Insets(10, 0, 0, 0));
            Button saveBtn = new Button("💾 Simpan Penilaian");
            saveBtn.getStyleClass().add("btn-success");
            saveBtn.setOnAction(e -> saveScores(selected.getId()));
            btnBar.getChildren().add(saveBtn);
            formContainer.getChildren().add(btnBar);

        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void saveScores(int vendorId) {
        try {
            List<Score> scores = new ArrayList<>();
            for (Map.Entry<Integer, TextField> entry : scoreFields.entrySet()) {
                String text = entry.getValue().getText().trim();
                if (text.isEmpty()) {
                    showAlert("Semua nilai harus diisi");
                    return;
                }
                double nilai;
                try {
                    nilai = Double.parseDouble(text);
                } catch (NumberFormatException ex) {
                    showAlert("Nilai harus berupa angka untuk semua kriteria");
                    return;
                }
                if (nilai <= 0) {
                    showAlert("Nilai harus lebih dari 0");
                    return;
                }
                Score score = new Score(vendorId, entry.getKey(), nilai);
                scores.add(score);
            }

            scoreUseCase.saveScores(vendorId, scores);
            showInfo("Penilaian berhasil disimpan!");
            checkCompleteness();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void checkCompleteness() {
        try {
            boolean complete = scoreUseCase.isScoreComplete();
            if (complete) {
                statusLabel.setText("✓ Semua penilaian lengkap");
                statusLabel.getStyleClass().setAll("badge-success");
            } else {
                statusLabel.setText("⚠ Penilaian belum lengkap");
                statusLabel.getStyleClass().setAll("badge-warning");
            }
        } catch (Exception e) {
            statusLabel.setText("✗ Error");
        }
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
        loadVendors();
        formContainer.getChildren().clear();
        checkCompleteness();
    }
}
