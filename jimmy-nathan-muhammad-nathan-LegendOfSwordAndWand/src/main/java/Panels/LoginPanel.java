package Panels;

import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    public LoginPanel(JPanel container, CardLayout cl, String[] currentUser) {
        setLayout(new GridBagLayout());

        JPanel loginBox = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);
        JButton loginBtn    = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        loginBox.add(new JLabel("Username:"));
        loginBox.add(userField);
        loginBox.add(new JLabel("Password:"));
        loginBox.add(passField);
        loginBox.add(registerBtn);
        loginBox.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both fields.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (DatabaseManager.getInstance().auth.validateLogin(username, password)) {
                currentUser[0] = username;
                cl.show(container, "Menu");
                passField.setText("");
                userField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> cl.show(container, "Register"));
        add(loginBox);
    }
}