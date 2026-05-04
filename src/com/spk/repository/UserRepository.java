package com.spk.repository;

import com.spk.domain.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for User CRUD operations.
 */
public class UserRepository {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = mapRow(rs);
        }
        rs.close();
        ps.close();
        return user;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = mapRow(rs);
        }
        rs.close();
        ps.close();
        return user;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        Statement stmt = DatabaseHelper.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            users.add(mapRow(rs));
        }
        rs.close();
        stmt.close();
        return users;
    }

    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getPassword());
        ps.setString(3, user.getFullName());
        ps.setString(4, user.getRole());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            user.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, full_name = ?, role = ? WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getFullName());
        ps.setString(3, user.getRole());
        ps.setInt(4, user.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setString(1, hashedPassword);
        ps.setInt(2, userId);
        ps.executeUpdate();
        ps.close();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        PreparedStatement ps = DatabaseHelper.getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("role")
        );
    }
}
