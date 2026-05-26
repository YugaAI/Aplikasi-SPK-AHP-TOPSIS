package com.spk.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import com.spk.usecase.AuthUseCase;

public class Sidebar extends JPanel {

    public interface NavigationListener {
        void onNavigate(String page);
    }

    private NavigationListener listener;
    private final List<NavButton> navButtons = new ArrayList<>();
    private String activePage = "dashboard";
    private JPanel navPanel;

    public Sidebar() {
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(260, 600));
        setMinimumSize(new Dimension(260, 0));
        setMaximumSize(new Dimension(260, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        buildUI();
    }

    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    private void buildUI() {
        removeAll();
        navButtons.clear();

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BorderLayout());
        topContainer.setOpaque(false);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(30, 144, 255, 30));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 20)),
                new EmptyBorder(24, 20, 20, 20)
        ));
        
        JLabel title = new JLabel("⬡ SPK Vendor IT");
        title.setFont(Theme.FONT_TITLE.deriveFont(16f));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("AHP + TOPSIS Method");
        subtitle.setFont(Theme.FONT_REGULAR.deriveFont(11f));
        subtitle.setForeground(new Color(255, 255, 255, 180));
        
        header.add(title);
        header.add(subtitle);
        topContainer.add(header);

        // User info
        if (AuthUseCase.getCurrentUser() != null) {
            JPanel userInfo = new JPanel();
            userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
            userInfo.setOpaque(false);
            userInfo.setBorder(new EmptyBorder(14, 20, 10, 20));
            
            String name = AuthUseCase.getCurrentUser().getFullName();
            if (name == null || name.isEmpty())
                name = AuthUseCase.getCurrentUser().getUsername();
                
            JLabel userName = new JLabel("☺ " + name);
            userName.setForeground(Color.WHITE);
            userName.setFont(Theme.FONT_REGULAR);
            
            JLabel userRole = new JLabel(AuthUseCase.isAdmin() ? "● Administrator" : "● User");
            userRole.setForeground(AuthUseCase.isAdmin() ? Theme.ACCENT_SUCCESS : Theme.ACCENT_PRIMARY);
            userRole.setFont(Theme.FONT_REGULAR.deriveFont(11f));
            
            userInfo.add(userName);
            userInfo.add(userRole);
            topContainer.add(userInfo);
            
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(255, 255, 255, 30));
            topContainer.add(sep);
        }

        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

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
        
        topContainer.add(navPanel);
        add(topContainer, BorderLayout.NORTH);

        // Logout button at bottom
        JPanel logoutBox = new JPanel(new BorderLayout());
        logoutBox.setOpaque(false);
        logoutBox.setBorder(new EmptyBorder(10, 16, 16, 16));
        
        CustomButton logoutBtn = new CustomButton("⏻  Logout");
        logoutBtn.setDanger();
        logoutBtn.setPreferredSize(new Dimension(198, 35));
        logoutBtn.addActionListener(e -> {
            if (listener != null)
                listener.onNavigate("logout");
        });
        
        logoutBox.add(logoutBtn, BorderLayout.CENTER);
        add(logoutBox, BorderLayout.SOUTH);

        // Set initial active
        setActive(activePage);
        
        revalidate();
        repaint();
    }

    private void addSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD.deriveFont(10f));
        label.setForeground(new Color(255, 255, 255, 160));
        label.setBorder(new EmptyBorder(16, 10, 6, 10));
        navPanel.add(label);
    }

    private void addNavButton(String text, String page) {
        NavButton btn = new NavButton(text, page);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activePage = page;
                setActive(page);
                if (listener != null)
                    listener.onNavigate(page);
            }
        });
        navButtons.add(btn);
        navPanel.add(btn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void setActive(String page) {
        for (NavButton btn : navButtons) {
            btn.setActive(btn.getPage().equals(page));
        }
    }

    public void refresh() {
        buildUI();
    }
    
    // Custom Nav Button Class
    private class NavButton extends JPanel {
        private String page;
        private boolean isActive = false;
        private boolean isHovered = false;
        private JLabel label;

        public NavButton(String text, String page) {
            this.page = page;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setMaximumSize(new Dimension(240, 40));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            label = new JLabel(text);
            label.setFont(Theme.FONT_REGULAR);
            label.setForeground(new Color(255, 255, 255, 220));
            add(label, BorderLayout.WEST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public String getPage() { return page; }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                label.setFont(Theme.FONT_BOLD);
                label.setForeground(Color.WHITE);
            } else {
                label.setFont(Theme.FONT_REGULAR);
                label.setForeground(new Color(255, 255, 255, 220));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(new Color(30, 144, 255, 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Left border indicator
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.fillRoundRect(0, 6, 4, getHeight() - 12, 4, 4);
            } else if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
