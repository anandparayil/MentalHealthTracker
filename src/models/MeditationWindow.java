package models;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.sql.*;

public class MeditationWindow extends JFrame {
    public MeditationWindow() {
        setTitle("Guided Meditations");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Column names
        String[] columnNames = {"Title", "Link"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(30);

        // Populate the table
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT title, file_url FROM meditations");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String url = rs.getString("file_url");
                tableModel.addRow(new Object[]{title, url});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Open link on row click
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String url = table.getValueAt(row, 1).toString();
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to open link.");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }
}
