package Panels;

import Factory.HeroFactory;
import Hero.*;
import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// load game screen
public class LoadGamePanel extends JPanel {
    public LoadGamePanel(JPanel container, CardLayout cl, String[] currentUser, GamePanel gamePanel) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Load Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Saved Party"));

        JLabel valParty = new JLabel("-");
        JLabel valGold  = new JLabel("-");
        JLabel valRoom  = new JLabel("-");

        statsPanel.add(new JLabel("Party:"));  statsPanel.add(valParty);
        statsPanel.add(new JLabel("Gold:"));   statsPanel.add(valGold);
        statsPanel.add(new JLabel("Room:"));   statsPanel.add(valRoom);

        JPanel centerWrapper = new JPanel(new FlowLayout());
        centerWrapper.add(statsPanel);
        add(centerWrapper, BorderLayout.CENTER);

        JButton loadBtn   = new JButton("Load & Play");
        JButton backBtn   = new JButton("Back to Menu");
        JLabel  noSaveLbl = new JLabel("", SwingConstants.CENTER);
        noSaveLbl.setForeground(Color.RED);
        loadBtn.setEnabled(false);

        final List<Hero> loadedParty = new ArrayList<>();
        final int[] savedMeta = new int[2]; // [gold, room]

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 10, 40));
        bottomPanel.add(noSaveLbl);
        bottomPanel.add(loadBtn);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // update data when shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadedParty.clear();
                loadBtn.setEnabled(false);
                noSaveLbl.setText("");

                // get party stats
                ResultSet rs = DatabaseManager.getInstance().save.loadParty(currentUser[0]);
                try {
                    if (rs != null) {
                        StringBuilder partySummary = new StringBuilder("<html>");
                        while (rs.next()) {
                            String heroName  = rs.getString("hero_name");
                            String heroClass = rs.getString("hero_class");
                            int    level     = rs.getInt("level");
                            double hp        = rs.getDouble("hp");
                            double maxHp     = rs.getDouble("max_hp");
                            int    attack    = rs.getInt("attack");
                            int    defense   = rs.getInt("defense");
                            int    mana      = rs.getInt("mana");
                            int    maxMana   = rs.getInt("max_mana");
                            int    exp       = rs.getInt("experience");
                            int    pclLvl    = rs.getInt("primary_class_level");
                            int    sclLvl    = rs.getInt("secondary_class_level");
                            String secName   = rs.getString("secondary_class_name");
                            String hybrid    = rs.getString("hybrid_class");

                            Hero hero = HeroFactory.getFactory(heroClass).createHero(heroName);
                            hero.setLevel(level);
                            hero.setMaxHp(maxHp);
                            hero.changeHp(hp);
                            hero.changeAttack(attack);
                            hero.changeDefense(defense);
                            hero.setMaxMana(maxMana);
                            hero.changeMana(mana);
                            hero.setExperience(exp);
                            hero.setPrimaryClassLevel(pclLvl);
                            hero.setSecondaryClassLevel(sclLvl);
                            if (secName != null) hero.setSecondaryClassName(secName);
                            if (hybrid != null) hero.setHybridClass(hybrid);
                            loadedParty.add(hero);

                            partySummary.append(heroName).append(" [").append(heroClass)
                                    .append(" Lv").append(level).append("]<br>");
                        }
                        partySummary.append("</html>");

                        if (!loadedParty.isEmpty()) {
                            valParty.setText(partySummary.toString());
                            loadBtn.setEnabled(true);
                        } else {
                            noSaveLbl.setText("No save file found.");
                        }
                    } else {
                        noSaveLbl.setText("No save file found.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // load gold and room
                ResultSet savesRs = DatabaseManager.getInstance().save.loadGame(currentUser[0]);
                try {
                    if (savesRs != null && savesRs.next()) {
                        savedMeta[0] = savesRs.getInt("gold");
                        savedMeta[1] = Integer.parseInt(savesRs.getString("room").replaceAll("[^0-9]", "0"));
                        valGold.setText(String.valueOf(savedMeta[0]));
                        valRoom.setText(String.valueOf(savedMeta[1]));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        loadBtn.addActionListener(e -> {
            if (loadedParty.isEmpty()) return;
            gamePanel.loadGame(loadedParty, savedMeta[0], savedMeta[1]);
            cl.show(container, "Game");
        });

        backBtn.addActionListener(e -> cl.show(container, "Menu"));
    }
}