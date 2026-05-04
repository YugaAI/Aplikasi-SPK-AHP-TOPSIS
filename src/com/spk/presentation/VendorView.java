package com.spk.presentation;

import com.spk.domain.Vendor;
import com.spk.usecase.VendorUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * View for managing vendors/alternatives (CRUD).
 */
public class VendorView extends VBox {

    private final VendorUseCase vendorUseCase = new VendorUseCase();
    private TableView<Vendor> table;
    private ObservableList<Vendor> dataList;

    public VendorView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Data Alternatif (Vendor)");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Kelola daftar vendor IT yang akan dievaluasi");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("＋ Tambah Vendor");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddDialog());

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addBtn, refreshBtn);

        // Table
        table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data vendor"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Vendor, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Vendor, String> namaCol = new TableColumn<>("Nama Vendor");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaVendor"));
        namaCol.setPrefWidth(250);

        TableColumn<Vendor, String> descCol = new TableColumn<>("Deskripsi");
        descCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        descCol.setPrefWidth(300);

        TableColumn<Vendor, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<Vendor, Void>() {
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

        table.getColumns().addAll(idCol, namaCol, descCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        getChildren().addAll(header, toolbar, table);
    }

    private void loadData() {
        try {
            dataList = FXCollections.observableArrayList(vendorUseCase.getAllVendors());
            table.setItems(dataList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<Vendor> dialog = createFormDialog("Tambah Vendor", null);
        Optional<Vendor> result = dialog.showAndWait();
        result.ifPresent(v -> {
            try {
                vendorUseCase.createVendor(v.getNamaVendor(), v.getDeskripsi());
                loadData();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    private void showEditDialog(Vendor vendor) {
        Dialog<Vendor> dialog = createFormDialog("Edit Vendor", vendor);
        Optional<Vendor> result = dialog.showAndWait();
        result.ifPresent(v -> {
            try {
                vendorUseCase.updateVendor(vendor.getId(), v.getNamaVendor(), v.getDeskripsi());
                loadData();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    private void handleDelete(Vendor vendor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus vendor '" + vendor.getNamaVendor() + "'?\nData penilaian terkait juga akan terhapus.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Vendor");
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    vendorUseCase.deleteVendor(vendor.getId());
                    loadData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    private Dialog<Vendor> createFormDialog(String titleText, Vendor existing) {
        Dialog<Vendor> dialog = new Dialog<>();
        dialog.setTitle(titleText);
        dialog.setHeaderText(titleText);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField namaField = new TextField();
        namaField.setPromptText("Nama vendor");
        namaField.setPrefWidth(300);
        if (existing != null) namaField.setText(existing.getNamaVendor());

        TextArea descField = new TextArea();
        descField.setPromptText("Deskripsi vendor");
        descField.setPrefWidth(300);
        descField.setPrefRowCount(3);
        if (existing != null) descField.setText(existing.getDeskripsi());

        Label namaLabel = new Label("Nama Vendor:");
        namaLabel.getStyleClass().add("form-label");
        Label descLabel = new Label("Deskripsi:");
        descLabel.getStyleClass().add("form-label");

        grid.add(namaLabel, 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                Vendor v = new Vendor();
                v.setNamaVendor(namaField.getText());
                v.setDeskripsi(descField.getText());
                return v;
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
