package Panels;

import Singleton.DatabaseManager;
import Singleton.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoadGamePanel extends JPanel {
    public LoadGamePanel(JPanel container, CardLayout cl, String[] currentUser) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Load Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Saved Game"));

        JLabel valClass = new JLabel("-"); JLabel valLevel   = new JLabel("-");
        JLabel valHp    = new JLabel("-"); JLabel valPower   = new JLabel("-");
        JLabel valDef   = new JLabel("-"); JLabel valSpeed   = new JLabel("-");
        JLabel valGold  = new JLabel("-"); JLabel valRoom    = new JLabel("-");

        statsPanel.add(new JLabel("Class:"));   statsPanel.add(valClass);
        statsPanel.add(new JLabel("Level:"));   statsPanel.add(valLevel);
        statsPanel.add(new JLabel("HP:"));      statsPanel.add(valHp);
        statsPanel.add(new JLabel("Power:"));   statsPanel.add(valPower);
        statsPanel.add(new JLabel("Defense:")); statsPanel.add(valDef);
        statsPanel.add(new JLabel("Speed:"));   statsPanel.add(valSpeed);
        statsPanel.add(new JLabel("Gold:"));    statsPanel.add(valGold);
        statsPanel.add(new JLabel("Room:"));    statsPanel.add(valRoom);

        JPanel centerWrapper = new JPanel(new FlowLayout());
        centerWrapper.add(statsPanel);
        add(centerWrapper, BorderLayout.CENTER);

        JButton loadBtn    = new JButton("Load & Play");
        JButton backBtn    = new JButton("Back to Menu");
        JLabel  noSaveLbl  = new JLabel("", SwingConstants.CENTER);
        noSaveLbl.setForeground(Color.RED);
        loadBtn.setEnabled(false);

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
                        valClass.setText(rs.getString("class") != null ? rs.getString("class") : "None");
                        valLevel.setText(String.valueOf(rs.getInt("level")));
                        valHp.setText(String.valueOf(rs.getDouble("hp")));
                        valPower.setText(String.valueOf(rs.getDouble("power")));
                        valDef.setText(String.valueOf(rs.getDouble("defense")));
                        valSpeed.setText(String.valueOf(rs.getDouble("speed")));
                        valGold.setText(String.valueOf(rs.getInt("gold")));
                        valRoom.setText(rs.getString("room"));
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
            JOptionPane.showMessageDialog(this, "Game loaded!", "Loaded", JOptionPane.INFORMATION_MESSAGE);
            // TODO: cl.show(container, "Game");
        });

        backBtn.addActionListener(e -> cl.show(container, "Menu"));
    }
}