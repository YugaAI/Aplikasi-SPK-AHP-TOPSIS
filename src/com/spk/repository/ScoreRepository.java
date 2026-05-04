package com.spk.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.spk.domain.Score;

/**
 * Repository for Score (vendor-criteria value) operations.
 */
public class ScoreRepository {

    public List<Score> findAll() throws SQLException {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.*, v.nama_vendor, c.nama_kriteria " +
                "FROM scores s " +
                "JOIN vendors v ON s.vendor_id = v.id " +
                "JOIN criteria c ON s.kriteria_id = c.id " +
                "ORDER BY s.vendor_id, s.kriteria_id";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Score score = new Score(rs.getInt("vendor_id"), rs.getInt("kriteria_id"), rs.getDouble("nilai"));
            score.setId(rs.getInt("id"));
            score.setVendorName(rs.getString("nama_vendor"));
            score.setKriteriaName(rs.getString("nama_kriteria"));
            list.add(score);
        }
        rs.close();
        stmt.close();
        return list;
    }

    public List<Score> findByVendor(int vendorId) throws SQLException {
        List<Score> list = new ArrayList<>();
        String sql = "SELECT s.*, c.nama_kriteria FROM scores s " +
                "JOIN criteria c ON s.kriteria_id = c.id " +
                "WHERE s.vendor_id = ? ORDER BY s.kriteria_id";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, vendorId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Score score = new Score(rs.getInt("vendor_id"), rs.getInt("kriteria_id"), rs.getDouble("nilai"));
            score.setId(rs.getInt("id"));
            score.setKriteriaName(rs.getString("nama_kriteria"));
            list.add(score);
        }
        rs.close();
        ps.close();
        return list;
    }

    /**
     * Insert or update a score (upsert).
     */
    public void upsert(Score score) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scores (vendor_id, kriteria_id, nilai) VALUES (?, ?, ?)";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, score.getVendorId());
        ps.setInt(2, score.getKriteriaId());
        ps.setDouble(3, score.getNilai());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Batch upsert all scores for a vendor.
     */
    public void upsertAll(List<Score> scores) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scores (vendor_id, kriteria_id, nilai) VALUES (?, ?, ?)";
        Connection conn = DatabaseHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (Score s : scores) {
                ps.setInt(1, s.getVendorId());
                ps.setInt(2, s.getKriteriaId());
                ps.setDouble(3, s.getNilai());
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

    public void deleteByVendor(int vendorId) throws SQLException {
        String sql = "DELETE FROM scores WHERE vendor_id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, vendorId);
        ps.executeUpdate();
        ps.close();
    }

    public void deleteByCriteria(int kriteriaId) throws SQLException {
        String sql = "DELETE FROM scores WHERE kriteria_id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, kriteriaId);
        ps.executeUpdate();
        ps.close();
    }

    public int countScores() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM scores");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    /**
     * Check if all vendors have scores for all criteria.
     */
    public boolean isComplete(int vendorCount, int criteriaCount) throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM scores");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count == vendorCount * criteriaCount;
    }

    /**
     * Get the score matrix as a 2D array. Rows = vendors, Cols = criteria.
     * vendorIds and criteriaIds must be sorted.
     */
    public double[][] getScoreMatrix(List<Integer> vendorIds, List<Integer> criteriaIds) throws SQLException {
        double[][] matrix = new double[vendorIds.size()][criteriaIds.size()];
        String sql = "SELECT vendor_id, kriteria_id, nilai FROM scores ORDER BY vendor_id, kriteria_id";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            int vi = vendorIds.indexOf(rs.getInt("vendor_id"));
            int ci = criteriaIds.indexOf(rs.getInt("kriteria_id"));
            if (vi >= 0 && ci >= 0) {
                matrix[vi][ci] = rs.getDouble("nilai");
            }
        }
        rs.close();
        stmt.close();
        return matrix;
    }
}
