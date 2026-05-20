package com.spk.presentation;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

/**
 * Login screen view.
 */
public class LoginView extends StackPane {

    public interface LoginListener {
        void onLogin(String username, String password);
    }

    private LoginListener listener;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView() {
        getStyleClass().add("login-container");
        buildUI();
    }

    public void setLoginListener(LoginListener listener) {
        this.listener = listener;
    }

    private void buildUI() {
        // Card
        VBox card = new VBox(20);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);

        // Logo / Icon
        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: -accent-primary;");
        icon.setAlignment(Pos.CENTER);
        icon.setMaxWidth(Double.MAX_VALUE);
        icon.setTextAlignment(TextAlignment.CENTER);

        // Title
        Label title = new Label("SPK Vendor IT");
        title.getStyleClass().add("login-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setTextAlignment(TextAlignment.CENTER);

        // Subtitle
        Label subtitle = new Label("Sistem Pendukung Keputusan\nMetode AHP + TOPSIS");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(Double.MAX_VALUE);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        // Username field
        VBox usernameGroup = new VBox(6);
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("form-label");
        usernameField = new TextField();
        usernameField.setPromptText("Masukkan username");
        usernameField.setPrefHeight(40);
        usernameGroup.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordGroup = new VBox(6);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("form-label");
        passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password");
        passwordField.setPrefHeight(40);
        passwordGroup.getChildren().addAll(passwordLabel, passwordField);

        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setTextAlignment(TextAlignment.CENTER);

        // Login button
        loginButton = new Button("Masuk");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setPrefHeight(42);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());

        // Enter key support
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Default credentials hint
        Label hint = new Label("Default: admin / admin123");
        hint.getStyleClass().add("label-muted");
        hint.setAlignment(Pos.CENTER);
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(icon, title, subtitle,
                new Separator(),
                usernameGroup, passwordGroup, errorLabel,
                loginButton, hint);

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Username tidak boleh kosong");
            return;
        }
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong");
            return;
        }

        errorLabel.setVisible(false);
        if (listener != null) {
            listener.onLogin(username, password);
        }
    }

    public void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
    }

    public void clearFields() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
    }
}
