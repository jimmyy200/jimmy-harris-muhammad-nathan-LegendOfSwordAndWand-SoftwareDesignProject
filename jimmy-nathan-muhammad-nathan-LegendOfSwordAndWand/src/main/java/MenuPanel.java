import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(JPanel container, CardLayout cl) {
        setBackground(Color.LIGHT_GRAY);
        add(new JLabel("MAIN MENU"));

        JButton btnGoSettings = new JButton("Go to Settings");
        // The button now knows how to tell the container to switch
        btnGoSettings.addActionListener(e -> cl.show(container, "Settings"));

        add(btnGoSettings);
    }
}