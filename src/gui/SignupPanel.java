package gui;

import db.DBConnection;
import utils.PasswordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class SignupPanel extends JPanel {
    public SignupPanel(AuthFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

        JLabel title = new JLabel("Sign Up");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 40));
        nameField.setPreferredSize(new Dimension(300, 40));
        nameField.setMinimumSize(new Dimension(300, 40));
        nameField.setBorder(BorderFactory.createTitledBorder("Name"));

        JTextField emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(300, 40));
        emailField.setPreferredSize(new Dimension(300, 40));
        emailField.setMinimumSize(new Dimension(300, 40));
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));

        // Password field
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMinimumSize(new Dimension(300, 40));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        // Show password checkbox
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBackground(new Color(250, 250, 250));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('\u2022');
            }
        });

        // Password wrapper panel
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setBackground(new Color(250, 250, 250));
        passwordPanel.setMaximumSize(new Dimension(300, 80));
        passwordPanel.setPreferredSize(new Dimension(300, 80));
        passwordPanel.setMinimumSize(new Dimension(300, 80));
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createVerticalStrut(5));
        passwordPanel.add(showPasswordCheckBox);

        // Signup button
        JButton signupBtn = new JButton("Create Account");
        signupBtn.setAlignmentX(CENTER_ALIGNMENT);

        // Link to login
        JButton toLogin = new JButton("Already have an account? Login");
        toLogin.setAlignmentX(CENTER_ALIGNMENT);
        toLogin.setBorder(null);
        toLogin.setBackground(new Color(250, 250, 250));
        toLogin.setForeground(Color.BLUE);
        toLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Layout everything
        add(Box.createVerticalStrut(30));
        add(title);
        add(Box.createVerticalStrut(30));
        add(nameField);
        add(Box.createVerticalStrut(10));
        add(emailField);
        add(Box.createVerticalStrut(10));
        add(passwordPanel);
        add(Box.createVerticalStrut(20));
        add(signupBtn);
        add(Box.createVerticalStrut(20));
        add(toLogin);
        add(Box.createVerticalStrut(10));
        add(statusLabel);

        // Signup logic
        signupBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());

            if (name.isBlank() || email.isBlank() || pwd.isBlank()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            // Email format validation
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                statusLabel.setText("⚠️ Invalid email format.");
                statusLabel.setForeground(Color.RED);
                return;
            }

            // Name format validation
            if (!name.matches("^[A-Za-z ]{2,50}$")) {
                statusLabel.setText("⚠️ Name must contain only letters and spaces (2–50 chars).");
                statusLabel.setForeground(Color.RED);
                return;
            }

            String hashedPwd = PasswordUtils.hashPassword(pwd);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, hashedPwd);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Account created successfully!");
                parent.switchTo("login");
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(this, "Email already registered.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error occurred during signup.");
            }
        });

        toLogin.addActionListener(e -> parent.switchTo("login"));
    }
}
