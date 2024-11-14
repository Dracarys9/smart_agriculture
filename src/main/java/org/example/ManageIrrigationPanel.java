package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageIrrigationPanel extends JFrame {
    private JTable irrigationTable;
    private JTable landTable;
    private DefaultTableModel irrigationTableModel;
    private DefaultTableModel landTableModel;

    public ManageIrrigationPanel() {
        setTitle("Manage Irrigation");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this panel

        // Create main panel with two columns: Form + Land Table on the right side
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Form panel for irrigation entry
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.WEST);

        // Land table on the right side
        landTable = new JTable();
        loadLandData();
        JScrollPane landScrollPane = new JScrollPane(landTable);
        mainPanel.add(landScrollPane, BorderLayout.CENTER);

        // Add the main panel to the JFrame
        add(mainPanel, BorderLayout.NORTH);

        // Irrigation table displayed below the form panel
        irrigationTable = new JTable();
        loadIrrigationData();
        JScrollPane irrigationScrollPane = new JScrollPane(irrigationTable);
        add(irrigationScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Creates the form panel for adding or updating irrigation
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add space between fields

        // Land ID field
        JLabel landLabel = new JLabel("Land ID:");
        JTextField landField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(landLabel, gbc);
        gbc.gridx = 1;
        panel.add(landField, gbc);

        // Start Time field
        JLabel startTimeLabel = new JLabel("Start Time (HH:mm):");
        JTextField startTimeField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(startTimeLabel, gbc);
        gbc.gridx = 1;
        panel.add(startTimeField, gbc);

        // End Time field
        JLabel endTimeLabel = new JLabel("End Time (HH:mm):");
        JTextField endTimeField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(endTimeLabel, gbc);
        gbc.gridx = 1;
        panel.add(endTimeField, gbc);

        // Buttons for submitting the form (Add, Update, Delete)
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Irrigation");
        JButton updateButton = new JButton("Update Irrigation");
        JButton deleteButton = new JButton("Delete Irrigation");

        // Add action listeners for the buttons
        addButton.addActionListener(e -> addIrrigation(landField.getText(), startTimeField.getText(), endTimeField.getText()));
        updateButton.addActionListener(e -> updateIrrigation(landField.getText(), startTimeField.getText(), endTimeField.getText()));
        deleteButton.addActionListener(e -> deleteIrrigation());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // Loads land data from the database to the land table
    private void loadLandData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM lands";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            landTableModel = buildTableModel(resultSet);
            landTable.setModel(landTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Loads irrigation schedule data from the database into the table
    private void loadIrrigationData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
                    SELECT irrigation.irrigation_id, lands.sector_name, irrigation.start_time, irrigation.end_time
                    FROM irrigation
                    JOIN lands ON irrigation.land_id = lands.land_id
                    """;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            irrigationTableModel = buildTableModel(resultSet);
            irrigationTable.setModel(irrigationTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adds a new irrigation schedule
    private void addIrrigation(String landIdText, String startTime, String endTime) {
        if (landIdText.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int landId = Integer.parseInt(landIdText);

            // Check if the entered landId exists in the lands table
            String checkQuery = "SELECT 1 FROM lands WHERE land_id = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setInt(1, landId);
                ResultSet resultSet = checkStatement.executeQuery();
                if (!resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "Land ID does not exist.");
                    return;
                }
            }

            String query = "INSERT INTO irrigation (land_id, start_time, end_time) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, landId);
                statement.setString(2, startTime);
                statement.setString(3, endTime);
                statement.executeUpdate();
            }

            loadIrrigationData(); // Refresh the irrigation table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Updates an existing irrigation schedule
    private void updateIrrigation(String landIdText, String startTime, String endTime) {
        int selectedRow = irrigationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an irrigation schedule to update.");
            return;
        }

        String irrigationId = irrigationTable.getValueAt(selectedRow, 0).toString();

        if (landIdText.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            int landId = Integer.parseInt(landIdText);

            // Check if the entered landId exists in the lands table
            String checkQuery = "SELECT 1 FROM lands WHERE land_id = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setInt(1, landId);
                ResultSet resultSet = checkStatement.executeQuery();
                if (!resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "Land ID does not exist.");
                    return;
                }
            }

            String query = "UPDATE irrigation SET land_id = ?, start_time = ?, end_time = ? WHERE irrigation_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, landId);
                statement.setString(2, startTime);
                statement.setString(3, endTime);
                statement.setInt(4, Integer.parseInt(irrigationId));
                statement.executeUpdate();
            }

            loadIrrigationData(); // Refresh the irrigation table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Deletes an irrigation schedule
    private void deleteIrrigation() {
        int selectedRow = irrigationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an irrigation schedule to delete.");
            return;
        }

        String irrigationId = irrigationTable.getValueAt(selectedRow, 0).toString();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM irrigation WHERE irrigation_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, Integer.parseInt(irrigationId));
                statement.executeUpdate();
            }

            loadIrrigationData(); // Refresh the irrigation table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Builds a TableModel to display ResultSet data in JTable
    private static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Column names
        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Data rows
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        return new DefaultTableModel(data, columnNames);
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManageIrrigationPanel::new);
    }
}
