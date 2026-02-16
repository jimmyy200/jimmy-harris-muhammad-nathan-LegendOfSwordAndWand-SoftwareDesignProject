import javax.swing.*;
import java.awt.*;

public class Main {
    public Main() {
        JFrame frame = new JFrame("Modular Swing App");
        CardLayout cl = new CardLayout();
        JPanel container = new JPanel(cl);

        // We pass the 'container' and 'cl' so the panels can switch screens
        container.add(new MenuPanel(container, cl), "Menu");
        container.add(new SettingsPanel(container, cl), "Settings");

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