package Singleton;
import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/localDB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass";

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

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
                if (check.executeQuery().next()) return false;
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

    public boolean saveGame(String username) {
        String query = "INSERT INTO saves (username, level, hp, power, defense, speed, gold, room) " +
                "VALUES (?, 1, 100.0, 5, 5, 50, 0, '0') " +
                "ON DUPLICATE KEY UPDATE level=1, hp=100.0, power=5, defense=5, speed=50, gold=0, room='0'";
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


    public boolean saveParty(String username, java.util.List<Hero.Hero> party, int gold, int room) {
        String deleteQuery = "DELETE FROM party_saves WHERE username = ?";
        String insertQuery = "INSERT INTO party_saves (username, hero_index, hero_name, hero_class, level, hp, attack, defense, mana, experience) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSaveQuery = "UPDATE saves SET gold=?, room=? WHERE username=?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteQuery)) {
                del.setString(1, username);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                for (int i = 0; i < party.size(); i++) {
                    Hero.Hero h = party.get(i);
                    ins.setString(1, username);
                    ins.setInt(2, i);
                    ins.setString(3, h.getName());
                    ins.setString(4, h.getClass().getSimpleName());
                    ins.setInt(5, h.getLevel());
                    ins.setDouble(6, h.getHp());
                    ins.setInt(7, h.getAttack());
                    ins.setInt(8, h.getDefense());
                    ins.setInt(9, h.getMana());
                    ins.setInt(10, h.getExperience());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            try (PreparedStatement upd = conn.prepareStatement(updateSaveQuery)) {
                upd.setInt(1, gold);
                upd.setString(2, String.valueOf(room));
                upd.setString(3, username);
                upd.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public ResultSet loadParty(String username) {
        String query = "SELECT hero_index, hero_name, hero_class, level, hp, attack, defense, mana, experience " +
                "FROM party_saves WHERE username = ? ORDER BY hero_index ASC";
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

    public boolean updateSave(String username, int level, double hp, int attack,
                              int defense, int mana, int gold, int room) {
        String query = "UPDATE saves SET level=?, hp=?, power=?, defense=?, speed=?, gold=?, room=? WHERE username=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, level);
            stmt.setDouble(2, hp);
            stmt.setInt(3, attack);
            stmt.setInt(4, defense);
            stmt.setInt(5, mana);
            stmt.setInt(6, gold);
            stmt.setString(7, String.valueOf(room));
            stmt.setString(8, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean saveScore(String username, int score) {
        String query = "INSERT INTO hall_of_fame (username, score) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE score = GREATEST(score, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, score);
            stmt.setInt(3, score);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}