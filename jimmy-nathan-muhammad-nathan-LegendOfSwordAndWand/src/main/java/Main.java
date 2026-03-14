import Panels.LoginPanel;
import Panels.MenuPanel;
import Panels.SettingsPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public Main() {
        JFrame frame = new JFrame("Legend Of Sword and Wand");
        CardLayout cl = new CardLayout();
        JPanel container = new JPanel(cl);

        // Add screens to the container
        container.add(new LoginPanel(container, cl), "Login");
        container.add(new MenuPanel(container, cl), "Menu");
        container.add(new SettingsPanel(container, cl), "Settings");

        // Ensure Login is the first thing seen
        cl.show(container, "Login");

        frame.add(container);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}