import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    public LoginPanel(JPanel container, CardLayout cl) {
        setLayout(new GridBagLayout()); // Centers the login box

        JPanel loginBox = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);
        JButton loginBtn = new JButton("Login");
        JLabel statusLabel = new JLabel("Enter Credentials", SwingConstants.CENTER);

        loginBox.add(new JLabel("Username:"));
        loginBox.add(userField);
        loginBox.add(new JLabel("Password:"));
        loginBox.add(passField);
        loginBox.add(new JLabel("")); // Spacer
        loginBox.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String password = new String(passField.getPassword());
            // Hardcoded check for demonstration
            if (userField.getText().equals("admin") && password.equals("1234")) {
                cl.show(container, "Menu");
                passField.setText(""); // Clear password for security
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(loginBox);
    }
}