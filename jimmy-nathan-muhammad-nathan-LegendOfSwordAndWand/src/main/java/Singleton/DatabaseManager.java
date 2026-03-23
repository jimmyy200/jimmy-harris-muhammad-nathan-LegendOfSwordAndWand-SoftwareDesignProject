package Singleton;

import java.sql.*;

// handles the database
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

    // check login info

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

    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // update character class in db

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

    // save and load game progress

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

    // save and load party stats

    public boolean saveParty(String username, java.util.List<Hero.Hero> party, int gold, int room) {
        String deleteQuery = "DELETE FROM party_saves WHERE username = ?";
        String insertQuery = "INSERT INTO party_saves (username, hero_index, hero_name, hero_class, " +
                "level, hp, max_hp, attack, defense, mana, max_mana, experience, " +
                "primary_class_level, secondary_class_level, secondary_class_name, hybrid_class) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                    ins.setDouble(7, h.getMaxHp());
                    ins.setInt(8, h.getAttack());
                    ins.setInt(9, h.getDefense());
                    ins.setInt(10, h.getMana());
                    ins.setInt(11, h.getMaxMana());
                    ins.setInt(12, h.getExperience());
                    ins.setInt(13, h.getPrimaryClassLevel());
                    ins.setInt(14, h.getSecondaryClassLevel());
                    ins.setString(15, h.getSecondaryClassName());
                    ins.setString(16, h.getHybridClass());
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
        String query = "SELECT hero_index, hero_name, hero_class, level, hp, max_hp, " +
                "attack, defense, mana, max_mana, experience, " +
                "primary_class_level, secondary_class_level, secondary_class_name, hybrid_class " +
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

    // inventory stuff

    public boolean saveInventory(String username, java.util.Map<String, Integer> inventory) {
        String deleteQuery = "DELETE FROM inventory WHERE username = ?";
        String insertQuery = "INSERT INTO inventory (username, item_name, quantity) VALUES (?, ?, ?)";
        try (Connection conn = getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteQuery)) {
                del.setString(1, username);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                for (java.util.Map.Entry<String, Integer> entry : inventory.entrySet()) {
                    if (entry.getValue() > 0) {
                        ins.setString(1, username);
                        ins.setString(2, entry.getKey());
                        ins.setInt(3, entry.getValue());
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public java.util.Map<String, Integer> loadInventory(String username) {
        java.util.Map<String, Integer> inv = new java.util.LinkedHashMap<>();
        String query = "SELECT item_name, quantity FROM inventory WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                inv.put(rs.getString("item_name"), rs.getInt("quantity"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return inv;
    }

    // hall of fame leaderboard

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

    public ResultSet getHallOfFame() {
        String query = "SELECT username, score FROM hall_of_fame ORDER BY score DESC";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(query).executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // pvp party slots

    public int countPvPParties(String username) {
        String query = "SELECT COUNT(DISTINCT slot_id) FROM pvp_parties WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean savePvPParty(String username, int slotId, java.util.List<Hero.Hero> party) {
        String deleteQuery = "DELETE FROM pvp_parties WHERE username = ? AND slot_id = ?";
        String insertQuery = "INSERT INTO pvp_parties (username, slot_id, hero_index, hero_name, hero_class, " +
                "level, hp, max_hp, attack, defense, mana, max_mana, experience, " +
                "primary_class_level, secondary_class_level, secondary_class_name, hybrid_class) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteQuery)) {
                del.setString(1, username);
                del.setInt(2, slotId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                for (int i = 0; i < party.size(); i++) {
                    Hero.Hero h = party.get(i);
                    ins.setString(1, username);
                    ins.setInt(2, slotId);
                    ins.setInt(3, i);
                    ins.setString(4, h.getName());
                    ins.setString(5, h.getClass().getSimpleName());
                    ins.setInt(6, h.getLevel());
                    ins.setDouble(7, h.getHp());
                    ins.setDouble(8, h.getMaxHp());
                    ins.setInt(9, h.getAttack());
                    ins.setInt(10, h.getDefense());
                    ins.setInt(11, h.getMana());
                    ins.setInt(12, h.getMaxMana());
                    ins.setInt(13, h.getExperience());
                    ins.setInt(14, h.getPrimaryClassLevel());
                    ins.setInt(15, h.getSecondaryClassLevel());
                    ins.setString(16, h.getSecondaryClassName());
                    ins.setString(17, h.getHybridClass());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public java.util.List<Hero.Hero> loadPvPParty(String username, int slotId) {
        java.util.List<Hero.Hero> party = new java.util.ArrayList<>();
        String query = "SELECT hero_name, hero_class, level, hp, max_hp, attack, defense, mana, max_mana, " +
                "experience, primary_class_level, secondary_class_level, secondary_class_name, hybrid_class " +
                "FROM pvp_parties WHERE username = ? AND slot_id = ? ORDER BY hero_index ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, slotId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Hero.Hero h = Factory.HeroFactory.getFactory(rs.getString("hero_class")).createHero(rs.getString("hero_name"));
                applyHeroStats(h, rs);
                party.add(h);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return party;
    }

    public java.util.List<String> getPvPPartySlotSummaries(String username) {
        java.util.List<String> summaries = new java.util.ArrayList<>();
        String query = "SELECT slot_id, hero_name, hero_class, level FROM pvp_parties " +
                "WHERE username = ? ORDER BY slot_id ASC, hero_index ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            int lastSlot = -1;
            StringBuilder sb = null;
            while (rs.next()) {
                int slot = rs.getInt("slot_id");
                if (slot != lastSlot) {
                    if (sb != null) summaries.add(sb.toString());
                    sb = new StringBuilder("Slot " + (slot + 1) + ": ");
                    lastSlot = slot;
                }
                sb.append(rs.getString("hero_name")).append("[")
                        .append(rs.getString("hero_class")).append(" Lv")
                        .append(rs.getInt("level")).append("] ");
            }
            if (sb != null) summaries.add(sb.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return summaries;
    }

    public boolean hasSavedParty(String username) {
        String query = "SELECT COUNT(*) FROM pvp_parties WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deletePvPParty(String username, int slotId) {
        String query = "DELETE FROM pvp_parties WHERE username = ? AND slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, slotId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // pvp rankings

    public void recordPvPResult(String winner, String loser) {
        String upsert = "INSERT INTO pvp_league (username, wins, losses) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE wins = wins + ?, losses = losses + ?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, winner); stmt.setInt(2, 1); stmt.setInt(3, 0);
                stmt.setInt(4, 1);        stmt.setInt(5, 0);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, loser); stmt.setInt(2, 0); stmt.setInt(3, 1);
                stmt.setInt(4, 0);       stmt.setInt(5, 1);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ResultSet getLeagueStandings() {
        String query = "SELECT username, wins, losses FROM pvp_league ORDER BY wins DESC, losses ASC";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(query).executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // helper methods

    private void applyHeroStats(Hero.Hero h, ResultSet rs) throws SQLException {
        h.setLevel(rs.getInt("level"));
        h.setMaxHp(rs.getDouble("max_hp"));
        h.changeHp(rs.getDouble("hp"));
        h.changeAttack(rs.getInt("attack"));
        h.changeDefense(rs.getInt("defense"));
        h.setMaxMana(rs.getInt("max_mana"));
        h.changeMana(rs.getInt("mana"));
        h.setExperience(rs.getInt("experience"));
        h.setPrimaryClassLevel(rs.getInt("primary_class_level"));
        h.setSecondaryClassLevel(rs.getInt("secondary_class_level"));
        String secName = rs.getString("secondary_class_name");
        if (secName != null) h.setSecondaryClassName(secName);
        String hybrid = rs.getString("hybrid_class");
        if (hybrid != null) h.setHybridClass(hybrid);
    }
}