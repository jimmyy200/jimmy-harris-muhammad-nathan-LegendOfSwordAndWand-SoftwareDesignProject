package Panels;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(JPanel container, CardLayout cl) {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new FlowLayout());

        add(new JLabel("MAIN MENU"));

        JButton btnNewGame    = new JButton("New Game");
        JButton btnLoadGame   = new JButton("Load Game");
        JButton btnGoSettings = new JButton("Go to Settings");

        btnNewGame.addActionListener(e    -> cl.show(container, "ClassSelect"));
        btnLoadGame.addActionListener(e   -> cl.show(container, "LoadGame"));
        btnGoSettings.addActionListener(e -> cl.show(container, "Settings"));

        add(btnNewGame);
        add(btnLoadGame);
        add(btnGoSettings);
    }
}