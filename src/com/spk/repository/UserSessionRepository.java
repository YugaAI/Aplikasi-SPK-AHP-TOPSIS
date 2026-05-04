package com.spk.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for storing user session/device login data.
 */
public class UserSessionRepository {

    public void upsertSession(int userId, String macAddress, String deviceType) throws SQLException {
        Connection conn = DatabaseHelper.getConnection();
        String sql = "INSERT INTO user_sessions (user_id, mac_address, device_type, last_login) " +
                "VALUES (?, ?, ?, datetime('now')) " +
                "ON CONFLICT(user_id, mac_address) DO UPDATE SET device_type = excluded.device_type, last_login = datetime('now')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, macAddress);
            ps.setString(3, deviceType);
            ps.executeUpdate();
        }
    }

    public Map<String, Integer> countByDeviceType() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT device_type, COUNT(*) AS total FROM user_sessions GROUP BY device_type";
        try (Statement stmt = DatabaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getString("device_type"), rs.getInt("total"));
            }
        }
        return counts;
    }
}
