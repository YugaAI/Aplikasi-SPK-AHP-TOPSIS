package com.spk.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Database helper class for managing SQLite connections and schema initialization.
 * Implements singleton pattern for the connection.
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:spk_ahp_topsis.db";
    private static Connection connection;

    /**
     * Get the shared database connection. Creates it if it doesn't exist.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign keys
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /**
     * Initialize the database schema. Creates all tables if they don't exist.
     */
    public static void initializeDatabase() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT, " +
                "role TEXT NOT NULL DEFAULT 'user'" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS criteria (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_kriteria TEXT NOT NULL, " +
                "tipe_kriteria TEXT NOT NULL CHECK(tipe_kriteria IN ('benefit', 'cost'))" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS vendors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_vendor TEXT NOT NULL, " +
                "alamat TEXT" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS scores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vendor_id INTEGER NOT NULL, " +
                "kriteria_id INTEGER NOT NULL, " +
                "nilai REAL NOT NULL, " +
                "FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (kriteria_id) REFERENCES criteria(id) ON DELETE CASCADE, " +
                "UNIQUE(vendor_id, kriteria_id)" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS pairwise_comparisons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kriteria_id_1 INTEGER NOT NULL, " +
                "kriteria_id_2 INTEGER NOT NULL, " +
                "nilai REAL NOT NULL, " +
                "FOREIGN KEY (kriteria_id_1) REFERENCES criteria(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (kriteria_id_2) REFERENCES criteria(id) ON DELETE CASCADE, " +
                "UNIQUE(kriteria_id_1, kriteria_id_2)" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS ahp_weights (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kriteria_id INTEGER NOT NULL UNIQUE, " +
                "bobot REAL NOT NULL, " +
                "consistency_ratio REAL, " +
                "calculated_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (kriteria_id) REFERENCES criteria(id) ON DELETE CASCADE" +
                ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS topsis_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vendor_id INTEGER NOT NULL UNIQUE, " +
                "skor_preferensi REAL NOT NULL, " +
                "ranking INTEGER NOT NULL, " +
                "calculated_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE" +
                ")");

        // Seed default admin user if no users exist
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next() && rs.getInt(1) == 0) {
            String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)");
            ps.setString(1, "admin");
            ps.setString(2, hashedPassword);
            ps.setString(3, "Administrator");
            ps.setString(4, "admin");
            ps.executeUpdate();
            ps.close();
        }
        rs.close();
        stmt.close();
    }

    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
