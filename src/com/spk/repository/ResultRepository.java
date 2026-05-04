package com.spk.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.spk.domain.TOPSISResult;

/**
 * Repository for storing/retrieving AHP weights and TOPSIS results.
 */
public class ResultRepository {

    // ==================== AHP Weights ====================

    /**
     * Save AHP weights (replaces all existing weights).
     */
    public void saveAHPWeights(Map<Integer, Double> weights, double consistencyRatio) throws SQLException {
        Connection conn = DatabaseHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            // Clear existing weights
            conn.createStatement().execute("DELETE FROM ahp_weights");

            String sql = "INSERT INTO ahp_weights (kriteria_id, bobot, consistency_ratio) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
                ps.setInt(1, entry.getKey());
                ps.setDouble(2, entry.getValue());
                ps.setDouble(3, consistencyRatio);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Get saved AHP weight for a specific criteria.
     */
    public double getWeight(int kriteriaId) throws SQLException {
        String sql = "SELECT bobot FROM ahp_weights WHERE kriteria_id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, kriteriaId);
        ResultSet rs = ps.executeQuery();
        double weight = 0;
        if (rs.next()) {
            weight = rs.getDouble("bobot");
        }
        rs.close();
        ps.close();
        return weight;
    }

    public Map<String, Double> getWeightsByCriteria() throws SQLException {
        Map<String, Double> weights = new LinkedHashMap<>();
        String sql = "SELECT c.nama_kriteria, w.bobot FROM ahp_weights w " +
                "JOIN criteria c ON w.kriteria_id = c.id ORDER BY c.id";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            weights.put(rs.getString("nama_kriteria"), rs.getDouble("bobot"));
        }
        rs.close();
        stmt.close();
        return weights;
    }

    /**
     * Check if AHP weights have been calculated.
     */
    public boolean hasWeights() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ahp_weights");
        boolean has = false;
        if (rs.next()) has = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return has;
    }

    /**
     * Get the stored consistency ratio.
     */
    public double getConsistencyRatio() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT consistency_ratio FROM ahp_weights LIMIT 1");
        double cr = 0;
        if (rs.next()) cr = rs.getDouble("consistency_ratio");
        rs.close();
        stmt.close();
        return cr;
    }

    public int countAHPWeights() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ahp_weights");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public int countTOPSISResults() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM topsis_results");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    // ==================== TOPSIS Results ====================

    /**
     * Save TOPSIS results (replaces all existing results).
     */
    public void saveTOPSISResults(List<TOPSISResult> results) throws SQLException {
        Connection conn = DatabaseHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            conn.createStatement().execute("DELETE FROM topsis_results");

            String sql = "INSERT INTO topsis_results (vendor_id, skor_preferensi, ranking) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (TOPSISResult r : results) {
                ps.setInt(1, r.getVendorId());
                ps.setDouble(2, r.getSkorPreferensi());
                ps.setInt(3, r.getRanking());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Get all TOPSIS results ordered by ranking.
     */
    public List<TOPSISResult> getTOPSISResults() throws SQLException {
        List<TOPSISResult> list = new ArrayList<>();
        String sql = "SELECT t.*, v.nama_vendor FROM topsis_results t " +
                "JOIN vendors v ON t.vendor_id = v.id ORDER BY t.ranking";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            TOPSISResult r = new TOPSISResult(
                    rs.getInt("vendor_id"),
                    rs.getString("nama_vendor"),
                    rs.getDouble("skor_preferensi"),
                    rs.getInt("ranking")
            );
            list.add(r);
        }
        rs.close();
        stmt.close();
        return list;
    }

    public boolean hasResults() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM topsis_results");
        boolean has = false;
        if (rs.next()) has = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return has;
    }
}
