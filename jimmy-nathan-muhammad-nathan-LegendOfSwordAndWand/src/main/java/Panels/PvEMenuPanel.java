package Panels;

import javax.swing.*;
import java.awt.*;

public class PvEMenuPanel extends JPanel {
    public PvEMenuPanel(JPanel container, CardLayout cl) {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new FlowLayout());

        add(new JLabel("PvE CAMPAIGN"));

        JButton btnNewGame    = new JButton("New Game");
        JButton btnLoadGame   = new JButton("Load Game");
        JButton btnBack       = new JButton("Back");

        btnNewGame.addActionListener(e    -> cl.show(container, "ClassSelect"));
        btnLoadGame.addActionListener(e   -> cl.show(container, "LoadGame"));
        btnBack.addActionListener(e       -> cl.show(container, "Menu"));

        add(btnNewGame);
        add(btnLoadGame);
        add(btnBack);
    }
}