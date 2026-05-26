package com.spk.presentation;

import com.spk.domain.Criteria;
import com.spk.domain.Score;
import com.spk.domain.Vendor;
import com.spk.presentation.components.CardPanel;
import com.spk.presentation.components.CustomButton;
import com.spk.presentation.components.Theme;
import com.spk.usecase.CriteriaUseCase;
import com.spk.usecase.ScoreUseCase;
import com.spk.usecase.VendorUseCase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreView extends JPanel {

    private final ScoreUseCase scoreUseCase = new ScoreUseCase();
    private final VendorUseCase vendorUseCase = new VendorUseCase();
    private final CriteriaUseCase criteriaUseCase = new CriteriaUseCase();

    private JComboBox<VendorItem> vendorCombo;
    private JPanel formContainer;
    private final Map<Integer, JTextField> scoreFields = new HashMap<>();
    private JLabel statusLabel;

    public ScoreView() {
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

        JLabel title = new JLabel("Penilaian Vendor");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Input nilai vendor terhadap setiap kriteria");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitle);

        // Status
        statusLabel = new JLabel();
        statusLabel.setFont(Theme.FONT_BOLD.deriveFont(12f));
        checkCompleteness();

        // Vendor selector
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorBar.setOpaque(false);
        selectorBar.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel selectLabel = new JLabel("Pilih Vendor:");
        selectLabel.setFont(Theme.FONT_BOLD);
        selectLabel.setForeground(Theme.TEXT_PRIMARY);

        vendorCombo = new JComboBox<>();
        vendorCombo.setPreferredSize(new Dimension(300, 35));
        vendorCombo.addActionListener(e -> loadScoresForVendor());

        selectorBar.add(selectLabel);
        selectorBar.add(vendorCombo);
        selectorBar.add(Box.createRigidArea(new Dimension(10, 0)));
        selectorBar.add(statusLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(selectorBar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Form container
        formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadVendors();
    }

    private void loadVendors() {
        try {
            List<Vendor> vendors = vendorUseCase.getAllVendors();
            vendorCombo.removeAllItems();
            vendorCombo.addItem(new VendorItem(-1, "-- Pilih Vendor --"));
            for (Vendor v : vendors) {
                vendorCombo.addItem(new VendorItem(v.getId(), v.getNamaVendor()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error memuat vendor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadScoresForVendor() {
        VendorItem selected = (VendorItem) vendorCombo.getSelectedItem();
        if (selected == null || selected.getId() == -1) {
            formContainer.removeAll();
            formContainer.revalidate();
            formContainer.repaint();
            return;
        }

        formContainer.removeAll();
        scoreFields.clear();

        try {
            List<Criteria> criteriaList = criteriaUseCase.getAllCriteria();
            List<Score> existingScores = scoreUseCase.getScoresByVendor(selected.getId());

            Map<Integer, Double> existingMap = new HashMap<>();
            for (Score s : existingScores) {
                existingMap.put(s.getKriteriaId(), s.getNilai());
            }

            if (criteriaList.isEmpty()) {
                JLabel emptyLabel = new JLabel("Belum ada kriteria. Tambahkan kriteria terlebih dahulu.");
                emptyLabel.setForeground(Theme.ACCENT_WARNING);
                formContainer.add(emptyLabel);
                formContainer.revalidate();
                formContainer.repaint();
                return;
            }

            CardPanel card = new CardPanel();
            card.setLayout(new BorderLayout(0, 15));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel formTitle = new JLabel("Penilaian untuk: " + selected.getName());
            formTitle.setFont(Theme.FONT_BOLD.deriveFont(16f));
            formTitle.setForeground(Theme.ACCENT_PRIMARY);
            card.add(formTitle, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.anchor = GridBagConstraints.WEST;

            // Header row
            gbc.gridy = 0;
            String[] headers = {"Kriteria", "Tipe", "Nilai"};
            for (int i = 0; i < headers.length; i++) {
                gbc.gridx = i;
                JLabel h = new JLabel(headers[i]);
                h.setFont(Theme.FONT_BOLD.deriveFont(12f));
                h.setForeground(Theme.ACCENT_PRIMARY);
                grid.add(h, gbc);
            }

            int row = 1;
            for (Criteria c : criteriaList) {
                gbc.gridy = row;

                gbc.gridx = 0;
                JLabel nameLabel = new JLabel(c.getNamaKriteria());
                nameLabel.setForeground(Theme.TEXT_PRIMARY);
                grid.add(nameLabel, gbc);

                gbc.gridx = 1;
                JLabel tipeLabel = new JLabel(c.isBenefit() ? "▲ Benefit" : "▼ Cost");
                tipeLabel.setForeground(c.isBenefit() ? Theme.ACCENT_SUCCESS : Theme.ACCENT_DANGER);
                tipeLabel.setFont(Theme.FONT_BOLD.deriveFont(11f));
                grid.add(tipeLabel, gbc);

                gbc.gridx = 2;
                JTextField valueField = new JTextField(10);
                if (existingMap.containsKey(c.getId())) {
                    valueField.setText(String.valueOf(existingMap.get(c.getId())));
                }
                scoreFields.put(c.getId(), valueField);
                grid.add(valueField, gbc);

                row++;
            }

            card.add(grid, BorderLayout.CENTER);

            // Save button
            JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnBar.setOpaque(false);
            CustomButton saveBtn = new CustomButton("💾 Simpan Penilaian");
            saveBtn.setSuccess();
            saveBtn.addActionListener(e -> saveScores(selected.getId()));
            btnBar.add(saveBtn);
            
            card.add(btnBar, BorderLayout.SOUTH);
            formContainer.add(card);

            formContainer.revalidate();
            formContainer.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveScores(int vendorId) {
        try {
            List<Score> scores = new ArrayList<>();
            for (Map.Entry<Integer, JTextField> entry : scoreFields.entrySet()) {
                String text = entry.getValue().getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Semua nilai harus diisi", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double nilai;
                try {
                    nilai = Double.parseDouble(text);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Nilai harus berupa angka", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (nilai <= 0) {
                    JOptionPane.showMessageDialog(this, "Nilai harus lebih dari 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Score score = new Score(vendorId, entry.getKey(), nilai);
                scores.add(score);
            }

            scoreUseCase.saveScores(vendorId, scores);
            JOptionPane.showMessageDialog(this, "Penilaian berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            checkCompleteness();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkCompleteness() {
        try {
            boolean complete = scoreUseCase.isScoreComplete();
            if (complete) {
                statusLabel.setText("✓ Semua penilaian lengkap");
                statusLabel.setForeground(Theme.ACCENT_SUCCESS);
            } else {
                statusLabel.setText("⚠ Penilaian belum lengkap");
                statusLabel.setForeground(Theme.ACCENT_WARNING);
            }
        } catch (Exception e) {
            statusLabel.setText("✗ Error");
            statusLabel.setForeground(Theme.ACCENT_DANGER);
        }
    }

    public void refresh() {
        loadVendors();
        formContainer.removeAll();
        formContainer.revalidate();
        formContainer.repaint();
        checkCompleteness();
    }

    private static class VendorItem {
        private final int id;
        private final String name;

        public VendorItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
