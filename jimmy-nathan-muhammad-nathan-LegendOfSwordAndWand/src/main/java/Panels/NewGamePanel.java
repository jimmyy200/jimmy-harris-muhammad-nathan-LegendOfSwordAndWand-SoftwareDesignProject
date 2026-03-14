package Panels;

import Singleton.DatabaseManager;
import Singleton.GameEngine;

import javax.swing.*;
import java.awt.*;

public class NewGamePanel extends JPanel {
    public NewGamePanel(JPanel container, CardLayout cl, String[] currentUser) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 16, 8, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("New Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel info = new JLabel("<html><center>Starting a new game will<br>overwrite your existing save!</center></html>", SwingConstants.CENTER);
        info.setForeground(Color.RED);

        JButton confirmBtn = new JButton("Start New Game");
        JButton backBtn    = new JButton("Back to Menu");

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(info, gbc);
        gbc.gridy = 2; add(confirmBtn, gbc);
        gbc.gridy = 3; add(backBtn, gbc);

        confirmBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure? This will overwrite your current save.",
                    "New Game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                if (DatabaseManager.getInstance().saveGame(currentUser[0])) {
                    GameEngine.getInstance().startGame();
                    JOptionPane.showMessageDialog(this, "New game started!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    cl.show(container, "Menu");
                }
            }
        });

        backBtn.addActionListener(e -> cl.show(container, "Menu"));
    }
}