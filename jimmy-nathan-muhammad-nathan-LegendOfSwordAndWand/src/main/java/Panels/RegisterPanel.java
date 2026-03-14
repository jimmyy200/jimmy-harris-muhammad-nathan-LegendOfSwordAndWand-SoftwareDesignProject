package Panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterPanel extends JPanel {
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/localDB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass";

    public RegisterPanel(JPanel container, CardLayout cl) {
        setLayout(new GridBagLayout());

        JPanel registerBox = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);
        JPasswordField confirmPassField = new JPasswordField(10);
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");

        registerBox.add(new JLabel("Username:"));
        registerBox.add(userField);
        registerBox.add(new JLabel("Password:"));
        registerBox.add(passField);
        registerBox.add(new JLabel("Confirm Password:"));
        registerBox.add(confirmPassField);
        registerBox.add(backBtn);
        registerBox.add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                userField.setText("");
                passField.setText("");
                confirmPassField.setText("");
                cl.show(container, "Login");
            }
        });

        backBtn.addActionListener(e -> cl.show(container, "Login"));

        add(registerBox);
    }

    private boolean registerUser(String username, String password) {
        // Check if username already exists
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Check duplicate
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            // Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                return true;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}