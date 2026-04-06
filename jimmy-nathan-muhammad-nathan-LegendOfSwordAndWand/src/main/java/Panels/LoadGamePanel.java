package Panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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

    // Refactor 10 - Inappropriate Intimacy
    // Panel now delegates all DB and factory work to SaveRepository
    private void refreshSaveData() {
        loadedParty.clear();
        loadBtn.setEnabled(false);
        noSaveLbl.setText("");
        valParty.setText("-");

        try {
            // Refactor 10 - use repository method instead of parsing ResultSet here
            List<Hero> heroes = DatabaseManager.getInstance().save.loadPartyAsHeroes(currentUser[0]);

            if (heroes.isEmpty()) {
                noSaveLbl.setText("No save file found.");
                return;
            }

            loadedParty.addAll(heroes);
            StringBuilder summary = new StringBuilder("<html>");
            for (Hero h : heroes) {
                summary.append(h.getName()).append(" [Lv").append(h.getLevel()).append("]<br>");
            }
            summary.append("</html>");
            valParty.setText(summary.toString());
            loadBtn.setEnabled(true);

            // Refactor 10 - use repository method instead of parsing ResultSet here
            int[] meta = DatabaseManager.getInstance().save.loadGameMeta(currentUser[0]);
            if (meta != null) {
                savedMeta[0] = meta[0];
                savedMeta[1] = meta[1];
                valGold.setText(String.valueOf(meta[0]));
                valRoom.setText(String.valueOf(meta[1]));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            noSaveLbl.setText("Database error occurred.");
        }
    }
}
