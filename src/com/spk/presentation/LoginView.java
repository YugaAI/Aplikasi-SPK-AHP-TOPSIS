package com.spk.presentation;

import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginView extends JPanel {

    public interface LoginListener {
        void onLogin(String username, String password);
    }

    private LoginListener listener;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private CustomButton loginButton;

    public LoginView() {
        setBackground(Theme.BG_PRIMARY);
        setLayout(new GridBagLayout()); // Center the card
        buildUI();
    }

    public void setLoginListener(LoginListener listener) {
        this.listener = listener;
    }

    private void buildUI() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(420, 500));

        // Logo / Icon
        JLabel icon = new JLabel("⬡", SwingConstants.CENTER);
        icon.setFont(Theme.FONT_TITLE.deriveFont(48f));
        icon.setForeground(Theme.ACCENT_PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel title = new JLabel("SPK Vendor IT", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE.deriveFont(28f));
        title.setForeground(Theme.ACCENT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("<html><center>Sistem Pendukung Keputusan<br>Metode AHP + TOPSIS</center></html>", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_REGULAR);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.BORDER_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Username field
        JPanel usernameGroup = new JPanel();
        usernameGroup.setLayout(new BoxLayout(usernameGroup, BoxLayout.Y_AXIS));
        usernameGroup.setOpaque(false);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(Theme.FONT_BOLD.deriveFont(12f));
        usernameLabel.setForeground(Theme.TEXT_SECONDARY);
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(300, 40));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameGroup.add(usernameLabel);
        usernameGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        usernameGroup.add(usernameField);

        // Password field
        JPanel passwordGroup = new JPanel();
        passwordGroup.setLayout(new BoxLayout(passwordGroup, BoxLayout.Y_AXIS));
        passwordGroup.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(Theme.FONT_BOLD.deriveFont(12f));
        passwordLabel.setForeground(Theme.TEXT_SECONDARY);
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordGroup.add(passwordLabel);
        passwordGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        passwordGroup.add(passwordField);

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setForeground(Theme.ACCENT_DANGER);
        errorLabel.setFont(Theme.FONT_REGULAR.deriveFont(12f));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);

        // Login button
        loginButton = new CustomButton("Masuk");
        loginButton.setPrimary();
        loginButton.setPreferredSize(new Dimension(300, 42));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        };
        loginButton.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // Default credentials hint
        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_REGULAR.deriveFont(12f));
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to card
        card.add(icon);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(separator);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(usernameGroup);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(passwordGroup);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(errorLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(loginButton);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(hint);

        add(card);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

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
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setVisible(false);
    }
}
