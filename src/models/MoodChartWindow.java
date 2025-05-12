package models;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.Locale;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class MoodChartWindow extends JFrame {

    private JComboBox<String> monthSelector;
    private int userId;

    public MoodChartWindow(int userId) {
        this.userId = userId;
        setTitle("Mood Frequency by Month");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        monthSelector = new JComboBox<>();
        populateMonths();
        monthSelector.addActionListener(e -> refreshChart());

        add(monthSelector, BorderLayout.NORTH);
        refreshChart();
        setVisible(true);
    }

    private void populateMonths() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT DATE_FORMAT(date_logged, '%Y-%m') AS month FROM mood_entries WHERE user_id = ? ORDER BY month DESC"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Set<String> months = new LinkedHashSet<>();
            while (rs.next()) {
                months.add(rs.getString("month"));
            }

            // Add current month even if no entry
            String currentMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            months.add(currentMonth);

            for (String m : months) {
                monthSelector.addItem(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshChart() {
        String selectedMonth = (String) monthSelector.getSelectedItem();
        if (selectedMonth == null) return;

        // Initialize mood counts
        Map<Integer, Integer> moodCounts = new TreeMap<>();
        for (int i = 1; i <= 5; i++) {
            moodCounts.put(i, 0);
        }

        boolean hasData = false;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT mood_level FROM mood_entries WHERE user_id = ? AND DATE_FORMAT(date_logged, '%Y-%m') = ?"
            );
            stmt.setInt(1, userId);
            stmt.setString(2, selectedMonth);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int mood = rs.getInt("mood_level");
                if (moodCounts.containsKey(mood)) {
                    moodCounts.put(mood, moodCounts.get(mood) + 1);
                    hasData = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int mood = 1; mood <= 5; mood++) {
            dataset.addValue(moodCounts.get(mood), "Mood Count", String.valueOf(mood));
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Mood Frequency - " + formatMonthLabel(selectedMonth),
                "Mood Level (1-5)",
                "Number of Times",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        getContentPane().removeAll();
        add(monthSelector, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        JLabel legendLabel = new JLabel("Mood Legend: 1 😢, 2 😐, 3 😊, 4 😎, 5 😍", SwingConstants.CENTER);
        legendLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(legendLabel, BorderLayout.SOUTH);

        revalidate();

        if (!hasData) {
            JOptionPane.showMessageDialog(this,
                    "No mood entries for " + formatMonthLabel(selectedMonth) + ".\nTry checking in this month to see your progress!",
                    "No Data", JOptionPane.INFORMATION_MESSAGE);
        }

        // Calculate total mood value and total count
        int totalMoodValue = 0;
        int totalMoodCount = 0;

        for (Map.Entry<Integer, Integer> entry : moodCounts.entrySet()) {
            int moodValue = entry.getKey();
            int count = entry.getValue();

            totalMoodValue += moodValue * count;  // Add weighted mood value
            totalMoodCount += count;  // Add total count of moods
        }

        // Calculate the average based on total mood value and total count
        double monthlyAvg = totalMoodCount > 0 ? (double) totalMoodValue / totalMoodCount : 0;

        // Show reminder if average mood is low (for average <= 2)
        if (monthlyAvg <= 2) {
            JOptionPane.showMessageDialog(this,
                    "Your average mood this month is quite low 😞\nMaybe take some time to recharge and do something you love 💛",
                    "Friendly Reminder",
                    JOptionPane.WARNING_MESSAGE);
        }

    }

    private String formatMonthLabel(String monthStr) {
        String[] parts = monthStr.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Month m = Month.of(month);
        return m.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
    }
}
