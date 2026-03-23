package Panels;

import javax.swing.*;
import java.awt.*;

// main menu navigation
public class MenuPanel extends JPanel {
    public MenuPanel(JPanel container, CardLayout cl) {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 16, 8, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("MAIN MENU", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton btnPvE     = new JButton("PvE Campaign");
        JButton btnPvP     = new JButton("PvP Battle");
        JButton btnHoF     = new JButton("Hall of Fame");
        JButton btnLogout  = new JButton("Logout");

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(btnPvE, gbc);
        gbc.gridy = 2; add(btnPvP, gbc);
        gbc.gridy = 3; add(btnHoF, gbc);
        gbc.gridy = 4; add(btnLogout, gbc);

        btnPvE.addActionListener(e    -> cl.show(container, "PvEMenu"));
        btnPvP.addActionListener(e    -> cl.show(container, "PvP"));
        btnHoF.addActionListener(e    -> cl.show(container, "HallOfFame"));
        btnLogout.addActionListener(e -> cl.show(container, "Login"));
    }
}