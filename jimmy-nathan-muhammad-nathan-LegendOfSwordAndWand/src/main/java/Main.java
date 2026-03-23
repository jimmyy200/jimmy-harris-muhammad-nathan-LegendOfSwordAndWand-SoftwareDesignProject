import Panels.*;

import javax.swing.*;
import java.awt.*;

// start of the program
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Legends of Sword and Wand");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            CardLayout cl = new CardLayout();
            JPanel container = new JPanel(cl);
            String[] currentUser = {""};

            // Create game panel first so other panels can reference it
            GamePanel      gamePanel      = new GamePanel(container, cl, currentUser);
            RegisterPanel  registerPanel  = new RegisterPanel(container, cl);
            LoginPanel     loginPanel     = new LoginPanel(container, cl, currentUser);
            MenuPanel      menuPanel      = new MenuPanel(container, cl);
            PvEMenuPanel   pveMenuPanel   = new PvEMenuPanel(container, cl);
            ClassSelectPanel classPanel   = new ClassSelectPanel(container, cl, currentUser, gamePanel);
            LoadGamePanel  loadGamePanel  = new LoadGamePanel(container, cl, currentUser, gamePanel);
            PvPPanel       pvpPanel       = new PvPPanel(container, cl, currentUser, gamePanel);
            HallOfFamePanel hofPanel      = new HallOfFamePanel(container, cl);

            container.add(registerPanel,  "Register");
            container.add(loginPanel,     "Login");
            container.add(menuPanel,      "Menu");
            container.add(pveMenuPanel,   "PvEMenu");
            container.add(classPanel,     "ClassSelect");
            container.add(loadGamePanel,  "LoadGame");
            container.add(pvpPanel,       "PvP");
            container.add(gamePanel,      "Game");
            container.add(hofPanel,       "HallOfFame");

            cl.show(container, "Login");
            frame.add(container);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
