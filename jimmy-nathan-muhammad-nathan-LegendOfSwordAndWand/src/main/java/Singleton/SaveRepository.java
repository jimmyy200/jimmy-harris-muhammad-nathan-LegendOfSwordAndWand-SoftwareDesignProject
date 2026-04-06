package Singleton;

import java.sql.*;
import java.util.*;
import Hero.Hero;
import Factory.HeroFactory;

public class SaveRepository {
    private final DatabaseManager db;

    public SaveRepository(DatabaseManager db) {
        this.db = db;
    }

    public boolean saveGame(String username) {
        String query = "INSERT INTO saves (username, level, hp, power, defense, speed, gold, room) " +
                "VALUES (?, 1, 100.0, 5, 5, 50, 0, '0') " +
                "ON DUPLICATE KEY UPDATE level=1, hp=100.0, power=5, defense=5, speed=50, gold=0, room='0'";
        try (Connection conn = db.getConnection();
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
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            return stmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void bindHeroToStatement(PreparedStatement ins, Hero h, int startIndex) throws SQLException {
        ins.setString(startIndex, h.getName());
        ins.setString(startIndex + 1, h.getClass().getSimpleName());
        ins.setInt(startIndex + 2, h.getLevel());
        ins.setDouble(startIndex + 3, h.getHp());
        ins.setDouble(startIndex + 4, h.getMaxHp());
        ins.setInt(startIndex + 5, h.getAttack());
        ins.setInt(startIndex + 6, h.getDefense());
        ins.setInt(startIndex + 7, h.getMana());
        ins.setInt(startIndex + 8, h.getMaxMana());
        ins.setInt(startIndex + 9, h.getExperience());
        ins.setInt(startIndex + 10, h.getPrimaryClassLevel());
        ins.setInt(startIndex + 11, h.getSecondaryClassLevel());
        ins.setString(startIndex + 12, h.getSecondaryClassName());
        ins.setString(startIndex + 13, h.getHybridClass());
    }

    public boolean saveParty(String username, List<Hero> party, int gold, int room) {
        String delete = "DELETE FROM party_saves WHERE username = ?";
        String insert = "INSERT INTO party_saves (username, hero_index, hero_name, hero_class, level, hp, max_hp, attack, defense, mana, max_mana, experience, primary_class_level, secondary_class_level, secondary_class_name, hybrid_class) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(delete)) {
                del.setString(1, username);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                for (int i = 0; i < party.size(); i++) {
                    ins.setString(1, username);
                    ins.setInt(2, i);
                    bindHeroToStatement(ins, party.get(i), 3); // Starts at index 3
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            try (PreparedStatement upd = conn.prepareStatement("UPDATE saves SET gold=?, room=? WHERE username=?")) {
                upd.setInt(1, gold);
                upd.setString(2, String.valueOf(room));
                upd.setString(3, username);
                upd.executeUpdate();
            }
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public ResultSet loadParty(String username) {
        String query = "SELECT hero_index, hero_name, hero_class, level, hp, max_hp, " +
                "attack, defense, mana, max_mana, experience, " +
                "primary_class_level, secondary_class_level, secondary_class_name, hybrid_class " +
                "FROM party_saves WHERE username = ? ORDER BY hero_index ASC";
        try {
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            return stmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Refactor 10 - Inappropriate Intimacy
    // Panels no longer parse ResultSets or call HeroFactory directly
    public List<Hero> loadPartyAsHeroes(String username) throws SQLException {
        List<Hero> heroes = new ArrayList<>();
        ResultSet rs = loadParty(username);
        if (rs == null) return heroes;
        while (rs.next()) {
            String name = rs.getString("hero_name");
            String className = rs.getString("hero_class");
            Hero hero = HeroFactory.getFactory(className).createHero(name);
            db.applyHeroStats(hero, rs);
            heroes.add(hero);
        }
        return heroes;
    }

    // Refactor 10 - Inappropriate Intimacy
    // Returns [gold, room] so panel does not need ResultSet access
    public int[] loadGameMeta(String username) throws SQLException {
        ResultSet rs = loadGame(username);
        if (rs != null && rs.next()) {
            int gold = rs.getInt("gold");
            String roomStr = rs.getString("room").replaceAll("[^0-9]", "");
            int room = roomStr.isEmpty() ? 0 : Integer.parseInt(roomStr);
            return new int[]{gold, room};
        }
        return null;
    }

    // Refactor 10 - Inappropriate Intimacy
    // Combines class save and game save into one call for ClassSelectPanel
    public void initNewGame(String username, String heroClass) {
        db.auth.saveClass(username, heroClass);
        saveGame(username);
    }

    // Refactor 10 - Inappropriate Intimacy
    // Builds a party of heroes from class names so panels don't call HeroFactory
    public List<Hero> buildParty(List<String> classNames, String username) {
        List<Hero> party = new ArrayList<>();
        for (int i = 0; i < classNames.size(); i++) {
            String heroName = username + (i == 0 ? "" : "-" + (i + 1));
            party.add(HeroFactory.getFactory(classNames.get(i)).createHero(heroName));
        }
        return party;
    }

    public boolean saveInventory(String username, Map<String, Integer> inventory) {
        String deleteQuery = "DELETE FROM inventory WHERE username = ?";
        String insertQuery = "INSERT INTO inventory (username, item_name, quantity) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteQuery)) {
                del.setString(1, username);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
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

    public Map<String, Integer> loadInventory(String username) {
        Map<String, Integer> inv = new LinkedHashMap<>();
        String query = "SELECT item_name, quantity FROM inventory WHERE username = ?";
        try (Connection conn = db.getConnection();
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

    public boolean saveScore(String username, int score) {
        String query = "INSERT INTO hall_of_fame (username, score) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE score = GREATEST(score, ?)";
        try (Connection conn = db.getConnection();
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
            Connection conn = db.getConnection();
            return conn.prepareStatement(query).executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
