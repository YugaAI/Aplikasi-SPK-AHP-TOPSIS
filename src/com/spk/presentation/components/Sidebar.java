package com.spk.presentation.components;

import com.spk.usecase.AuthUseCase;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Sidebar navigation component.
 */
public class Sidebar extends VBox {

    public interface NavigationListener {
        void onNavigate(String page);
    }

    private NavigationListener listener;
    private final List<Button> navButtons = new ArrayList<>();
    private String activePage = "dashboard";

    public Sidebar() {
        getStyleClass().add("sidebar");
        setPrefWidth(230);
        setMinWidth(230);
        setMaxWidth(230);

        buildUI();
    }

    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    private void buildUI() {
        getChildren().clear();
        navButtons.clear();

        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");
        Label title = new Label("⬡ SPK Vendor IT");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("AHP + TOPSIS Method");
        subtitle.getStyleClass().add("sidebar-subtitle");
        header.getChildren().addAll(title, subtitle);

        getChildren().add(header);

        // User info
        if (AuthUseCase.getCurrentUser() != null) {
            VBox userInfo = new VBox(2);
            userInfo.setPadding(new Insets(14, 20, 10, 20));
            String name = AuthUseCase.getCurrentUser().getFullName();
            if (name == null || name.isEmpty())
                name = AuthUseCase.getCurrentUser().getUsername();
            Label userName = new Label("☺ " + name);
            userName.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 13px;");
            Label userRole = new Label(AuthUseCase.isAdmin() ? "● Administrator" : "● User");
            userRole.setStyle("-fx-text-fill: " + (AuthUseCase.isAdmin() ? "-accent-success" : "-accent-primary")
                    + "; -fx-font-size: 11px;");
            userInfo.getChildren().addAll(userName, userRole);
            getChildren().add(userInfo);
        }

        getChildren().add(new Separator());

        // Navigation items
        if (AuthUseCase.isAdmin()) {
            addSectionLabel("MENU UTAMA");
            addNavButton("▣  Dashboard", "dashboard");
            addNavButton("◈  Kriteria", "criteria");
            addNavButton("◉  Alternatif", "vendor");
            addNavButton("✎  Penilaian", "score");

            addSectionLabel("PERHITUNGAN");
            addNavButton("△  AHP (Bobot)", "ahp");
            addNavButton("◎  Hasil TOPSIS", "result");

            addSectionLabel("PENGATURAN");
            addNavButton("☷  Kelola User", "users");
            addNavButton("☺  Profil Saya", "profile");
        } else {
            addSectionLabel("MENU");
            addNavButton("◎  Hasil Ranking", "result");
            addNavButton("☺  Profil Saya", "profile");
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Logout button at bottom
        VBox logoutBox = new VBox();
        logoutBox.setPadding(new Insets(10, 16, 16, 16));
        Button logoutBtn = new Button("⏻  Logout");
        logoutBtn.getStyleClass().addAll("btn-danger", "btn-small");
        logoutBtn.setPrefWidth(198);
        logoutBtn.setOnAction(e -> {
            if (listener != null)
                listener.onNavigate("logout");
        });
        logoutBox.getChildren().add(logoutBtn);
        getChildren().add(logoutBox);

        // Set initial active
        setActive(activePage);
    }

    private void addSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("nav-section-label");
        getChildren().add(label);
    }

    private void addNavButton(String text, String page) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setOnAction(e -> {
            activePage = page;
            setActive(page);
            if (listener != null)
                listener.onNavigate(page);
        });
        navButtons.add(btn);
        getChildren().add(btn);
    }

    private void setActive(String page) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        // Find button matching page
        for (Button btn : navButtons) {
            String btnPage = getPageFromButton(btn);
            if (page.equals(btnPage)) {
                btn.getStyleClass().add("nav-btn-active");
                break;
            }
        }
    }

    private String getPageFromButton(Button btn) {
        String text = btn.getText().toLowerCase();
        if (text.contains("dashboard"))
            return "dashboard";
        if (text.contains("kriteria"))
            return "criteria";
        if (text.contains("alternatif"))
            return "vendor";
        if (text.contains("penilaian"))
            return "score";
        if (text.contains("ahp"))
            return "ahp";
        if (text.contains("hasil") || text.contains("ranking"))
            return "result";
        if (text.contains("user"))
            return "users";
        if (text.contains("profil"))
            return "profile";
        return "";
    }

    public void refresh() {
        buildUI();
    }
}
