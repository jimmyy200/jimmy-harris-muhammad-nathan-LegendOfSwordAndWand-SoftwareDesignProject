package Panels;

import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
    public RegisterPanel(JPanel container, CardLayout cl) {
        setLayout(new GridBagLayout());

        JPanel registerBox = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField userField        = new JTextField(10);
        JPasswordField passField    = new JPasswordField(10);
        JPasswordField confirmField = new JPasswordField(10);
        JButton registerBtn = new JButton("Register");
        JButton backBtn     = new JButton("Back to Login");

        registerBox.add(new JLabel("Username:"));
        registerBox.add(userField);
        registerBox.add(new JLabel("Password:"));
        registerBox.add(passField);
        registerBox.add(new JLabel("Confirm Password:"));
        registerBox.add(confirmField);
        registerBox.add(backBtn);
        registerBox.add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirm  = new String(confirmField.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = DatabaseManager.getInstance().registerUser(username, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                userField.setText(""); passField.setText(""); confirmField.setText("");
                cl.show(container, "Login");
            } else {
                JOptionPane.showMessageDialog(this, "Username already taken!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> cl.show(container, "Login"));
        add(registerBox);
    }
}