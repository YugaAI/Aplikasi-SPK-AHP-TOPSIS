package com.spk.presentation;

import com.spk.domain.Criteria;
import com.spk.usecase.CriteriaUseCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * View for managing criteria (CRUD).
 */
public class CriteriaView extends VBox {

    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();
    private TableView<Criteria> table;
    private ObservableList<Criteria> dataList;

    public CriteriaView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Data Kriteria");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Kelola kriteria penilaian vendor (benefit/cost)");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("＋ Tambah Kriteria");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddDialog());

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addBtn, refreshBtn);

        // Table
        table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data kriteria"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Criteria, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Criteria, String> namaCol = new TableColumn<>("Nama Kriteria");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaKriteria"));
        namaCol.setPrefWidth(250);

        TableColumn<Criteria, String> tipeCol = new TableColumn<>("Tipe");
        tipeCol.setCellValueFactory(data -> {
            String tipe = data.getValue().getTipeKriteria();
            return new SimpleStringProperty(tipe.substring(0, 1).toUpperCase() + tipe.substring(1));
        });
        tipeCol.setPrefWidth(120);

        TableColumn<Criteria, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<Criteria, Void>() {
            private final Button editBtn = new Button("✎ Edit");
            private final Button deleteBtn = new Button("✕ Hapus");
            {
                editBtn.getStyleClass().addAll("btn-warning", "btn-small");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, namaCol, tipeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        getChildren().addAll(header, toolbar, table);
    }

    private void loadData() {
        try {
            dataList = FXCollections.observableArrayList(criteriaUseCase.getAllCriteria());
            table.setItems(dataList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<Criteria> dialog = createFormDialog("Tambah Kriteria", null);
        Optional<Criteria> result = dialog.showAndWait();
        result.ifPresent(c -> {
            try {
                criteriaUseCase.createCriteria(c.getNamaKriteria(), c.getTipeKriteria());
                loadData();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    private void showEditDialog(Criteria criteria) {
        Dialog<Criteria> dialog = createFormDialog("Edit Kriteria", criteria);
        Optional<Criteria> result = dialog.showAndWait();
        result.ifPresent(c -> {
            try {
                criteriaUseCase.updateCriteria(criteria.getId(), c.getNamaKriteria(), c.getTipeKriteria());
                loadData();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    private void handleDelete(Criteria criteria) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus kriteria '" + criteria.getNamaKriteria() + "'?\nData pairwise dan score terkait juga akan terhapus.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Kriteria");
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    criteriaUseCase.deleteCriteria(criteria.getId());
                    loadData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    private Dialog<Criteria> createFormDialog(String titleText, Criteria existing) {
        Dialog<Criteria> dialog = new Dialog<>();
        dialog.setTitle(titleText);
        dialog.setHeaderText(titleText);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField namaField = new TextField();
        namaField.setPromptText("Nama kriteria");
        namaField.setPrefWidth(300);
        if (existing != null) namaField.setText(existing.getNamaKriteria());

        ComboBox<String> tipeCombo = new ComboBox<>(FXCollections.observableArrayList("benefit", "cost"));
        tipeCombo.setPromptText("Pilih tipe");
        tipeCombo.setPrefWidth(300);
        if (existing != null) tipeCombo.setValue(existing.getTipeKriteria());

        Label namaLabel = new Label("Nama Kriteria:");
        namaLabel.getStyleClass().add("form-label");
        Label tipeLabel = new Label("Tipe Kriteria:");
        tipeLabel.getStyleClass().add("form-label");

        grid.add(namaLabel, 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(tipeLabel, 0, 1);
        grid.add(tipeCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                Criteria c = new Criteria();
                c.setNamaKriteria(namaField.getText());
                c.setTipeKriteria(tipeCombo.getValue());
                return c;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public void refresh() {
        loadData();
    }
}
