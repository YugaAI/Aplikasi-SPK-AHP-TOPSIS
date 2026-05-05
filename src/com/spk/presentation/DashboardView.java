package com.spk.presentation;

import java.sql.SQLException;
import java.util.Map;

import com.spk.repository.CriteriaRepository;
import com.spk.repository.ResultRepository;
import com.spk.repository.ScoreRepository;
import com.spk.repository.UserRepository;
import com.spk.repository.VendorRepository;

import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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

        // KPI row with metrics and usage flow panel
        HBox kpiRow = new HBox(20);
        kpiRow.setAlignment(Pos.TOP_LEFT);

        HBox metricGroup = new HBox(20);
        metricGroup.setAlignment(Pos.CENTER_LEFT);
        metricGroup.setPrefWidth(0);
        metricGroup.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(metricGroup, javafx.scene.layout.Priority.ALWAYS);

        VBox flowPanel = new VBox();
        flowPanel.setAlignment(Pos.TOP_LEFT);
        flowPanel.setPrefWidth(340);

        VBox sideCards = new VBox(20);
        sideCards.setPrefWidth(340);
        sideCards.setMaxWidth(340);

        VBox chartCard = new VBox(18);
        chartCard.getStyleClass().add("chart-card");
        chartCard.setPrefWidth(0);
        chartCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(chartCard, javafx.scene.layout.Priority.ALWAYS);

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

            metricGroup.getChildren().addAll(
                    createMetricCard("Total Kriteria", String.valueOf(criteriaCount), "", "◈", "#38bdf8"),
                    createMetricCard("Total Vendor", String.valueOf(vendorCount), "", "◉", "#8b5cf6"),
                    createMetricCard("Total Skor", String.valueOf(scoreCount), "", "▣", "#f59e0b"),
                    createMetricCard("Total User", String.valueOf(userCount), "", "☷", "#38bdf8")
            );
            flowPanel.getChildren().add(createFlowUsageCard());
            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, javafx.scene.layout.Priority.ALWAYS);
            kpiRow.getChildren().addAll(metricGroup, flowPanel, rowSpacer);

            sideCards.getChildren().add(createCriteriaTypeBarChartCard(criteriaTypeCounts));
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

        getChildren().addAll(header, kpiRow, analyticsRow);
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

    private VBox createCriteriaTypeBarChartCard(Map<String, Integer> criteriaTypeCounts) {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");

        Label title = new Label("Jumlah Tipe Kriteria");
        title.getStyleClass().add("label-section");
        card.getChildren().add(title);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Tipe Kriteria");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(320);
        barChart.setPrefWidth(420);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalGridLinesVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (criteriaTypeCounts.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No Data", 0));
        } else {
            for (Map.Entry<String, Integer> entry : criteriaTypeCounts.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        barChart.getData().add(series);

        card.getChildren().add(barChart);
        return card;
    }

    private VBox createFlowUsageCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);

        Label title = new Label("Flow Penggunaan");
        title.getStyleClass().add("label-section");

        String[] activities = {
                "1. Login dengan akun valid",
                "2. Kelola kriteria dan vendor",
                "3. Hitung bobot AHP",
                "4. Jalankan TOPSIS untuk ranking"
        };

        card.getChildren().add(title);
        for (String activity : activities) {
            Label row = new Label(activity);
            row.getStyleClass().add("list-subtitle");
            card.getChildren().add(row);
        }

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
        pieChart.setPrefHeight(520);
        pieChart.setPrefWidth(Double.MAX_VALUE);
        pieChart.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(pieChart, javafx.scene.layout.Priority.ALWAYS);

        for (Map.Entry<String, Double> entry : weightsByCriteria.entrySet()) {
            double value = entry.getValue() * 100;
            String label = entry.getKey() + " (" + String.format("%.1f%%", value) + ")";
            pieChart.getData().add(new PieChart.Data(label, value));
        }

        card.getChildren().add(pieChart);
        return card;
    }

    public void refresh() {
        buildUI();
    }
}
