package Panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Hero.Hero;
import Singleton.DatabaseManager;

public class ClassSelectPanel extends JPanel {
    private static final String[] CLASSES      = {"Warrior", "Mage", "Order", "Chaos"};
    private static final String[] DESCRIPTIONS = {
            "Balanced fighter with high HP and solid defense.",
            "Devastating power, but fragile. Master of arcane arts.",
            "Disciplined tank. Boosts defense with every level.",
            "High speed and power, but reckless — attacks cost HP."
    };
    private static final int MAX_PARTY = 5;

    private final List<String> selectedClasses = new ArrayList<>();
    private JLabel partyLabel;

    public ClassSelectPanel(JPanel container, CardLayout cl, String[] currentUser, GamePanel gamePanel) {
        setLayout(new BorderLayout(10, 10));

        // title of the screen
        JLabel title = new JLabel("Build Your Party (1–5 Heroes)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // buttons for picking classes
        JPanel classBox = new JPanel(new GridLayout(2, 2, 10, 10));
        classBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        for (int i = 0; i < CLASSES.length; i++) {
            final String className = CLASSES[i];
            final String desc      = DESCRIPTIONS[i];

            JButton btn = new JButton("<html><center><b>" + className + "</b><br><small>" + desc + "</small></center></html>");
            btn.setPreferredSize(new Dimension(160, 80));
            btn.addActionListener(e -> {
                if (selectedClasses.size() >= MAX_PARTY) {
                    JOptionPane.showMessageDialog(this, "Party is full! (Max 5)", "Party Full", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selectedClasses.add(className);
                refreshPartyLabel();
            });
            classBox.add(btn);
        }
        add(classBox, BorderLayout.CENTER);

        // bottom part for party and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        partyLabel = new JLabel("Party: (none)", SwingConstants.CENTER);
        partyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bottomPanel.add(partyLabel, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout());

        JButton undoBtn  = new JButton("Undo Last");
        JButton startBtn = new JButton("Start Game");
        JButton backBtn  = new JButton("Back to Menu");

        btnRow.add(undoBtn);
        btnRow.add(startBtn);
        btnRow.add(backBtn);
        bottomPanel.add(btnRow, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // undo button code
        undoBtn.addActionListener(e -> {
            if (!selectedClasses.isEmpty()) {
                selectedClasses.remove(selectedClasses.size() - 1);
                refreshPartyLabel();
            }
        });

        // start game button code
        startBtn.addActionListener(e -> {
            if (selectedClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pick at least 1 hero!", "No Heroes", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Refactor 10 - Inappropriate Intimacy
            // Delegate all DB and factory work to SaveRepository
            DatabaseManager.getInstance().save.initNewGame(currentUser[0], selectedClasses.get(0));
            List<Hero> party = DatabaseManager.getInstance().save.buildParty(selectedClasses, currentUser[0]);

            gamePanel.startNewGame(party);
            selectedClasses.clear();
            refreshPartyLabel();
            cl.show(container, "Game");
        });

        backBtn.addActionListener(e -> {
            selectedClasses.clear();
            refreshPartyLabel();
            cl.show(container, "Menu");
        });
    }

    private void refreshPartyLabel() {
        if (selectedClasses.isEmpty()) {
            partyLabel.setText("Party: (none)");
        } else {
            partyLabel.setText("Party (" + selectedClasses.size() + "/5): " + String.join(", ", selectedClasses));
        }
    }
}
