import Panels.*;

import javax.swing.*;
import java.awt.*;

public class Main {
    public Main() {
        JFrame frame = new JFrame("Legend Of Sword and Wand");
        CardLayout cl = new CardLayout();
        JPanel container = new JPanel(cl);

        String[] currentUser = new String[1];

        GamePanel gamePanel = new GamePanel(container, cl, currentUser);

        container.add(new RegisterPanel(container, cl),                 "Register");
        container.add(new LoginPanel(container, cl, currentUser),       "Login");
        container.add(new MenuPanel(container, cl),                     "Menu");
        container.add(new PvEMenuPanel(container, cl),                  "PvEMenu");
        container.add(new ClassSelectPanel(container, cl, currentUser, gamePanel), "ClassSelect");
        container.add(new LoadGamePanel(container, cl, currentUser, gamePanel),    "LoadGame");
        container.add(new PvPPanel(container, cl, currentUser, gamePanel),         "PvP");
        container.add(gamePanel,                                        "Game");

        cl.show(container, "Login");

        frame.add(container);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}