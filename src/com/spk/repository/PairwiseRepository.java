package com.spk.repository;

import com.spk.domain.PairwiseComparison;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for AHP pairwise comparison operations.
 */
public class PairwiseRepository {

    public List<PairwiseComparison> findAll() throws SQLException {
        List<PairwiseComparison> list = new ArrayList<>();
        String sql = "SELECT * FROM pairwise_comparisons ORDER BY kriteria_id_1, kriteria_id_2";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            PairwiseComparison pc = new PairwiseComparison(
                    rs.getInt("kriteria_id_1"),
                    rs.getInt("kriteria_id_2"),
                    rs.getDouble("nilai")
            );
            pc.setId(rs.getInt("id"));
            list.add(pc);
        }
        rs.close();
        stmt.close();
        return list;
    }

    /**
     * Insert or update a pairwise comparison value.
     */
    public void upsert(PairwiseComparison pc) throws SQLException {
        String sql = "INSERT OR REPLACE INTO pairwise_comparisons (kriteria_id_1, kriteria_id_2, nilai) VALUES (?, ?, ?)";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, pc.getKriteriaId1());
        ps.setInt(2, pc.getKriteriaId2());
        ps.setDouble(3, pc.getNilai());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Batch upsert all pairwise comparisons.
     */
    public void upsertAll(List<PairwiseComparison> comparisons) throws SQLException {
        String sql = "INSERT OR REPLACE INTO pairwise_comparisons (kriteria_id_1, kriteria_id_2, nilai) VALUES (?, ?, ?)";
        Connection conn = DatabaseHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (PairwiseComparison pc : comparisons) {
                ps.setInt(1, pc.getKriteriaId1());
                ps.setInt(2, pc.getKriteriaId2());
                ps.setDouble(3, pc.getNilai());
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

    public void deleteAll() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        stmt.execute("DELETE FROM pairwise_comparisons");
        stmt.close();
    }

    public int count() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pairwise_comparisons");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }
}
