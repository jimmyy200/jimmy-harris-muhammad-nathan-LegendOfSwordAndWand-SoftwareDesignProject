package Panels;

import Hero.*;
import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

        // ── Title ──
        JLabel title = new JLabel("Build Your Party (1–5 Heroes)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // ── Class buttons ──
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

        // ── Bottom: party display + buttons ──
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        partyLabel = new JLabel("Party: (none)", SwingConstants.CENTER);
        partyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bottomPanel.add(partyLabel, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout());

        JButton undoBtn  = new JButton("↩ Undo Last");
        JButton startBtn = new JButton("▶ Start Game");
        JButton backBtn  = new JButton("Back to Menu");

        btnRow.add(undoBtn);
        btnRow.add(startBtn);
        btnRow.add(backBtn);
        bottomPanel.add(btnRow, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ── Undo last selection ──
        undoBtn.addActionListener(e -> {
            if (!selectedClasses.isEmpty()) {
                selectedClasses.remove(selectedClasses.size() - 1);
                refreshPartyLabel();
            }
        });

        // ── Start game ──
        startBtn.addActionListener(e -> {
            if (selectedClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pick at least 1 hero!", "No Heroes", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DatabaseManager.getInstance().saveClass(currentUser[0], selectedClasses.get(0));
            DatabaseManager.getInstance().saveGame(currentUser[0]);

            // Build party list and pass to GamePanel
            List<Hero> party = new ArrayList<>();
            for (int i = 0; i < selectedClasses.size(); i++) {
                String heroName = currentUser[0] + (i == 0 ? "" : "-" + (i + 1));
                party.add(createHero(selectedClasses.get(i), heroName));
            }
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

    private Hero createHero(String heroClass, String heroName) {
        switch (heroClass.toUpperCase()) {
            case "WARRIOR": return new Warrior(heroName);
            case "MAGE":    return new Mage(heroName);
            case "ORDER":   return new Order(heroName);
            case "CHAOS":   return new Chaos(heroName);
            default:        return new Warrior(heroName);
        }
    }
}