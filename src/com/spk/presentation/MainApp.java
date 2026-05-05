package com.spk.presentation;

import com.spk.domain.User;
import com.spk.presentation.components.Sidebar;
import com.spk.repository.DatabaseHelper;
import com.spk.usecase.AuthUseCase;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Main JavaFX Application entry point.
 * Handles navigation between login and main views.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private Scene scene;
    private BorderPane mainLayout;
    private Sidebar sidebar;
    private final AuthUseCase authUseCase = new AuthUseCase();
    private StackPane contentArea;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Initialize database
        try {
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Gagal menginisialisasi database:\n" + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            System.exit(1);
        }

        // Start with login screen
        showLogin();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setTitle("SPK Pemilihan Vendor IT — AHP + TOPSIS");
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.show();

        stage.setOnCloseRequest(e -> {
            DatabaseHelper.closeConnection();
        });
    }

    private void showLogin() {
        LoginView loginView = new LoginView();
        loginView.setLoginListener((username, password) -> {
            try {
                User user = authUseCase.login(username, password);
                if (user != null) {
                    showMainView();
                } else {
                    loginView.showError("Username atau password salah");
                }
            } catch (Exception e) {
                loginView.showError(e.getMessage());
            }
        });

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        scene = new Scene(loginView, screenBounds.getWidth(), screenBounds.getHeight());
        applyCSS(scene);
        primaryStage.setScene(scene);
    }

    private void showMainView() {
        mainLayout = new BorderPane();

        // Sidebar
        sidebar = new Sidebar();
        sidebar.setNavigationListener(page -> {
            if ("logout".equals(page)) {
                handleLogout();
            } else {
                navigateTo(page);
            }
        });

        // Content area
        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_LEFT);

        // Top bar
        HBox topBar = new HBox();
        topBar.getStyleClass().add("topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label topTitle = new Label("Analytics Dashboard");
        topTitle.getStyleClass().add("topbar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String role = AuthUseCase.isAdmin() ? "Administrator" : "User";
        String displayName = "User";
        if (AuthUseCase.getCurrentUser() != null) {
            displayName = AuthUseCase.getCurrentUser().getFullName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = AuthUseCase.getCurrentUser().getUsername();
            }
        }
        Label topUser = new Label("☺ " + displayName + " — " + role);
        topUser.getStyleClass().add("topbar-user");

        topBar.getChildren().addAll(topTitle, spacer, topUser);

        // Status bar
        HBox statusBar = new HBox();
        statusBar.getStyleClass().add("status-bar");
        Label statusLabel = new Label("SPK Vendor IT — AHP + TOPSIS | Analytic insights in real time");
        statusBar.getChildren().add(statusLabel);

        mainLayout.setTop(topBar);
        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(contentArea);
        mainLayout.setBottom(statusBar);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        scene = new Scene(mainLayout, screenBounds.getWidth(), screenBounds.getHeight());
        applyCSS(scene);
        primaryStage.setScene(scene);

        // Navigate to default page
        if (AuthUseCase.isAdmin()) {
            navigateTo("dashboard");
        } else {
            navigateTo("result");
        }
    }

    private void navigateTo(String page) {
        contentArea.getChildren().clear();
        switch (page) {
            case "dashboard":
                contentArea.getChildren().add(new DashboardView());
                break;
            case "criteria":
                contentArea.getChildren().add(new CriteriaView());
                break;
            case "vendor":
                contentArea.getChildren().add(new VendorView());
                break;
            case "score":
                contentArea.getChildren().add(new ScoreView());
                break;
            case "ahp":
                contentArea.getChildren().add(new AHPView());
                break;
            case "result":
                contentArea.getChildren().add(new ResultView());
                break;
            case "users":
                contentArea.getChildren().add(new UserManagementView());
                break;
            case "profile":
                contentArea.getChildren().add(new ProfileView());
                break;
            default:
                Label notFound = new Label("Halaman tidak ditemukan: " + page);
                notFound.setStyle("-fx-text-fill: -accent-danger; -fx-font-size: 16px; -fx-padding: 40;");
                contentArea.getChildren().add(notFound);
                break;
        }
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Apakah Anda yakin ingin logout?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Logout");
        confirm.setHeaderText("Logout");
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                authUseCase.logout();
                showLogin();
            }
        });
    }

    private void applyCSS(Scene scene) {
        try {
            String css = getClass().getResource("/com/spk/presentation/styles/neumorphism.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Warning: Could not load CSS theme: " + e.getMessage());
            // Try alternative path
            try {
                String css = getClass().getClassLoader().getResource("com/spk/presentation/styles/neumorphism.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e2) {
                System.err.println("Warning: CSS theme not found (will use defaults)");
            }
        }
    }

    @Override
    public void stop() {
        DatabaseHelper.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
