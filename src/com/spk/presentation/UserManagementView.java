package com.spk.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.spk.domain.User;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.UserUseCase;

public class UserManagementView extends JPanel {

    private final UserUseCase userUseCase = new UserUseCase();
    private JTable table;
    private DefaultTableModel tableModel;

    public UserManagementView() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        removeAll();

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Kelola User");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manajemen akun pengguna dan role");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));

        CustomButton addBtn = new CustomButton("Tambah User");
        addBtn.setPrimary();
        addBtn.addActionListener(e -> showAddDialog());

        CustomButton refreshBtn = new CustomButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        CustomButton editBtn = new CustomButton("Edit");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                User u = getUserFromRow(row);
                showEditDialog(u);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user yang akan diedit", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        CustomButton resetBtn = new CustomButton("Reset Pass");
        resetBtn.setWarning();
        resetBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                User u = getUserFromRow(row);
                showResetPasswordDialog(u);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user yang akan direset", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        CustomButton deleteBtn = new CustomButton("Hapus");
        deleteBtn.setDanger();
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                User u = getUserFromRow(row);
                handleDelete(u);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user yang akan dihapus", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toolbar.add(addBtn);
        toolbar.add(refreshBtn);
        toolbar.add(editBtn);
        toolbar.add(resetBtn);
        toolbar.add(deleteBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Username", "Nama Lengkap", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(Theme.FONT_REGULAR);
        table.getTableHeader().setFont(Theme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(37, 52, 85));
        table.getTableHeader().setForeground(Theme.ACCENT_PRIMARY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userUseCase.getAllUsers();
            for (User u : users) {
                String role = u.getRole().substring(0, 1).toUpperCase() + u.getRole().substring(1);
                tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), role});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private User getUserFromRow(int row) {
        User u = new User();
        u.setId((int) tableModel.getValueAt(row, 0));
        u.setUsername((String) tableModel.getValueAt(row, 1));
        u.setFullName((String) tableModel.getValueAt(row, 2));
        u.setRole(((String) tableModel.getValueAt(row, 3)).toLowerCase());
        return u;
    }

    private void showAddDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nameField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "user"});
        roleCombo.setSelectedItem("user");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Nama Lengkap:"));
        panel.add(nameField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                userUseCase.createUser(usernameField.getText(), new String(passwordField.getPassword()),
                        nameField.getText(), (String) roleCombo.getSelectedItem());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(User user) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JTextField usernameField = new JTextField(user.getUsername());
        JTextField nameField = new JTextField(user.getFullName());
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "user"});
        roleCombo.setSelectedItem(user.getRole());

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Nama Lengkap:"));
        panel.add(nameField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                userUseCase.updateUser(user.getId(), usernameField.getText(),
                        nameField.getText(), (String) roleCombo.getSelectedItem());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showResetPasswordDialog(User user) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Password baru untuk " + user.getUsername() + ":"), BorderLayout.NORTH);
        JPasswordField pf = new JPasswordField(20);
        panel.add(pf, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                userUseCase.resetPassword(user.getId(), new String(pf.getPassword()));
                JOptionPane.showMessageDialog(this, "Password berhasil direset", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(User user) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus user '" + user.getUsername() + "'?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userUseCase.deleteUser(user.getId());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refresh() {
        loadData();
    }
}
