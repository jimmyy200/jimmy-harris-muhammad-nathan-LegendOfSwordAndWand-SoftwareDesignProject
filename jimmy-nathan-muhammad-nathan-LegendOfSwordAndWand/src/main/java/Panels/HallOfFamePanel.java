package Panels;

import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

// shows top players
public class HallOfFamePanel extends JPanel {

    private JTextArea leaderboardArea;

    public HallOfFamePanel(JPanel container, CardLayout cl) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Hall of Fame", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);
        leaderboardArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);

        JButton btnBack = new JButton("Back to Menu");
        btnBack.addActionListener(e -> cl.show(container, "Menu"));
        add(btnBack, BorderLayout.SOUTH);

        // refresh when shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshLeaderboard();
            }
        });
    }

    private void refreshLeaderboard() {
        leaderboardArea.setText(String.format("%-5s %-20s %10s%n", "Rank", "Username", "Score"));
        leaderboardArea.append("-".repeat(37) + "\n");
        ResultSet rs = DatabaseManager.getInstance().getHallOfFame();
        try {
            int rank = 1;
            if (rs != null) {
                while (rs.next()) {
                    leaderboardArea.append(String.format("%-5d %-20s %10d%n",
                            rank++,
                            rs.getString("username"),
                            rs.getInt("score")));
                }
            }
            if (rank == 1) {
                leaderboardArea.append("  No scores yet.\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
