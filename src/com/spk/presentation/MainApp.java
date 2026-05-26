package com.spk.presentation;

import com.spk.domain.User;
import com.spk.presentation.components.Sidebar;
import com.spk.presentation.components.Theme;
import com.spk.repository.DatabaseHelper;
import com.spk.usecase.AuthUseCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainApp {

    private JFrame frame;
    private JPanel mainLayout;
    private Sidebar sidebar;
    private final AuthUseCase authUseCase = new AuthUseCase();
    private JPanel contentArea;
    
    private JPanel topBar;
    private JLabel topTitle;
    private JLabel topUser;

    public void start() {
        // Initialize database
        try {
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal menginisialisasi database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        frame = new JFrame("SPK Pemilihan Vendor IT — AHP + TOPSIS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Theme.BG_PRIMARY);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseHelper.closeConnection();
            }
        });

        // Start with login screen
        showLogin();

        frame.setVisible(true);
    }

    private void showLogin() {
        frame.getContentPane().removeAll();
        
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

        frame.add(loginView, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void showMainView() {
        frame.getContentPane().removeAll();
        mainLayout = new JPanel(new BorderLayout());
        mainLayout.setBackground(Theme.BG_PRIMARY);

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
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG_PRIMARY);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top bar
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        topTitle = new JLabel("Analytics Dashboard");
        topTitle.setFont(Theme.FONT_TITLE.deriveFont(18f));
        topTitle.setForeground(Theme.TEXT_PRIMARY);

        String role = AuthUseCase.isAdmin() ? "Administrator" : "User";
        String displayName = "User";
        if (AuthUseCase.getCurrentUser() != null) {
            displayName = AuthUseCase.getCurrentUser().getFullName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = AuthUseCase.getCurrentUser().getUsername();
            }
        }
        
        topUser = new JLabel("☺ " + displayName + " — " + role);
        topUser.setFont(Theme.FONT_BOLD);
        topUser.setForeground(Theme.TEXT_PRIMARY);
        
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.setOpaque(false);
        rightTop.add(topUser);

        topBar.add(topTitle, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(Theme.BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));
        JLabel statusLabel = new JLabel("SPK Vendor IT — AHP + TOPSIS | Analytic insights in real time");
        statusLabel.setFont(Theme.FONT_REGULAR.deriveFont(11f));
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusBar.add(statusLabel);

        mainLayout.add(topBar, BorderLayout.NORTH);
        mainLayout.add(sidebar, BorderLayout.WEST);
        mainLayout.add(contentArea, BorderLayout.CENTER);
        mainLayout.add(statusBar, BorderLayout.SOUTH);

        frame.add(mainLayout);
        frame.revalidate();
        frame.repaint();

        // Navigate to default page
        if (AuthUseCase.isAdmin()) {
            navigateTo("dashboard");
        } else {
            navigateTo("result");
        }
    }

    private void navigateTo(String page) {
        contentArea.removeAll();
        
        JPanel view = null;
        switch (page) {
            case "dashboard":
                topTitle.setText("Analytics Dashboard");
                view = new DashboardView();
                break;
            case "criteria":
                topTitle.setText("Manajemen Kriteria");
                view = new CriteriaView();
                break;
            case "vendor":
                topTitle.setText("Manajemen Vendor");
                view = new VendorView();
                break;
            case "score":
                topTitle.setText("Input Penilaian");
                view = new ScoreView();
                break;
            case "ahp":
                topTitle.setText("Perhitungan AHP");
                view = new AHPView();
                break;
            case "result":
                topTitle.setText("Hasil TOPSIS & Rekomendasi");
                view = new ResultView();
                break;
            case "users":
                topTitle.setText("Manajemen Pengguna");
                view = new UserManagementView();
                break;
            case "profile":
                topTitle.setText("Profil Saya");
                view = new ProfileView();
                break;
            default:
                topTitle.setText("Not Found");
                view = new JPanel();
                view.setBackground(Theme.BG_PRIMARY);
                JLabel notFound = new JLabel("Halaman tidak ditemukan: " + page);
                notFound.setForeground(Theme.ACCENT_DANGER);
                notFound.setFont(Theme.FONT_TITLE);
                view.add(notFound);
                break;
        }
        
        if (view != null) {
            contentArea.add(view, BorderLayout.CENTER);
        }
        
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Apakah Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authUseCase.logout();
            showLogin();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainApp().start();
        });
    }
}
