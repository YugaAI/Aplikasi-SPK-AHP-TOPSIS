package com.spk.repository;

import com.spk.domain.Vendor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Vendor CRUD operations.
 */
public class VendorRepository {

    public List<Vendor> findAll() throws SQLException {
        List<Vendor> list = new ArrayList<>();
        String sql = "SELECT * FROM vendors ORDER BY id";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close();
        stmt.close();
        return list;
    }

    public Vendor findById(int id) throws SQLException {
        String sql = "SELECT * FROM vendors WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Vendor v = null;
        if (rs.next()) {
            v = mapRow(rs);
        }
        rs.close();
        ps.close();
        return v;
    }

    public void insert(Vendor vendor) throws SQLException {
        String sql = "INSERT INTO vendors (nama_vendor, deskripsi) VALUES (?, ?)";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, vendor.getNamaVendor());
        ps.setString(2, vendor.getDeskripsi());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            vendor.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void update(Vendor vendor) throws SQLException {
        String sql = "UPDATE vendors SET nama_vendor = ?, deskripsi = ? WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setString(1, vendor.getNamaVendor());
        ps.setString(2, vendor.getDeskripsi());
        ps.setInt(3, vendor.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vendors WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public int count() throws SQLException {
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM vendors");
        int count = 0;
        if (rs.next()) count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    private Vendor mapRow(ResultSet rs) throws SQLException {
        return new Vendor(
                rs.getInt("id"),
                rs.getString("nama_vendor"),
                rs.getString("deskripsi")
        );
    }
}
