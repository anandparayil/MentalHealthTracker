package gui;

import db.DBConnection;
import utils.PasswordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class LoginPanel extends JPanel {
    public LoginPanel(AuthFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

        JLabel title = new JLabel("Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(CENTER_ALIGNMENT);

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
        showPasswordCheckBox.setBackground(new Color(245, 245, 245));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('\u2022');
            }
        });

        // Wrap password field + checkbox
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setBackground(new Color(245, 245, 245));
        passwordPanel.setMaximumSize(new Dimension(300, 80));
        passwordPanel.setPreferredSize(new Dimension(300, 80));
        passwordPanel.setMinimumSize(new Dimension(300, 80));
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createVerticalStrut(5));
        passwordPanel.add(showPasswordCheckBox);

        // Login button
        JButton loginBtn = new JButton("Login");
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);

        // Link to signup
        JButton toSignup = new JButton("Don't have an account? Sign up");
        toSignup.setAlignmentX(CENTER_ALIGNMENT);
        toSignup.setBorder(null);
        toSignup.setBackground(new Color(245, 245, 245));
        toSignup.setForeground(Color.BLUE);
        toSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Layout components
        add(Box.createVerticalStrut(30));
        add(title);
        add(Box.createVerticalStrut(30));
        add(emailField);
        add(Box.createVerticalStrut(10));
        add(passwordPanel);
        add(Box.createVerticalStrut(20));
        add(loginBtn);
        add(Box.createVerticalStrut(20));
        add(toSignup);
        add(Box.createVerticalStrut(10));
        add(statusLabel);

        // Login logic
        loginBtn.addActionListener((ActionEvent e) -> {
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());

            if (email.isEmpty() || pwd.isEmpty()) {
                statusLabel.setText("Please enter both email and password.");
                statusLabel.setForeground(Color.BLACK);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String hashedPwd = rs.getString("password");
                    if (PasswordUtils.checkPassword(pwd, hashedPwd)) {
                        statusLabel.setText("✅ Login successful!");
                        statusLabel.setForeground(new Color(0, 128, 0)); // green
                        SwingUtilities.getWindowAncestor(this).dispose(); // Close login window
                        new MoodTrackerFrame(rs.getInt("id")); // Launch tracker
                    } else {
                        statusLabel.setText("❌ Incorrect password.");
                        statusLabel.setForeground(Color.RED);
                    }
                } else {
                    statusLabel.setText("⚠️ User not found.");
                    statusLabel.setForeground(Color.BLACK);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Something went wrong. Please try again later.");
                ex.printStackTrace(); // For debugging
            }

            // Make Enter key trigger login
            InputMap im = getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke("ENTER"), "login");
            am.put("login", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginBtn.doClick(); // Simulate button click
                }
            });

        });

        // Make Enter key trigger login
        passwordField.addActionListener(e -> loginBtn.doClick());

        // Switch to signup
        toSignup.addActionListener(e -> parent.switchTo("signup"));
    }
}
