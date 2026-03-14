package Panels;

import Singleton.DatabaseManager;
import Singleton.GameEngine;

import javax.swing.*;
import java.awt.*;

public class ClassSelectPanel extends JPanel {
    private static final String[] CLASSES      = {"Warrior", "Mage", "Order", "Chaos"};
    private static final String[] DESCRIPTIONS = {
            "Balanced fighter with high HP and solid defense.",
            "Devastating power, but fragile. Master of arcane arts.",
            "Disciplined tank. Boosts defense with every level.",
            "High speed and power, but reckless — attacks cost HP."
    };

    public ClassSelectPanel(JPanel container, CardLayout cl, String[] currentUser) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Choose Your Class", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel classBox = new JPanel(new GridLayout(2, 2, 10, 10));
        classBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (int i = 0; i < CLASSES.length; i++) {
            final String className = CLASSES[i];
            final String desc      = DESCRIPTIONS[i];

            JButton btn = new JButton("<html><center><b>" + className + "</b><br><small>" + desc + "</small></center></html>");
            btn.setPreferredSize(new Dimension(160, 80));
            btn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Choose " + className + " as your class?", "Confirm Class", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.getInstance().saveClass(currentUser[0], className);
                    DatabaseManager.getInstance().saveGame(currentUser[0]);
                    GameEngine.getInstance().startGame();
                    // TODO: cl.show(container, "Game"); — wire to your game screen
                    JOptionPane.showMessageDialog(this, "Starting new game as " + className + "!", "Let's Go", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            classBox.add(btn);
        }

        add(classBox, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> cl.show(container, "Menu"));
        JPanel bottom = new JPanel();
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }
}