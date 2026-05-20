package com.spk.presentation;

import java.util.List;

import com.spk.domain.TOPSISResult;
import com.spk.repository.ResultRepository;
import com.spk.usecase.AuthUseCase;
import com.spk.usecase.CalculateAHPUseCase;
import com.spk.usecase.CalculateTOPSISUseCase;
import com.spk.usecase.ScoreUseCase;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for displaying TOPSIS calculation results and rankings.
 */
public class ResultView extends VBox {

    private final CalculateTOPSISUseCase topsisUseCase = new CalculateTOPSISUseCase();
    private final CalculateAHPUseCase ahpUseCase = new CalculateAHPUseCase();
    private final ScoreUseCase scoreUseCase = new ScoreUseCase();
    private final ResultRepository resultRepository = new ResultRepository();

    private VBox resultContainer;

    public ResultView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Hasil Perhitungan TOPSIS");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Ranking vendor berdasarkan metode TOPSIS");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Toolbar (admin only)
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        if (AuthUseCase.isAdmin()) {
            Button calcBtn = new Button("⚡ Hitung TOPSIS");
            calcBtn.getStyleClass().add("btn-success");
            calcBtn.setOnAction(e -> calculateTOPSIS());

            Button refreshBtn = new Button("↻ Refresh");
            refreshBtn.setOnAction(e -> loadSavedResults());

            toolbar.getChildren().addAll(calcBtn, refreshBtn);
        }

        resultContainer = new VBox(20);

        ScrollPane scrollPane = new ScrollPane(resultContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, toolbar, scrollPane);

        loadSavedResults();
    }

    private void loadSavedResults() {
        resultContainer.getChildren().clear();

        try {
            if (!resultRepository.hasResults()) {
                VBox emptyCard = new VBox(10);
                emptyCard.getStyleClass().add("card");
                emptyCard.setAlignment(Pos.CENTER);
                Label emptyIcon = new Label("◎");
                emptyIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: -text-muted;");
                Label emptyText = new Label("Belum ada hasil perhitungan TOPSIS");
                emptyText.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 14px;");
                Label emptyHint = new Label(AuthUseCase.isAdmin() ?
                        "Klik tombol 'Hitung TOPSIS' untuk memulai perhitungan" :
                        "Admin belum melakukan perhitungan");
                emptyHint.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 12px;");
                emptyCard.getChildren().addAll(emptyIcon, emptyText, emptyHint);
                resultContainer.getChildren().add(emptyCard);
                return;
            }

            List<TOPSISResult> results = topsisUseCase.getSavedResults();
            displayResults(results);

        } catch (java.sql.SQLException | IllegalStateException e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void calculateTOPSIS() {
        try {
            // Validate prerequisites
            if (!ahpUseCase.hasWeights()) {
                showAlert("Bobot AHP belum dihitung. Lakukan perhitungan AHP terlebih dahulu.");
                return;
            }

            if (!scoreUseCase.isScoreComplete()) {
                showAlert("Penilaian vendor belum lengkap. Lengkapi penilaian untuk semua vendor terhadap semua kriteria.");
                return;
            }

            List<TOPSISResult> results = topsisUseCase.calculate();
            resultContainer.getChildren().clear();
            displayResults(results);

            showInfo("Perhitungan TOPSIS berhasil! Ranking telah disimpan.");
        } catch (java.sql.SQLException | IllegalStateException e) {
            showAlert("Error perhitungan: " + e.getMessage());
        }
    }

    private void displayResults(List<TOPSISResult> results) {
        if (results == null || results.isEmpty()) return;

        // Winner card
        TOPSISResult winner = results.get(0);
        VBox winnerCard = new VBox(8);
        winnerCard.getStyleClass().add("card");
        winnerCard.setStyle("-fx-border-color: rgba(102,187,106,0.4); -fx-background-color: rgba(102,187,106,0.05);");
        winnerCard.setAlignment(Pos.CENTER);
        winnerCard.setPadding(new Insets(24));

        Label trophy = new Label("🏆");
        trophy.setStyle("-fx-font-size: 42px;");
        Label winnerTitle = new Label("Vendor Terbaik");
        winnerTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: -accent-success; -fx-font-weight: bold;");
        Label winnerName = new Label(winner.getVendorName());
        winnerName.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        Label winnerScore = new Label("Skor Preferensi: " + String.format("%.6f", winner.getSkorPreferensi()));
        winnerScore.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-secondary;");

        winnerCard.getChildren().addAll(trophy, winnerTitle, winnerName, winnerScore);

        // Ranking table
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");

        Label tableTitle = new Label("📊 Tabel Ranking");
        tableTitle.getStyleClass().add("label-section");

        TableView<TOPSISResult> table = new TableView<>();
        table.setPlaceholder(new Label("Tidak ada data"));

        TableColumn<TOPSISResult, Integer> rankCol = new TableColumn<>("Ranking");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("ranking"));
        rankCol.setPrefWidth(80);
        rankCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<TOPSISResult, String> nameCol = new TableColumn<>("Nama Vendor");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        nameCol.setPrefWidth(250);

        TableColumn<TOPSISResult, Double> scoreCol = new TableColumn<>("Skor Preferensi");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("skorPreferensi"));
        scoreCol.setPrefWidth(160);
        scoreCol.setCellFactory(col -> new TableCell<TOPSISResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });

        TableColumn<TOPSISResult, Double> dPosCol = new TableColumn<>("D+ (Jarak Ideal +)");
        dPosCol.setCellValueFactory(new PropertyValueFactory<>("jarakIdealPositif"));
        dPosCol.setPrefWidth(150);
        dPosCol.setCellFactory(col -> new TableCell<TOPSISResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });

        TableColumn<TOPSISResult, Double> dNegCol = new TableColumn<>("D- (Jarak Ideal -)");
        dNegCol.setCellValueFactory(new PropertyValueFactory<>("jarakIdealNegatif"));
        dNegCol.setPrefWidth(150);
        dNegCol.setCellFactory(col -> new TableCell<TOPSISResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });

        table.getColumns().add(rankCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(scoreCol);
        table.getColumns().add(dPosCol);
        table.getColumns().add(dNegCol);
        table.setItems(FXCollections.observableArrayList(results));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(Math.min(400, 60 + results.size() * 40));

        tableCard.getChildren().addAll(tableTitle, table);

        // CR info
        VBox crCard = new VBox(8);
        crCard.getStyleClass().add("card");
        crCard.setStyle("-fx-background-color: rgba(79,195,247,0.05);");
        try {
            double cr = ahpUseCase.getSavedConsistencyRatio();
            Label crTitle = new Label("ℹ Informasi Bobot AHP");
            crTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -accent-primary;");
            Label crValue = new Label("Consistency Ratio (CR): " + String.format("%.4f", cr) +
                    (cr <= 0.1 ? " ✓ Konsisten" : " ✗ Tidak Konsisten"));
            crValue.setStyle("-fx-text-fill: " + (cr <= 0.1 ? "-accent-success" : "-accent-danger") + ";");
            crCard.getChildren().addAll(crTitle, crValue);
        } catch (java.sql.SQLException ignored) {}

        resultContainer.getChildren().addAll(winnerCard, tableCard, crCard);
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
        loadSavedResults();
    }
}
