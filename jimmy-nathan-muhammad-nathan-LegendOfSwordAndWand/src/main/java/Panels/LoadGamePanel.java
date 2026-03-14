package Panels;

import Singleton.DatabaseManager;
import Singleton.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoadGamePanel extends JPanel {
    public LoadGamePanel(JPanel container, CardLayout cl, String[] currentUser, GamePanel gamePanel) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Load Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Saved Game"));

        JLabel valClass = new JLabel("-"); JLabel valLevel = new JLabel("-");
        JLabel valHp    = new JLabel("-"); JLabel valAtk   = new JLabel("-");
        JLabel valDef   = new JLabel("-"); JLabel valMana  = new JLabel("-");
        JLabel valGold  = new JLabel("-"); JLabel valRoom  = new JLabel("-");

        statsPanel.add(new JLabel("Class:"));   statsPanel.add(valClass);
        statsPanel.add(new JLabel("Level:"));   statsPanel.add(valLevel);
        statsPanel.add(new JLabel("HP:"));      statsPanel.add(valHp);
        statsPanel.add(new JLabel("Attack:"));  statsPanel.add(valAtk);
        statsPanel.add(new JLabel("Defense:")); statsPanel.add(valDef);
        statsPanel.add(new JLabel("Mana:"));    statsPanel.add(valMana);
        statsPanel.add(new JLabel("Gold:"));    statsPanel.add(valGold);
        statsPanel.add(new JLabel("Room:"));    statsPanel.add(valRoom);

        JPanel centerWrapper = new JPanel(new FlowLayout());
        centerWrapper.add(statsPanel);
        add(centerWrapper, BorderLayout.CENTER);

        JButton loadBtn   = new JButton("Load & Play");
        JButton backBtn   = new JButton("Back to Menu");
        JLabel  noSaveLbl = new JLabel("", SwingConstants.CENTER);
        noSaveLbl.setForeground(Color.RED);
        loadBtn.setEnabled(false);

        // Hold save data for use when Load is clicked
        int[] savedData = new int[4]; // level, attack, defense, mana, gold, room
        double[] savedHp = new double[1];
        int[] savedGold  = new int[1];
        int[] savedRoom  = new int[1];
        String[] savedClass = new String[1];

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 10, 40));
        bottomPanel.add(noSaveLbl);
        bottomPanel.add(loadBtn);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                ResultSet rs = DatabaseManager.getInstance().loadGame(currentUser[0]);
                try {
                    if (rs != null && rs.next()) {
                        savedClass[0] = rs.getString("class") != null ? rs.getString("class") : "Warrior";
                        savedData[0]  = rs.getInt("level");
                        savedHp[0]    = rs.getDouble("hp");
                        savedData[1]  = rs.getInt("power");
                        savedData[2]  = rs.getInt("defense");
                        savedData[3]  = rs.getInt("speed"); // used as mana
                        savedGold[0]  = rs.getInt("gold");
                        savedRoom[0]  = Integer.parseInt(rs.getString("room").replaceAll("[^0-9]", "0"));

                        valClass.setText(savedClass[0]);
                        valLevel.setText(String.valueOf(savedData[0]));
                        valHp.setText(String.valueOf(savedHp[0]));
                        valAtk.setText(String.valueOf(savedData[1]));
                        valDef.setText(String.valueOf(savedData[2]));
                        valMana.setText(String.valueOf(savedData[3]));
                        valGold.setText(String.valueOf(savedGold[0]));
                        valRoom.setText(String.valueOf(savedRoom[0]));
                        noSaveLbl.setText("");
                        loadBtn.setEnabled(true);
                    } else {
                        noSaveLbl.setText("No save file found.");
                        loadBtn.setEnabled(false);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        loadBtn.addActionListener(e -> {
            GameEngine.getInstance().startGame();
            gamePanel.loadGame(savedClass[0], currentUser[0],
                    savedData[0], savedHp[0], savedData[1], savedData[2], savedData[3],
                    savedGold[0], savedRoom[0]);
            cl.show(container, "Game");
        });

        backBtn.addActionListener(e -> cl.show(container, "Menu"));
    }
}