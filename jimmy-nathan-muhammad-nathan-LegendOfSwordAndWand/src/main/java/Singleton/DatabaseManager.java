package Singleton;

import java.sql.*;
import Hero.Hero;

public class DatabaseManager {
    private static DatabaseManager instance;

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/localDB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass";

    public final AuthRepository auth;
    public final SaveRepository save;
    public final PvPRepository pvp;

    private DatabaseManager() {
        this.auth = new AuthRepository(this);
        this.save = new SaveRepository(this);
        this.pvp = new PvPRepository(this);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Shared internal utility for re-instantiating Hero stats
    protected void applyHeroStats(Hero h, ResultSet rs) throws SQLException {
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