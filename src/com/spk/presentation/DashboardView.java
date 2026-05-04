package com.spk.presentation;

import java.sql.SQLException;
import java.util.Map;

import com.spk.repository.CriteriaRepository;
import com.spk.repository.ResultRepository;
import com.spk.repository.ScoreRepository;
import com.spk.repository.UserRepository;
import com.spk.repository.VendorRepository;

import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Dashboard view showing system statistics.
 */
public class DashboardView extends VBox {

    public DashboardView() {
        getStyleClass().add("content-area");
        setSpacing(24);
        buildUI();
    }

    private void buildUI() {
        getChildren().clear();

        // Header with breadcrumb
        VBox header = new VBox(10);
        header.getStyleClass().add("page-header");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Dashboard");
        title.getStyleClass().add("label-title");

        Label breadcrumb = new Label("Home  >  Dashboard");
        breadcrumb.getStyleClass().add("breadcrumb");

        titleRow.getChildren().addAll(title, breadcrumb);

        Label subtitle = new Label("Ringkasan sistem pilihan vendor IT dalam tampilan analytics modern");
        subtitle.getStyleClass().add("label-subtitle");

        header.getChildren().addAll(titleRow, subtitle, new Separator());

        // KPI row
        HBox kpiRow = new HBox(20);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        VBox sideCards = new VBox(20);
        sideCards.setPrefWidth(320);

        VBox chartCard = new VBox(18);
        chartCard.getStyleClass().add("chart-card");
        chartCard.setPrefWidth(620);

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

            kpiRow.getChildren().addAll(
                    createMetricCard("Total Kriteria", String.valueOf(criteriaCount), "", "◈", "#38bdf8"),
                    createMetricCard("Total Vendor", String.valueOf(vendorCount), "", "◉", "#8b5cf6"),
                    createMetricCard("Total Skor", String.valueOf(scoreCount), "", "▣", "#f59e0b"),
                    createMetricCard("Total User", String.valueOf(userCount), "", "☷", "#38bdf8")
            );

            sideCards.getChildren().add(createCriteriaTypeLineChartCard(criteriaTypeCounts));
            chartCard.getChildren().addAll(createWeightPieChartCard(weightsByCriteria));
        } catch (SQLException e) {
            Label errLabel = new Label("Error loading stats: " + e.getMessage());
            errLabel.setStyle("-fx-text-fill: -accent-danger;");
            kpiRow.getChildren().add(errLabel);
        }

        // Main analytics row
        HBox analyticsRow = new HBox(20);
        analyticsRow.setAlignment(Pos.TOP_LEFT);

        analyticsRow.getChildren().addAll(chartCard, sideCards);

        // Bottom insights
        HBox insightRow = new HBox(20);
        insightRow.setAlignment(Pos.TOP_LEFT);

        VBox activityCard = new VBox(16);
        activityCard.getStyleClass().add("card");
        Label activityTitle = new Label("Recent Activity");
        activityTitle.getStyleClass().add("label-section");
        activityCard.getChildren().add(activityTitle);

        String[] activities = {
                "Data vendor baru berhasil ditambahkan",
                "Bobot AHP terbaru dihitung",
                "Perhitungan TOPSIS selesai untuk 5 vendor"
        };
        for (String activity : activities) {
            Label row = new Label("• " + activity);
            row.getStyleClass().add("list-subtitle");
            activityCard.getChildren().add(row);
        }

        VBox performanceCard = new VBox(16);
        performanceCard.getStyleClass().add("card");
        Label performanceTitle = new Label("Performance Overview");
        performanceTitle.getStyleClass().add("label-section");
        performanceCard.getChildren().addAll(performanceTitle,
                createPerformanceRow("Akurasi Data", 82, "#38bdf8"),
                createPerformanceRow("Kepuasan User", 73, "#8b5cf6"),
                createPerformanceRow("Penyelesaian Analisa", 94, "#22c55e"));

        insightRow.getChildren().addAll(activityCard, performanceCard);

        getChildren().addAll(header, kpiRow, analyticsRow, insightRow);
    }

    private VBox createMetricCard(String label, String value, String change, String icon, String iconColor) {
        VBox card = new VBox(12);
        card.getStyleClass().add("metric-card");
        card.setAlignment(Pos.TOP_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");
        iconLabel.setTextFill(Color.web(iconColor));

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("metric-label");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");

        card.getChildren().addAll(iconLabel, nameLabel, valueLabel);
        if (change != null && !change.isEmpty()) {
            Label changeLabel = new Label(change);
            changeLabel.getStyleClass().add("metric-change");
            card.getChildren().add(changeLabel);
        }
        return card;
    }

    private VBox createCriteriaTypeLineChartCard(Map<String, Integer> criteriaTypeCounts) {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");

        Label title = new Label("Jumlah Tipe Kriteria");
        title.getStyleClass().add("label-section");
        card.getChildren().add(title);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Tipe Kriteria");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(260);
        lineChart.setPrefWidth(320);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (criteriaTypeCounts.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No Data", 0));
        } else {
            for (Map.Entry<String, Integer> entry : criteriaTypeCounts.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        lineChart.getData().add(series);

        card.getChildren().add(lineChart);
        return card;
    }

    private VBox createWeightPieChartCard(Map<String, Double> weightsByCriteria) {
        VBox card = new VBox(16);
        card.getStyleClass().add("chart-card");

        Label title = new Label("Bobot Kriteria");
        title.getStyleClass().add("chart-title");
        Label subtitle = new Label("Distribusi bobot kriteria AHP");
        subtitle.getStyleClass().add("chart-subtitle");
        card.getChildren().addAll(title, subtitle);

        if (weightsByCriteria.isEmpty()) {
            Label empty = new Label("Bobot AHP belum dihitung");
            empty.getStyleClass().add("list-subtitle");
            card.getChildren().add(empty);
            return card;
        }

        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefHeight(300);
        pieChart.setPrefWidth(560);

        for (Map.Entry<String, Double> entry : weightsByCriteria.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue() * 100));
        }

        card.getChildren().add(pieChart);
        return card;
    }

    private VBox createPerformanceRow(String label, int percent, String color) {
        VBox row = new VBox(6);
        Label title = new Label(label);
        title.getStyleClass().add("list-subtitle");

        Pane progress = new Pane();
        progress.getStyleClass().add("progress-bar");
        progress.setPrefWidth(220);
        Rectangle fill = new Rectangle(percent * 2.0, 8);
        fill.setArcWidth(8);
        fill.setArcHeight(8);
        fill.setFill(Color.web(color));
        progress.getChildren().add(fill);

        Label value = new Label(percent + "%");
        value.getStyleClass().add("metric-change");

        row.getChildren().addAll(title, progress, value);
        return row;
    }

    public void refresh() {
        buildUI();
    }
}
