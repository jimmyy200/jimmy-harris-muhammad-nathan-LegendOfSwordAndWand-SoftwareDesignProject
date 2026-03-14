package Singleton;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/localDB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass";

    // Private constructor - no one can instantiate this directly
    private DatabaseManager() {}

    // The one and only way to get the instance
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ── Connection ────────────────────────────────────────────
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ── Auth ──────────────────────────────────────────────────
    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeQuery().next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        String checkQuery  = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            try (PreparedStatement check = conn.prepareStatement(checkQuery)) {
                check.setString(1, username);
                if (check.executeQuery().next()) return false; // username taken
            }
            try (PreparedStatement insert = conn.prepareStatement(insertQuery)) {
                insert.setString(1, username);
                insert.setString(2, password);
                insert.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ── Class ─────────────────────────────────────────────────
    public boolean saveClass(String username, String className) {
        String query = "UPDATE users SET class = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, className);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ── Save / Load ───────────────────────────────────────────
    public boolean saveGame(String username) {
        String query = "INSERT INTO saves (username, level, hp, power, defense, speed, gold, room) " +
                "VALUES (?, 1, 100.0, 10.0, 10.0, 10.0, 0, 'Start') " +
                "ON DUPLICATE KEY UPDATE level=1, hp=100.0, power=10.0, defense=10.0, speed=10.0, gold=0, room='Start'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public ResultSet loadGame(String username) {
        String query = "SELECT s.level, s.hp, s.power, s.defense, s.speed, s.gold, s.room, u.class " +
                "FROM saves s JOIN users u ON s.username = u.username WHERE s.username = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            return stmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

