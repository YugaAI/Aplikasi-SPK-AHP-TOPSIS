package com.spk.presentation;

import com.spk.domain.Criteria;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.CriteriaUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CriteriaView extends JPanel {

    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();
    private JTable table;
    private DefaultTableModel tableModel;

    public CriteriaView() {
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

        JLabel title = new JLabel("Data Kriteria");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Kelola kriteria penilaian vendor (benefit/cost)");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));

        CustomButton addBtn = new CustomButton("＋ Tambah Kriteria");
        addBtn.setPrimary();
        addBtn.addActionListener(e -> showAddDialog());

        CustomButton refreshBtn = new CustomButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadData());

        CustomButton editBtn = new CustomButton("✎ Edit Terpilih");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                String type = (String) tableModel.getValueAt(row, 2);
                Criteria c = new Criteria();
                c.setId(id);
                c.setNamaKriteria(name);
                c.setTipeKriteria(type.toLowerCase());
                showEditDialog(c);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih kriteria yang akan diedit", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        CustomButton deleteBtn = new CustomButton("✕ Hapus Terpilih");
        deleteBtn.setDanger();
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                handleDelete(id, name);
            } else {
                JOptionPane.showMessageDialog(this, "Pilih kriteria yang akan dihapus", "Info", JOptionPane.INFORMATION_MESSAGE);
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
        String[] columnNames = {"ID", "Nama Kriteria", "Tipe"};
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
            List<Criteria> criteriaList = criteriaUseCase.getAllCriteria();
            for (Criteria c : criteriaList) {
                String tipe = c.getTipeKriteria();
                String tipeDisplay = tipe.substring(0, 1).toUpperCase() + tipe.substring(1);
                tableModel.addRow(new Object[]{c.getId(), c.getNamaKriteria(), tipeDisplay});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField namaField = new JTextField();
        JComboBox<String> tipeCombo = new JComboBox<>(new String[]{"benefit", "cost"});

        panel.add(new JLabel("Nama Kriteria:"));
        panel.add(namaField);
        panel.add(new JLabel("Tipe Kriteria:"));
        panel.add(tipeCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Kriteria",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                criteriaUseCase.createCriteria(namaField.getText(), (String) tipeCombo.getSelectedItem());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(Criteria criteria) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField namaField = new JTextField(criteria.getNamaKriteria());
        JComboBox<String> tipeCombo = new JComboBox<>(new String[]{"benefit", "cost"});
        tipeCombo.setSelectedItem(criteria.getTipeKriteria());

        panel.add(new JLabel("Nama Kriteria:"));
        panel.add(namaField);
        panel.add(new JLabel("Tipe Kriteria:"));
        panel.add(tipeCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Kriteria",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                criteriaUseCase.updateCriteria(criteria.getId(), namaField.getText(), (String) tipeCombo.getSelectedItem());
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(int id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus kriteria '" + name + "'?\nData pairwise dan score terkait juga akan terhapus.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                criteriaUseCase.deleteCriteria(id);
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
