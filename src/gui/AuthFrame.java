package gui;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {
    CardLayout cardLayout;
    JPanel cardPanel;

    public AuthFrame() {
        setTitle("Mental Health Tracker");
        setSize(800, 600);
        setExtendedState(JFrame.NORMAL);
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center it
        setResizable(true); // Allow resizing & maximize

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        LoginPanel login = new LoginPanel(this);
        SignupPanel signup = new SignupPanel(this);

        cardPanel.add(login, "login");
        cardPanel.add(signup, "signup");

        add(cardPanel);
        setVisible(true);
    }

    public void switchTo(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }
}
