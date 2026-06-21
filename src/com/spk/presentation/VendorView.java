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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.spk.domain.Vendor;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.VendorUseCase;

public class VendorView extends JPanel {

    private final VendorUseCase vendorUseCase = new VendorUseCase();
    private JTable table;
    private DefaultTableModel tableModel;

    public VendorView() {
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

        JLabel title = new JLabel("Data Alternatif (Vendor)");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Kelola daftar vendor IT yang akan dievaluasi");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));

        CustomButton addBtn = new CustomButton("Tambah Vendor");
        addBtn.setPrimary();
        addBtn.addActionListener(e -> showAddDialog());

        CustomButton refreshBtn = new CustomButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        CustomButton editBtn = new CustomButton("Edit Terpilih");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                String desc = (String) tableModel.getValueAt(row, 2);
                Vendor v = new Vendor();
                v.setId(id);
                v.setNamaVendor(name);
                v.setAlamat(desc);
                showEditDialog(v);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih vendor yang akan diedit", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        CustomButton deleteBtn = new CustomButton("Hapus Terpilih");
        deleteBtn.setDanger();
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                handleDelete(id, name);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih vendor yang akan dihapus", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toolbar.add(addBtn);
        toolbar.add(refreshBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Nama Vendor", "Alamat"};
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
            List<Vendor> vendors = vendorUseCase.getAllVendors();
            for (Vendor v : vendors) {
                tableModel.addRow(new Object[]{v.getId(), v.getNamaVendor(), v.getAlamat()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JTextField namaField = new JTextField();
        JTextArea descField = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descField);

        form.add(new JLabel("Nama Vendor:"));
        form.add(namaField);
        form.add(new JLabel("Alamat:"));
        form.add(new JLabel("")); // Spacer

        panel.add(form, BorderLayout.NORTH);
        panel.add(descScroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Vendor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                vendorUseCase.createVendor(namaField.getText(), descField.getText());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(Vendor vendor) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JTextField namaField = new JTextField(vendor.getNamaVendor());
        JTextArea descField = new JTextArea(vendor.getAlamat(), 3, 20);
        JScrollPane descScroll = new JScrollPane(descField);

        form.add(new JLabel("Nama Vendor:"));
        form.add(namaField);
        form.add(new JLabel("Alamat:"));
        form.add(new JLabel(""));

        panel.add(form, BorderLayout.NORTH);
        panel.add(descScroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Vendor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                vendorUseCase.updateVendor(vendor.getId(), namaField.getText(), descField.getText());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(int id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus vendor '" + name + "'?\nData penilaian terkait juga akan terhapus.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                vendorUseCase.deleteVendor(id);
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
