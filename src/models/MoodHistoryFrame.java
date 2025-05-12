package models;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;

public class MoodHistoryFrame extends JFrame {

    private int userId;
    private JPanel entriesPanel;
    private JTextField dateField;
    private JComboBox<String> moodFilterCombo;

    public MoodHistoryFrame(int userId) {
        this.userId = userId;
        setTitle("Mood History");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Top search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        dateField = new JTextField(10);
        String[] moodOptions = {"All", "1 - 😢", "2 - 😐", "3 - 😊", "4 - 😎", "5 - 😍"};
        moodFilterCombo = new JComboBox<>(moodOptions);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");

        searchPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        searchPanel.add(dateField);
        searchPanel.add(new JLabel("Mood:"));
        searchPanel.add(moodFilterCombo);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);

        // Scrollable entries panel
        entriesPanel = new JPanel();
        entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(entriesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        searchBtn.addActionListener(e -> loadEntries(dateField.getText().trim()));
        showAllBtn.addActionListener(e -> {
            dateField.setText("");
            moodFilterCombo.setSelectedIndex(0); // Reset to "All"
            loadEntries(null);
        });

        loadEntries(null); // load all initially
        setVisible(true);
    }

    private void loadEntries(String dateFilter) {
        entriesPanel.removeAll();

        try (Connection conn = DBConnection.getConnection()) {
            String moodSelected = moodFilterCombo.getSelectedItem().toString();
            boolean moodFiltered = !moodSelected.equals("All");
            int moodLevel = moodFiltered ? Integer.parseInt(moodSelected.substring(0, 1)) : -1;

            StringBuilder query = new StringBuilder("SELECT * FROM mood_entries WHERE user_id = ?");
            ArrayList<Object> params = new ArrayList<>();
            params.add(userId);

            if (dateFilter != null && !dateFilter.isEmpty()) {
                query.append(" AND date_logged = ?");
                params.add(dateFilter);
            }

            if (moodFiltered) {
                query.append(" AND mood_level = ?");
                params.add(moodLevel);
            }

            query.append(" ORDER BY date_logged DESC");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;

            while (rs.next()) {
                int entryId = rs.getInt("id"); // Primary key
                hasResults = true;
                String mood = rs.getString("mood_level");
                String note = rs.getString("note");
                String date = rs.getString("date_logged");

                JPanel entryPanel = new JPanel();
                entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
                entryPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JLabel dateLabel = new JLabel("Date: " + date);
                JLabel moodLabel = new JLabel("Mood: " + mood);
                JTextArea noteArea = new JTextArea(note);
                noteArea.setWrapStyleWord(true);
                noteArea.setLineWrap(true);
                noteArea.setEditable(false);
                noteArea.setBackground(new Color(245, 245, 245));
                noteArea.setBorder(BorderFactory.createEmptyBorder());

                entryPanel.add(dateLabel);
                entryPanel.add(moodLabel);
                entryPanel.add(Box.createVerticalStrut(5));
                entryPanel.add(noteArea);
                entryPanel.add(Box.createVerticalStrut(10));

                JButton deleteBtn = new JButton("🗑 Delete");
                deleteBtn.setForeground(Color.RED);
                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection deleteConn = DBConnection.getConnection(); // New connection here
                             PreparedStatement delStmt = deleteConn.prepareStatement("DELETE FROM mood_entries WHERE id = ?")) {
                            delStmt.setInt(1, entryId);
                            delStmt.executeUpdate();
                            loadEntries(null); // Reload all entries after deletion
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Failed to delete entry.");
                        }
                    }
                });

                entryPanel.add(deleteBtn);

                entriesPanel.add(entryPanel);
                entriesPanel.add(Box.createVerticalStrut(10));
            }

            if (!hasResults) {
                entriesPanel.add(new JLabel("No entries found."));
            }

            entriesPanel.revalidate();
            entriesPanel.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading entries.");
        }
    }
}
