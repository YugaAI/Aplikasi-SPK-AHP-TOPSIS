package com.spk.presentation;

import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.AuthUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileView extends JPanel {

    private final AuthUseCase authUseCase = new AuthUseCase();
    private JTextField nameField;
    private JLabel usernameLabel;
    private JLabel roleLabel;

    public ProfileView() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Profil Saya");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Edit profil dan ganti password");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);
        contentContainer.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Profile card
        CardPanel profileCard = new CardPanel();
        profileCard.setLayout(new BorderLayout(0, 15));
        profileCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel profileTitle = new JLabel("☺ Informasi Profil");
        profileTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        profileTitle.setForeground(Theme.ACCENT_PRIMARY);
        profileCard.add(profileTitle, BorderLayout.NORTH);

        JPanel profileGrid = new JPanel(new GridBagLayout());
        profileGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel usernameHdr = new JLabel("Username:");
        usernameHdr.setFont(Theme.FONT_BOLD);
        profileGrid.add(usernameHdr, gbc);

        gbc.gridx = 1;
        usernameLabel = new JLabel(AuthUseCase.getCurrentUser() != null ? AuthUseCase.getCurrentUser().getUsername() : "");
        usernameLabel.setFont(Theme.FONT_REGULAR);
        profileGrid.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel roleHdr = new JLabel("Role:");
        roleHdr.setFont(Theme.FONT_BOLD);
        profileGrid.add(roleHdr, gbc);

        gbc.gridx = 1;
        roleLabel = new JLabel(AuthUseCase.getCurrentUser() != null ?
                (AuthUseCase.getCurrentUser().getRole().substring(0, 1).toUpperCase() + AuthUseCase.getCurrentUser().getRole().substring(1)) : "");
        roleLabel.setFont(Theme.FONT_BOLD);
        roleLabel.setForeground(Theme.ACCENT_PRIMARY);
        profileGrid.add(roleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel nameHdr = new JLabel("Nama Lengkap:");
        nameHdr.setFont(Theme.FONT_BOLD);
        profileGrid.add(nameHdr, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(AuthUseCase.getCurrentUser() != null ? AuthUseCase.getCurrentUser().getFullName() : "");
        nameField.setPreferredSize(new Dimension(300, 35));
        profileGrid.add(nameField, gbc);

        profileCard.add(profileGrid, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        CustomButton saveProfileBtn = new CustomButton("💾 Simpan Profil");
        saveProfileBtn.setPrimary();
        saveProfileBtn.addActionListener(e -> saveProfile());
        btnPanel.add(saveProfileBtn);
        profileCard.add(btnPanel, BorderLayout.SOUTH);

        contentContainer.add(profileCard);
        contentContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password card
        CardPanel passwordCard = new CardPanel();
        passwordCard.setLayout(new BorderLayout(0, 15));
        passwordCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel passwordTitle = new JLabel("🔑 Ganti Password");
        passwordTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
        passwordTitle.setForeground(Theme.ACCENT_WARNING);
        passwordCard.add(passwordTitle, BorderLayout.NORTH);

        JPanel passGrid = new JPanel(new GridBagLayout());
        passGrid.setOpaque(false);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel oldPassHdr = new JLabel("Password Lama:");
        oldPassHdr.setFont(Theme.FONT_BOLD);
        passGrid.add(oldPassHdr, gbc);

        gbc.gridx = 1;
        JPasswordField oldPassField = new JPasswordField();
        oldPassField.setPreferredSize(new Dimension(300, 35));
        passGrid.add(oldPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel newPassHdr = new JLabel("Password Baru:");
        newPassHdr.setFont(Theme.FONT_BOLD);
        passGrid.add(newPassHdr, gbc);

        gbc.gridx = 1;
        JPasswordField newPassField = new JPasswordField();
        newPassField.setPreferredSize(new Dimension(300, 35));
        passGrid.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel confirmPassHdr = new JLabel("Konfirmasi Password:");
        confirmPassHdr.setFont(Theme.FONT_BOLD);
        passGrid.add(confirmPassHdr, gbc);

        gbc.gridx = 1;
        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.setPreferredSize(new Dimension(300, 35));
        passGrid.add(confirmPassField, gbc);

        passwordCard.add(passGrid, BorderLayout.CENTER);

        JPanel passBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passBtnPanel.setOpaque(false);
        CustomButton changePassBtn = new CustomButton("🔑 Ubah Password");
        changePassBtn.setWarning();
        changePassBtn.addActionListener(e -> {
            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field password harus diisi", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Password baru dan konfirmasi tidak sama", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                authUseCase.changePassword(oldPass, newPass);
                JOptionPane.showMessageDialog(this, "Password berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                oldPassField.setText("");
                newPassField.setText("");
                confirmPassField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        passBtnPanel.add(changePassBtn);
        passwordCard.add(passBtnPanel, BorderLayout.SOUTH);

        contentContainer.add(passwordCard);

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void saveProfile() {
        try {
            authUseCase.updateProfile(nameField.getText());
            JOptionPane.showMessageDialog(this, "Profil berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        buildUI();
    }
}
