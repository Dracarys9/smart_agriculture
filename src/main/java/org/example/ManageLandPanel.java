package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageLandPanel extends JFrame {
    private JTable landTable;
    private DefaultTableModel tableModel;
    private JTextField sectorNameField, soilTypeField, areaField;

    public ManageLandPanel() {
        setTitle("Manage Land");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this panel

        // Set up the main layout
        setLayout(new BorderLayout());

        // Form Panel: For entering land details
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Fields for adding/updating land
        formPanel.add(new JLabel("Sector Name:"));
        sectorNameField = new JTextField();
        formPanel.add(sectorNameField);

        formPanel.add(new JLabel("Soil Type:"));
        soilTypeField = new JTextField();
        formPanel.add(soilTypeField);

        formPanel.add(new JLabel("Area (in sq meters):"));
        areaField = new JTextField();
        formPanel.add(areaField);

        // Buttons for Add, Update, Delete operations
        JPanel buttonPanel = new JPanel();
        JButton addLandBtn = new JButton("Add Land");
        JButton updateLandBtn = new JButton("Update Land");
        JButton deleteLandBtn = new JButton("Delete Land");

        addLandBtn.addActionListener(e -> addLand());
        updateLandBtn.addActionListener(e -> updateLand());
        deleteLandBtn.addActionListener(e -> deleteLand());

        buttonPanel.add(addLandBtn);
        buttonPanel.add(updateLandBtn);
        buttonPanel.add(deleteLandBtn);

        // Add the form and buttons at the top of the window
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        // Initialize table to show land data
        landTable = new JTable();
        loadLandData();

        // Add table with scroll pane at the bottom of the window
        JScrollPane tableScrollPane = new JScrollPane(landTable);
        add(tableScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Loads land data from the database into the table
    private void loadLandData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM lands"; // Use the correct table name `lands`
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel = buildTableModel(resultSet);
            landTable.setModel(tableModel);

            // Adjust column widths
            adjustColumnWidths(landTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adjust the column widths to make sure all columns fit the content
    private void adjustColumnWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 100; // Default width
            for (int row = 0; row < table.getRowCount(); row++) {
                int cellWidth = table.getCellRenderer(row, column).getTableCellRendererComponent(table, table.getValueAt(row, column), false, false, row, column).getPreferredSize().width;
                width = Math.max(width, cellWidth);
            }
            columnModel.getColumn(column).setPreferredWidth(width + 10); // Add a little padding
        }
    }

    // Adds a new land sector
    private void addLand() {
        String sectorName = sectorNameField.getText();
        String soilType = soilTypeField.getText();
        String areaText = areaField.getText();

        if (sectorName.isEmpty() || soilType.isEmpty() || areaText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            double area = Double.parseDouble(areaText);
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO lands (sector_name, soil_type, area) VALUES (?, ?, ?)"; // Correct table name `lands`
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, sectorName);
                statement.setString(2, soilType);
                statement.setDouble(3, area);
                statement.executeUpdate();
                loadLandData(); // Refresh table
                clearForm();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid area.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Updates an existing land sector
    private void updateLand() {
        int selectedRow = landTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a land sector to update.");
            return;
        }

        String landId = landTable.getValueAt(selectedRow, 0).toString();
        String sectorName = sectorNameField.getText();
        String soilType = soilTypeField.getText();
        String areaText = areaField.getText();

        if (sectorName.isEmpty() || soilType.isEmpty() || areaText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            double area = Double.parseDouble(areaText);
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "UPDATE lands SET sector_name = ?, soil_type = ?, area = ? WHERE land_id = ?"; // Correct table name `lands`
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, sectorName);
                statement.setString(2, soilType);
                statement.setDouble(3, area);
                statement.setInt(4, Integer.parseInt(landId));
                statement.executeUpdate();
                loadLandData(); // Refresh table
                clearForm();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid area.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Deletes a selected land sector
    private void deleteLand() {
        int selectedRow = landTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a land sector to delete.");
            return;
        }

        String landId = landTable.getValueAt(selectedRow, 0).toString();

        try {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM lands WHERE land_id = ?"; // Correct table name `lands`
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, Integer.parseInt(landId));
                statement.executeUpdate();
                loadLandData(); // Refresh table
                clearForm();
            }
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

    // Clear the input fields after adding/updating data
    private void clearForm() {
        sectorNameField.setText("");
        soilTypeField.setText("");
        areaField.setText("");
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManageLandPanel::new);
    }
}
