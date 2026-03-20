package Panels;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(JPanel container, CardLayout cl) {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new FlowLayout());

        add(new JLabel("MAIN MENU"));

        JButton btnPvE     = new JButton("PvE Campaign");
        JButton btnPvP     = new JButton("PvP Battle");

        btnPvE.addActionListener(e -> cl.show(container, "PvEMenu"));
        btnPvP.addActionListener(e -> cl.show(container, "PvP"));

        add(btnPvE);
        add(btnPvP);
    }
}