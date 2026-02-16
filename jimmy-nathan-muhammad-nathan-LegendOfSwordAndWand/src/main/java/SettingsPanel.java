import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    public SettingsPanel(JPanel container, CardLayout cl) {
        setBackground(Color.CYAN);
        add(new JLabel("SETTINGS SCREEN"));

        JButton btnBack = new JButton("Back to Menu");
        btnBack.addActionListener(e -> cl.show(container, "Menu"));

        add(btnBack);
    }
}