package Panels;

import Factory.HeroFactory;
import Hero.*;
import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// pvp screen
public class PvPPanel extends JPanel {

    private final JPanel      container;
    private final CardLayout  cl;
    private final String[]    currentUser;
    private final GamePanel   gamePanel;

    private String       opponentUsername;
    private List<Hero>   player1Party;
    private List<Hero>   player2Party;

    private JTextField   txtOpponent;
    private JLabel       lblStatus;
    private JPanel       cardContainer;
    private CardLayout   cardCl;

    public PvPPanel(JPanel container, CardLayout cl, String[] currentUser, GamePanel gamePanel) {
        this.container   = container;
        this.cl          = cl;
        this.currentUser = currentUser;
        this.gamePanel   = gamePanel;

        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("PvP Battle", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        cardCl        = new CardLayout();
        cardContainer = new JPanel(cardCl);
        cardContainer.add(buildInviteStep(),   "Invite");
        cardContainer.add(buildP1PickStep(),   "P1Pick");
        cardContainer.add(buildP2PickStep(),   "P2Pick");
        cardContainer.add(buildLeagueStep(),   "League");
        cardCl.show(cardContainer, "Invite");
        add(cardContainer, BorderLayout.CENTER);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        add(lblStatus, BorderLayout.SOUTH);
    }

    // step 1 enter who you wanna fight

    private JPanel buildInviteStep() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 16, 8, 16);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.gridx  = 0;

        JLabel lbl = new JLabel("Enter opponent username:", SwingConstants.CENTER);
        txtOpponent = new JTextField(15);
        JButton btnInvite  = new JButton("Send Invitation");
        JButton btnLeague  = new JButton("View League Standings");
        JButton btnBack    = new JButton("Back to Menu");

        g.gridy = 0; p.add(lbl, g);
        g.gridy = 1; p.add(txtOpponent, g);
        g.gridy = 2; p.add(btnInvite, g);
        g.gridy = 3; p.add(btnLeague, g);
        g.gridy = 4; p.add(btnBack, g);

        btnInvite.addActionListener(e -> handleInvite());
        btnLeague.addActionListener(e -> {
            refreshLeague();
            cardCl.show(cardContainer, "League");
        });
        btnBack.addActionListener(e -> {
            reset();
            cl.show(container, "Menu");
        });

        return p;
    }

    private void handleInvite() {
        opponentUsername = txtOpponent.getText().trim();
        lblStatus.setText(" ");

        if (opponentUsername.isEmpty()) {
            lblStatus.setText("Please enter a username.");
            return;
        }
        if (opponentUsername.equals(currentUser[0])) {
            lblStatus.setText("You cannot invite yourself.");
            return;
        }
        if (!DatabaseManager.getInstance().userExists(opponentUsername)) {
            lblStatus.setText("User '" + opponentUsername + "' does not exist.");
            return;
        }
        if (!DatabaseManager.getInstance().hasSavedParty(currentUser[0])) {
            lblStatus.setText("You need a saved party! Complete a PvE campaign first.");
            return;
        }
        if (!DatabaseManager.getInstance().hasSavedParty(opponentUsername)) {
            lblStatus.setText(opponentUsername + " has no saved party.");
            return;
        }

        // fake accept invite
        int accept = JOptionPane.showConfirmDialog(this,
                opponentUsername + " has been invited!\n(Simulating: does " + opponentUsername + " accept?)",
                "PvP Invitation", JOptionPane.YES_NO_OPTION);

        if (accept != JOptionPane.YES_OPTION) {
            lblStatus.setText("Invitation declined.");
            return;
        }

        lblStatus.setText(" ");
        refreshP1Pick();
        cardCl.show(cardContainer, "P1Pick");
    }

    // step 2 player 1 picks a party

    private JPanel p1PickPanel;
    private JList<String> p1PartyList;
    private DefaultListModel<String> p1ListModel;

    private JPanel buildP1PickStep() {
        p1PickPanel  = new JPanel(new BorderLayout(8, 8));
        p1ListModel  = new DefaultListModel<>();
        p1PartyList  = new JList<>(p1ListModel);
        p1PartyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        p1PickPanel.add(lbl, BorderLayout.NORTH);
        p1PickPanel.putClientProperty("lbl", lbl);

        p1PickPanel.add(new JScrollPane(p1PartyList), BorderLayout.CENTER);

        JButton btnConfirm = new JButton("Choose this party");
        btnConfirm.addActionListener(e -> {
            int idx = p1PartyList.getSelectedIndex();
            if (idx < 0) { lblStatus.setText("Select a party slot."); return; }
            player1Party = DatabaseManager.getInstance().loadPvPParty(currentUser[0], idx);
            if (player1Party == null || player1Party.isEmpty()) {
                lblStatus.setText("Could not load party."); return;
            }
            lblStatus.setText(" ");
            refreshP2Pick();
            cardCl.show(cardContainer, "P2Pick");
        });
        p1PickPanel.add(btnConfirm, BorderLayout.SOUTH);
        return p1PickPanel;
    }

    private void refreshP1Pick() {
        JLabel lbl = (JLabel) p1PickPanel.getClientProperty("lbl");
        lbl.setText(currentUser[0] + " — choose your party");
        p1ListModel.clear();
        List<String> summaries = DatabaseManager.getInstance().getPvPPartySlotSummaries(currentUser[0]);
        for (String s : summaries) p1ListModel.addElement(s);
        if (!summaries.isEmpty()) p1PartyList.setSelectedIndex(0);
    }

    // step 3 player 2 picks a party

    private JPanel p2PickPanel;
    private JList<String> p2PartyList;
    private DefaultListModel<String> p2ListModel;

    private JPanel buildP2PickStep() {
        p2PickPanel  = new JPanel(new BorderLayout(8, 8));
        p2ListModel  = new DefaultListModel<>();
        p2PartyList  = new JList<>(p2ListModel);
        p2PartyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        p2PickPanel.add(lbl, BorderLayout.NORTH);
        p2PickPanel.putClientProperty("lbl", lbl);

        p2PickPanel.add(new JScrollPane(p2PartyList), BorderLayout.CENTER);

        JButton btnConfirm = new JButton("Choose this party");
        btnConfirm.addActionListener(e -> {
            int idx = p2PartyList.getSelectedIndex();
            if (idx < 0) { lblStatus.setText("Select a party slot."); return; }
            player2Party = DatabaseManager.getInstance().loadPvPParty(opponentUsername, idx);
            if (player2Party == null || player2Party.isEmpty()) {
                lblStatus.setText("Could not load party."); return;
            }
            lblStatus.setText(" ");
            startPvPBattle();
        });
        p2PickPanel.add(btnConfirm, BorderLayout.SOUTH);
        return p2PickPanel;
    }

    private void refreshP2Pick() {
        JLabel lbl = (JLabel) p2PickPanel.getClientProperty("lbl");
        lbl.setText(opponentUsername + " — choose your party");
        p2ListModel.clear();
        List<String> summaries = DatabaseManager.getInstance().getPvPPartySlotSummaries(opponentUsername);
        for (String s : summaries) p2ListModel.addElement(s);
        if (!summaries.isEmpty()) p2PartyList.setSelectedIndex(0);
    }

    // step 4 league rankings

    private JTextArea leagueArea;

    private JPanel buildLeagueStep() {
        JPanel p  = new JPanel(new BorderLayout(8, 8));
        JLabel lbl = new JLabel("League Standings", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        p.add(lbl, BorderLayout.NORTH);

        leagueArea = new JTextArea();
        leagueArea.setEditable(false);
        leagueArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        p.add(new JScrollPane(leagueArea), BorderLayout.CENTER);

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> cardCl.show(cardContainer, "Invite"));
        p.add(btnBack, BorderLayout.SOUTH);
        return p;
    }

    private void refreshLeague() {
        leagueArea.setText(String.format("%-20s %5s %5s%n", "Username", "Wins", "Losses"));
        leagueArea.append("-".repeat(32) + "\n");
        ResultSet rs = DatabaseManager.getInstance().getLeagueStandings();
        try {
            if (rs != null) {
                while (rs.next()) {
                    leagueArea.append(String.format("%-20s %5d %5d%n",
                            rs.getString("username"),
                            rs.getInt("wins"),
                            rs.getInt("losses")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // launch the battle

    private void startPvPBattle() {
        gamePanel.startPvPBattle(
                player1Party, currentUser[0],
                player2Party, opponentUsername,
                (winner, loser) -> {
                    DatabaseManager.getInstance().recordPvPResult(winner, loser);
                    JOptionPane.showMessageDialog(this,
                            winner + " wins the PvP battle!\nLeague standings updated.",
                            "PvP Result", JOptionPane.INFORMATION_MESSAGE);
                    reset();
                    refreshLeague();
                    cardCl.show(cardContainer, "League");
                    cl.show(container, "PvP");
                }
        );
        cl.show(container, "Game");
    }

    private void reset() {
        opponentUsername = null;
        player1Party     = null;
        player2Party     = null;
        txtOpponent.setText("");
        lblStatus.setText(" ");
        cardCl.show(cardContainer, "Invite");
    }
}