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

    // Derived tones — sidebar selalu gelap (BG_SIDEBAR), jadi overlay
    // putih-transparan dipakai untuk divider/hover, bukan token Theme baru.
    private static final Color DIVIDER     = new Color(255, 255, 255, 20);
    private static final Color HOVER_FILL  = new Color(255, 255, 255, 15);
    private static final Color ACTIVE_FILL = withAlpha(Theme.ACCENT_PRIMARY, 46);
    private static final Color TEXT_MUTED_ON_DARK = new Color(255, 255, 255, 153);
    private static final Color TEXT_DIM_ON_DARK    = new Color(255, 255, 255, 76);
    private static final Color SUBTITLE_ON_DARK    = new Color(255, 255, 255, 115);

    public Sidebar() {
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(240, 600));
        setMinimumSize(new Dimension(240, 0));
        setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        setLayout(new BorderLayout());
        buildUI();
    }

    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    private static Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private void buildUI() {
        removeAll();
        navButtons.clear();

        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                new EmptyBorder(20, 18, 18, 18)
        ));

        JLabel title = new JLabel("SPK Vendor IT");
        title.setFont(Theme.FONT_BOLD.deriveFont(14f));
        title.setForeground(Theme.TEXT_ON_ACCENT);

        JLabel subtitle = new JLabel("AHP + TOPSIS Method");
        subtitle.setFont(Theme.FONT_REGULAR.deriveFont(10f));
        subtitle.setForeground(SUBTITLE_ON_DARK);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 2)));
        header.add(subtitle);

        // ── Container ────────────────────────────────────────────
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(header);

        // ── User info ────────────────────────────────────────────
        if (AuthUseCase.getCurrentUser() != null) {
            JPanel userInfo = new JPanel();
            userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
            userInfo.setOpaque(false);
            userInfo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                    new EmptyBorder(12, 18, 12, 18)
            ));

            String name = AuthUseCase.getCurrentUser().getFullName();
            if (name == null || name.isEmpty())
                name = AuthUseCase.getCurrentUser().getUsername();

            JLabel userName = new JLabel(name);
            userName.setForeground(Theme.TEXT_ON_ACCENT);
            userName.setFont(Theme.FONT_BOLD.deriveFont(12f));

            boolean isAdmin = AuthUseCase.isAdmin();
            JLabel userRole = new JLabel(isAdmin ? "Administrator" : "User");
            userRole.setForeground(isAdmin ? Theme.ACCENT_SUCCESS : Theme.ACCENT_PRIMARY);
            userRole.setFont(Theme.FONT_REGULAR.deriveFont(10f));

            userInfo.add(userName);
            userInfo.add(Box.createRigidArea(new Dimension(0, 3)));
            userInfo.add(userRole);
            topContainer.add(userInfo);
        }

        // ── Navigation ───────────────────────────────────────────
        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        if (AuthUseCase.isAdmin()) {
            addSectionLabel("MENU UTAMA");
            addNavButton("Dashboard",    "dashboard");
            addNavButton("Kriteria",     "criteria");
            addNavButton("Alternatif",   "vendor");
            addNavButton("Penilaian",    "score");

            addSectionLabel("PERHITUNGAN");
            addNavButton("AHP (Bobot)",  "ahp");
            addNavButton("Hasil TOPSIS", "result");

            addSectionLabel("PENGATURAN");
            addNavButton("Kelola User",  "users");
            addNavButton("Profil Saya",  "profile");
        } else {
            addSectionLabel("MENU");
            addNavButton("Hasil Ranking", "result");
            addNavButton("Profil Saya",   "profile");
        }

        topContainer.add(navPanel);
        add(topContainer, BorderLayout.NORTH);

        // ── Logout ───────────────────────────────────────────────
        JPanel logoutBox = new JPanel(new BorderLayout());
        logoutBox.setOpaque(false);
        logoutBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                new EmptyBorder(10, 12, 14, 12)
        ));

        LogoutButton logoutBtn = new LogoutButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(216, 34));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) listener.onNavigate("logout");
            }
        });

        logoutBox.add(logoutBtn, BorderLayout.CENTER);
        add(logoutBox, BorderLayout.SOUTH);

        setActive(activePage);
        revalidate();
        repaint();
    }

    private void addSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD.deriveFont(9.5f));
        label.setForeground(TEXT_DIM_ON_DARK);
        label.setBorder(new EmptyBorder(14, 8, 5, 8));
        label.setAlignmentX(LEFT_ALIGNMENT);
        navPanel.add(label);
    }

    private void addNavButton(String text, String page) {
        NavButton btn = new NavButton(text, page);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activePage = page;
                setActive(page);
                if (listener != null) listener.onNavigate(page);
            }
        });
        navButtons.add(btn);
        navPanel.add(btn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 2)));
    }

    private void setActive(String page) {
        for (NavButton btn : navButtons) {
            btn.setActive(btn.getPage().equals(page));
        }
    }

    public void refresh() {
        buildUI();
    }

    // ── NavButton ────────────────────────────────────────────────
    private class NavButton extends JPanel {
        private final String page;
        private boolean isActive  = false;
        private boolean isHovered = false;
        private final JLabel label;

        public NavButton(String text, String page) {
            this.page = page;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setMaximumSize(new Dimension(220, 36));
            setPreferredSize(new Dimension(220, 36));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            label = new JLabel(text);
            label.setFont(Theme.FONT_REGULAR.deriveFont(12.5f));
            label.setForeground(TEXT_MUTED_ON_DARK);
            add(label, BorderLayout.WEST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        public String getPage() { return page; }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                label.setFont(Theme.FONT_BOLD.deriveFont(12.5f));
                label.setForeground(Theme.TEXT_ON_ACCENT);
                setBorder(new EmptyBorder(8, 7, 8, 10)); // kompensasi left accent bar 3px
            } else {
                label.setFont(Theme.FONT_REGULAR.deriveFont(12.5f));
                label.setForeground(TEXT_MUTED_ON_DARK);
                setBorder(new EmptyBorder(8, 10, 8, 10));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(ACTIVE_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.fillRect(0, 6, 3, getHeight() - 12);
            } else if (isHovered) {
                g2.setColor(HOVER_FILL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── LogoutButton ─────────────────────────────────────────────
    private class LogoutButton extends JPanel {
        private boolean isHovered = false;

        public LogoutButton(String text) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel label = new JLabel(text, JLabel.CENTER);
            label.setFont(Theme.FONT_BOLD.deriveFont(12.5f));
            label.setForeground(Theme.TEXT_ON_ACCENT);
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = isHovered ? Theme.ACCENT_DANGER.brighter() : Theme.ACCENT_DANGER;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}