package gui;

import db.DBConnection;
import models.MoodHistoryFrame;
import models.MoodChartWindow;
import models.MeditationWindow;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class MoodTrackerFrame extends JFrame {

    int userId;

    public MoodTrackerFrame(int userId) {
        this.userId = userId;

        setTitle("Mood Tracker");
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("How are you feeling today?");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(CENTER_ALIGNMENT);

        String[] moods = {"Choose Mood", "😢", "😐", "😊", "😎", "😍"};
        JComboBox<String> moodCombo = new JComboBox<>(moods);
        moodCombo.setMaximumSize(new Dimension(150, 30));
        moodCombo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel legendLabel = new JLabel("<html><b>Mood Legend:</b><br>" +
                "😢 = 1 (Sad) <br>" +
                "😐 = 2 (Neutral) <br>" +
                "😊 = 3 (Happy) <br>" +
                "😎 = 4 (Excited) <br>" +
                "😍 = 5 (Loved)</html>");
        legendLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        legendLabel.setAlignmentX(CENTER_ALIGNMENT);

        JTextArea journalArea = new JTextArea(5, 30);
        journalArea.setLineWrap(true);
        journalArea.setWrapStyleWord(true);
        journalArea.setBorder(BorderFactory.createTitledBorder("Write your thoughts..."));

        JScrollPane scrollPane = new JScrollPane(journalArea);

        JLabel moodLabel = new JLabel("Selected Mood: ");
        moodLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        moodLabel.setAlignmentX(CENTER_ALIGNMENT);

        JButton addEntryBtn = new JButton("Add Mood Entry");
        addEntryBtn.setAlignmentX(CENTER_ALIGNMENT);

        JButton meditationBtn = new JButton("️Meditations");
        JButton tipsBtn = new JButton("💡 Tips");
        JButton historyBtn = new JButton("📜 Mood History");
        JButton chartBtn = new JButton("📈 Mood Chart");
        JButton logoutBtn = new JButton("🚪 Logout");

        meditationBtn.setAlignmentX(CENTER_ALIGNMENT);
        tipsBtn.setAlignmentX(CENTER_ALIGNMENT);
        historyBtn.setAlignmentX(CENTER_ALIGNMENT);
        chartBtn.setAlignmentX(CENTER_ALIGNMENT);
        logoutBtn.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(moodCombo);
        panel.add(Box.createVerticalStrut(15));
        panel.add(legendLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(15));
        panel.add(moodLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addEntryBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(meditationBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(tipsBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(historyBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(chartBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(logoutBtn);

        add(panel);
        setVisible(true);

        showDailyAffirmation();

        addEntryBtn.addActionListener((ActionEvent e) -> {
            String selectedMood = moodCombo.getSelectedItem().toString();
            int moodLevel;

            switch (selectedMood) {
                case "😢":
                    moodLevel = 1;
                    break;
                case "😐":
                    moodLevel = 2;
                    break;
                case "😊":
                    moodLevel = 3;
                    break;
                case "😎":
                    moodLevel = 4;
                    break;
                case "😍":
                    moodLevel = 5;
                    break;
                default:
                    moodLevel = 0; // Handle default case
                    break;
            }

            if (moodCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a mood.");
                return;
            }

            String note = journalArea.getText().trim();
            String today = LocalDate.now().toString();

            if (note.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a note before saving.");
                return; 
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO mood_entries (user_id, mood_level, note, date_logged) VALUES (?, ?, ?, ?)"
                );
                stmt.setInt(1, userId);
                stmt.setInt(2, moodLevel);
                stmt.setString(3, note);
                stmt.setString(4, today);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Mood entry saved!");
                journalArea.setText(""); // clear journal
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save entry.");
            }
        });

        tipsBtn.addActionListener(e -> {
            String[] tips = {
                    "💡 Tip: Journal daily for clarity. Small steps = Big change.",
                    "💡 Tip: Deep breaths can reset your mood. Try it now!",
                    "💡 Tip: Don't be hard on yourself. You're doing your best.",
                    "💡 Tip: Celebrate little wins — they add up!",
                    "💡 Tip: Drink water, sit up straight, and smile for 5 seconds 😄"
            };

            int randomIndex = new Random().nextInt(tips.length);
            JOptionPane.showMessageDialog(this, tips[randomIndex]);
        });

        meditationBtn.addActionListener(e -> new MeditationWindow());

        historyBtn.addActionListener(e -> new MoodHistoryFrame(userId));

        chartBtn.addActionListener(e -> new MoodChartWindow(userId));

        logoutBtn.addActionListener(e -> {
            dispose(); // Close current window
            new AuthFrame(); // Go back to login/signup
        });

        // Add listener to moodCombo to update the mood label when the mood is selected
        moodCombo.addActionListener(e -> {
            String selectedMood = moodCombo.getSelectedItem().toString();
            int moodLevel;

            switch (selectedMood) {
                case "😢":
                    moodLevel = 1;
                    break;
                case "😐":
                    moodLevel = 2;
                    break;
                case "😊":
                    moodLevel = 3;
                    break;
                case "😎":
                    moodLevel = 4;
                    break;
                case "😍":
                    moodLevel = 5;
                    break;
                default:
                    moodLevel = 0;
                    break;
            }

            // Update the moodLabel with the selected emoji and its corresponding number
            moodLabel.setText("Selected Mood: " + selectedMood + " (" + moodLevel + ")");
        });

        moodCombo.addActionListener(e -> {
            String selectedMood = moodCombo.getSelectedItem().toString();
            int moodLevel;
            String feedbackMsg;

            switch (selectedMood) {
                case "😢":
                    moodLevel = 1;
                    feedbackMsg = "It's okay to feel down. You're not alone 💙";
                    break;
                case "😐":
                    moodLevel = 2;
                    feedbackMsg = "A neutral day is still a day won. Keep going!";
                    break;
                case "😊":
                    moodLevel = 3;
                    feedbackMsg = "Glad to see you smiling 😊 Spread that joy!";
                    break;
                case "😎":
                    moodLevel = 4;
                    feedbackMsg = "You're rocking it today! Keep that energy 🔥";
                    break;
                case "😍":
                    moodLevel = 5;
                    feedbackMsg = "Love is in the air 🌸 Cherish this feeling!";
                    break;
                default:
                    moodLevel = 0;
                    feedbackMsg = "";
                    break;
            }

            moodLabel.setText("Selected Mood: " + selectedMood + " (" + moodLevel + ")");

            if (!feedbackMsg.isEmpty()) {
                JOptionPane.showMessageDialog(this, feedbackMsg, "Mood Boost", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void showDailyAffirmation() {
        String today = LocalDate.now().toString();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT * FROM mood_entries WHERE user_id = ? AND date_logged = ?"
            );
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, today);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                // Show affirmation only if no entry today
                Statement stmt = conn.createStatement();
                ResultSet affirms = stmt.executeQuery("SELECT * FROM affirmations ORDER BY RAND() LIMIT 1");
                if (affirms.next()) {
                    JOptionPane.showMessageDialog(this,
                            "💖 Daily Affirmation:\n" + affirms.getString("text"),
                            "Affirmation", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch affirmation: " + e.getMessage());
        }
    }
}
