package Singleton;

import java.sql.*;
import java.util.*;
import Hero.Hero;
import Factory.HeroFactory;

public class PvPRepository {
    private final DatabaseManager db;

    public PvPRepository(DatabaseManager db) {
        this.db = db;
    }

    public int countPvPParties(String username) {
        String query = "SELECT COUNT(DISTINCT slot_id) FROM pvp_parties WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean savePvPParty(String username, int slotId, List<Hero> party) {
        String deleteQuery = "DELETE FROM pvp_parties WHERE username = ? AND slot_id = ?";
        String insertQuery = "INSERT INTO pvp_parties (username, slot_id, hero_index, hero_name, hero_class, " +
                "level, hp, max_hp, attack, defense, mana, max_mana, experience, " +
                "primary_class_level, secondary_class_level, secondary_class_name, hybrid_class) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteQuery)) {
                del.setString(1, username);
                del.setInt(2, slotId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertQuery)) {
                for (int i = 0; i < party.size(); i++) {
                    Hero h = party.get(i);
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

    public List<Hero> loadPvPParty(String username, int slotId) {
        List<Hero> party = new ArrayList<>();
        String query = "SELECT hero_name, hero_class, level, hp, max_hp, attack, defense, mana, max_mana, " +
                "experience, primary_class_level, secondary_class_level, secondary_class_name, hybrid_class " +
                "FROM pvp_parties WHERE username = ? AND slot_id = ? ORDER BY hero_index ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, slotId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Hero h = HeroFactory.getFactory(rs.getString("hero_class")).createHero(rs.getString("hero_name"));
                db.applyHeroStats(h, rs);
                party.add(h);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return party;
    }

    public List<String> getPvPPartySlotSummaries(String username) {
        List<String> summaries = new ArrayList<>();
        String query = "SELECT slot_id, hero_name, hero_class, level FROM pvp_parties " +
                "WHERE username = ? ORDER BY slot_id ASC, hero_index ASC";
        try (Connection conn = db.getConnection();
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

    public void recordPvPResult(String winner, String loser) {
        String upsert = "INSERT INTO pvp_league (username, wins, losses) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE wins = wins + ?, losses = losses + ?";
        try (Connection conn = db.getConnection()) {
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
            Connection conn = db.getConnection();
            return conn.prepareStatement(query).executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean deletePvPParty(String username, int slotId) {
        String query = "DELETE FROM pvp_parties WHERE username = ? AND slot_id = ?";
        try (Connection conn = db.getConnection();
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

    public boolean hasSavedParty(String username) {
        String query = "SELECT COUNT(*) FROM pvp_parties WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}