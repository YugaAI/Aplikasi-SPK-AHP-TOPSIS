package com.spk.presentation;

import com.spk.usecase.AuthUseCase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * View for editing user profile and changing password.
 */
public class ProfileView extends VBox {

    private final AuthUseCase authUseCase = new AuthUseCase();
    private TextField nameField;
    private Label usernameLabel;
    private Label roleLabel;

    public ProfileView() {
        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
    }

    private void buildUI() {
        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("page-header");
        Label title = new Label("Profil Saya");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Edit profil dan ganti password");
        subtitle.getStyleClass().add("label-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Profile card
        VBox profileCard = new VBox(16);
        profileCard.getStyleClass().add("card");

        Label profileTitle = new Label("☺ Informasi Profil");
        profileTitle.getStyleClass().add("label-section");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(16);
        profileGrid.setVgap(12);

        Label usernameHdr = new Label("Username:");
        usernameHdr.getStyleClass().add("form-label");
        usernameLabel = new Label(AuthUseCase.getCurrentUser() != null ? AuthUseCase.getCurrentUser().getUsername() : "");
        usernameLabel.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 14px;");

        Label roleHdr = new Label("Role:");
        roleHdr.getStyleClass().add("form-label");
        roleLabel = new Label(AuthUseCase.getCurrentUser() != null ?
                (AuthUseCase.getCurrentUser().getRole().substring(0, 1).toUpperCase() +
                        AuthUseCase.getCurrentUser().getRole().substring(1)) : "");
        roleLabel.setStyle("-fx-text-fill: -accent-primary; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label nameHdr = new Label("Nama Lengkap:");
        nameHdr.getStyleClass().add("form-label");
        nameField = new TextField(AuthUseCase.getCurrentUser() != null ? AuthUseCase.getCurrentUser().getFullName() : "");
        nameField.setPrefWidth(300);

        profileGrid.add(usernameHdr, 0, 0);
        profileGrid.add(usernameLabel, 1, 0);
        profileGrid.add(roleHdr, 0, 1);
        profileGrid.add(roleLabel, 1, 1);
        profileGrid.add(nameHdr, 0, 2);
        profileGrid.add(nameField, 1, 2);

        Button saveProfileBtn = new Button("💾 Simpan Profil");
        saveProfileBtn.getStyleClass().add("btn-primary");
        saveProfileBtn.setOnAction(e -> saveProfile());

        profileCard.getChildren().addAll(profileTitle, profileGrid, saveProfileBtn);

        // Password card
        VBox passwordCard = new VBox(16);
        passwordCard.getStyleClass().add("card");

        Label passwordTitle = new Label("🔑 Ganti Password");
        passwordTitle.getStyleClass().add("label-section");

        GridPane passGrid = new GridPane();
        passGrid.setHgap(16);
        passGrid.setVgap(12);

        Label oldPassHdr = new Label("Password Lama:");
        oldPassHdr.getStyleClass().add("form-label");
        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Masukkan password lama");
        oldPassField.setPrefWidth(300);

        Label newPassHdr = new Label("Password Baru:");
        newPassHdr.getStyleClass().add("form-label");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Masukkan password baru (min 4 karakter)");
        newPassField.setPrefWidth(300);

        Label confirmPassHdr = new Label("Konfirmasi Password:");
        confirmPassHdr.getStyleClass().add("form-label");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Masukkan ulang password baru");
        confirmPassField.setPrefWidth(300);

        passGrid.add(oldPassHdr, 0, 0);
        passGrid.add(oldPassField, 1, 0);
        passGrid.add(newPassHdr, 0, 1);
        passGrid.add(newPassField, 1, 1);
        passGrid.add(confirmPassHdr, 0, 2);
        passGrid.add(confirmPassField, 1, 2);

        Button changePassBtn = new Button("🔑 Ubah Password");
        changePassBtn.getStyleClass().add("btn-warning");
        changePassBtn.setOnAction(e -> {
            String oldPass = oldPassField.getText();
            String newPass = newPassField.getText();
            String confirm = confirmPassField.getText();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                showAlert("Semua field password harus diisi");
                return;
            }
            if (!newPass.equals(confirm)) {
                showAlert("Password baru dan konfirmasi tidak sama");
                return;
            }

            try {
                authUseCase.changePassword(oldPass, newPass);
                showInfo("Password berhasil diubah!");
                oldPassField.clear();
                newPassField.clear();
                confirmPassField.clear();
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });

        passwordCard.getChildren().addAll(passwordTitle, passGrid, changePassBtn);

        getChildren().addAll(header, profileCard, passwordCard);
    }

    private void saveProfile() {
        try {
            authUseCase.updateProfile(nameField.getText());
            showInfo("Profil berhasil disimpan!");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
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
        getChildren().clear();
        buildUI();
    }
}
