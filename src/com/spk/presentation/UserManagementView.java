package com.spk.presentation;

import com.spk.domain.User;
import com.spk.usecase.UserUseCase;
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
 * View for admin user management (CRUD users + assign roles).
 */
public class UserManagementView extends VBox {

    private final UserUseCase userUseCase = new UserUseCase();
    private TableView<User> table;

    public UserManagementView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Kelola User");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Manajemen akun pengguna dan role");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("＋ Tambah User");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddDialog());

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addBtn, refreshBtn);

        // Table
        table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data user"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> nameCol = new TableColumn<>("Nama Lengkap");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> {
            String role = data.getValue().getRole();
            return new SimpleStringProperty(role.substring(0, 1).toUpperCase() + role.substring(1));
        });
        roleCol.setPrefWidth(100);

        TableColumn<User, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(280);
        actionCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✎ Edit");
            private final Button resetBtn = new Button("🔑 Reset Password");
            private final Button deleteBtn = new Button("✕ Hapus");
            {
                editBtn.getStyleClass().addAll("btn-warning", "btn-small");
                resetBtn.getStyleClass().addAll("btn-small");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                resetBtn.setOnAction(e -> showResetPasswordDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(4, editBtn, resetBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, usernameCol, nameCol, roleCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        getChildren().addAll(header, toolbar, table);
    }

    private void loadData() {
        try {
            table.setItems(FXCollections.observableArrayList(userUseCase.getAllUsers()));
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Tambah User");
        dialog.setHeaderText("Tambah User Baru");

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 4 karakter)");
        passwordField.setPrefWidth(300);

        TextField nameField = new TextField();
        nameField.setPromptText("Nama lengkap");
        nameField.setPrefWidth(300);

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("admin", "user"));
        roleCombo.setValue("user");
        roleCombo.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Nama Lengkap:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                try {
                    userUseCase.createUser(usernameField.getText(), passwordField.getText(),
                            nameField.getText(), roleCombo.getValue());
                    loadData();
                } catch (Exception e) {
                    showAlert("Error: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit User: " + user.getUsername());

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(user.getUsername());
        usernameField.setPrefWidth(300);

        TextField nameField = new TextField(user.getFullName());
        nameField.setPrefWidth(300);

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("admin", "user"));
        roleCombo.setValue(user.getRole());
        roleCombo.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Nama Lengkap:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                try {
                    userUseCase.updateUser(user.getId(), usernameField.getText(),
                            nameField.getText(), roleCombo.getValue());
                    loadData();
                } catch (Exception e) {
                    showAlert("Error: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showResetPasswordDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password untuk: " + user.getUsername());
        dialog.setContentText("Password baru:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                userUseCase.resetPassword(user.getId(), newPassword);
                showInfo("Password berhasil direset");
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
            }
        });
    }

    private void handleDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus user '" + user.getUsername() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus User");
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    userUseCase.deleteUser(user.getId());
                    loadData();
                } catch (Exception e) {
                    showAlert("Error: " + e.getMessage());
                }
            }
        });
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
        loadData();
    }
}
