package com.spk.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spk.domain.Criteria;

/**
 * Repository for Criteria CRUD operations.
 */
public class CriteriaRepository {

    public List<Criteria> findAll() throws SQLException {
        List<Criteria> list = new ArrayList<>();
        String sql = "SELECT * FROM criteria ORDER BY id";
        try (Statement stmt = DatabaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Criteria findById(int id) throws SQLException {
        String sql = "SELECT * FROM criteria WHERE id = ?";
        try (PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void insert(Criteria criteria) throws SQLException {
        String sql = "INSERT INTO criteria (nama_kriteria, tipe_kriteria) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, criteria.getNamaKriteria());
            ps.setString(2, criteria.getTipeKriteria());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    criteria.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Criteria criteria) throws SQLException {
        String sql = "UPDATE criteria SET nama_kriteria = ?, tipe_kriteria = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, criteria.getNamaKriteria());
            ps.setString(2, criteria.getTipeKriteria());
            ps.setInt(3, criteria.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM criteria WHERE id = ?";
        try (PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement stmt = DatabaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM criteria")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<String, Integer> countByType() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT tipe_kriteria, COUNT(*) AS total FROM criteria GROUP BY tipe_kriteria";
        try (Statement stmt = DatabaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getString("tipe_kriteria"), rs.getInt("total"));
            }
        }
        return counts;
    }

    private Criteria mapRow(ResultSet rs) throws SQLException {
        return new Criteria(
                rs.getInt("id"),
                rs.getString("nama_kriteria"),
                rs.getString("tipe_kriteria")
        );
    }
}
