package Panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Factory.HeroFactory;
import Hero.Hero;
import Singleton.DatabaseManager;


// refactor 3
// load game screen
public class LoadGamePanel extends JPanel {
    private final String[] currentUser;
    private final List<Hero> loadedParty = new ArrayList<>();
    private final int[] savedMeta = new int[2];

    private final JLabel valParty = new JLabel("-");
    private final JLabel valGold = new JLabel("-");
    private final JLabel valRoom = new JLabel("-");
    private final JLabel noSaveLbl = new JLabel("");
    private final JButton loadBtn = new JButton("Load & Play");

    public LoadGamePanel(JPanel container, CardLayout cl, String[] currentUser, GamePanel gamePanel) {
        this.currentUser = currentUser;

        setupLayout();
        setupActions(container, cl, gamePanel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshSaveData();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Load Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Saved Party"));
        statsPanel.add(new JLabel("Party:"));  statsPanel.add(valParty);
        statsPanel.add(new JLabel("Gold:"));   statsPanel.add(valGold);
        statsPanel.add(new JLabel("Room:"));   statsPanel.add(valRoom);

        JPanel centerWrapper = new JPanel(new FlowLayout());
        centerWrapper.add(statsPanel);
        add(centerWrapper, BorderLayout.CENTER);

        noSaveLbl.setForeground(Color.RED);
        noSaveLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 10, 40));
        bottomPanel.add(noSaveLbl);
        bottomPanel.add(loadBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> {
            Container parent = getParent();
            if (parent instanceof JPanel && parent.getLayout() instanceof CardLayout layout) {
                layout.show(parent, "Menu");
            }
        });

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupActions(JPanel container, CardLayout cl, GamePanel gamePanel) {
        loadBtn.addActionListener(e -> {
            if (!loadedParty.isEmpty()) {
                gamePanel.loadGame(loadedParty, savedMeta[0], savedMeta[1]);
                cl.show(container, "Game");
            }
        });
    }

    private void refreshSaveData() {
        loadedParty.clear();
        loadBtn.setEnabled(false);
        noSaveLbl.setText("");
        valParty.setText("-");

        try {
            loadHeroData();
            loadMetadata();
        } catch (SQLException ex) {
            ex.printStackTrace();
            noSaveLbl.setText("Database error occurred.");
        }
    }

    private void loadHeroData() throws SQLException {
        ResultSet rs = DatabaseManager.getInstance().save.loadParty(currentUser[0]);
        if (rs == null) {
            noSaveLbl.setText("No save file found.");
            return;
        }

        StringBuilder summary = new StringBuilder("<html>");
        while (rs.next()) {
            Hero hero = mapResultSetToHero(rs);
            loadedParty.add(hero);
            summary.append(hero.getName()).append(" [").append(rs.getString("hero_class"))
                    .append(" Lv").append(hero.getLevel()).append("]<br>");
        }
        summary.append("</html>");

        if (!loadedParty.isEmpty()) {
            valParty.setText(summary.toString());
            loadBtn.setEnabled(true);
        } else {
            noSaveLbl.setText("No save file found.");
        }
    }

    private void loadMetadata() throws SQLException {
        ResultSet rs = DatabaseManager.getInstance().save.loadGame(currentUser[0]);
        if (rs != null && rs.next()) {
            savedMeta[0] = rs.getInt("gold");
            String roomStr = rs.getString("room").replaceAll("[^0-9]", "");
            savedMeta[1] = roomStr.isEmpty() ? 0 : Integer.parseInt(roomStr);

            valGold.setText(String.valueOf(savedMeta[0]));
            valRoom.setText(String.valueOf(savedMeta[1]));
        }
    }

    // Refactor 10 - Inappropriate Intimacy
    // Isolated HeroFactory call into a helper method to reduce coupling
    private Hero createHeroFromClass(String className, String name) {
        return HeroFactory.getFactory(className).createHero(name);
    }

    private Hero mapResultSetToHero(ResultSet rs) throws SQLException {
        String name = rs.getString("hero_name");
        String className = rs.getString("hero_class");

        Hero hero = createHeroFromClass(className, name);
        hero.setLevel(rs.getInt("level"));
        hero.setMaxHp(rs.getDouble("max_hp"));
        hero.changeHp(rs.getDouble("hp"));
        hero.changeAttack(rs.getInt("attack"));
        hero.changeDefense(rs.getInt("defense"));
        hero.setMaxMana(rs.getInt("max_mana"));
        hero.changeMana(rs.getInt("mana"));
        hero.setExperience(rs.getInt("experience"));
        hero.setPrimaryClassLevel(rs.getInt("primary_class_level"));
        hero.setSecondaryClassLevel(rs.getInt("secondary_class_level"));

        String secName = rs.getString("secondary_class_name");
        String hybrid = rs.getString("hybrid_class");
        if (secName != null) hero.setSecondaryClassName(secName);
        if (hybrid != null) hero.setHybridClass(hybrid);

        return hero;
    }
}