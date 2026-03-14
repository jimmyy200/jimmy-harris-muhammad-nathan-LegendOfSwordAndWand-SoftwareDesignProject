package Panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPanel extends JPanel {
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/localDB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass";

    public LoginPanel(JPanel container, CardLayout cl) {
        setLayout(new GridBagLayout());

        JPanel loginBox = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Go Register");

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

            if (validateLogin(username, password)) {
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

    private boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}