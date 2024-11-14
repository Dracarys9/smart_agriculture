package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageCropsPanel extends JFrame {
    private JTable cropsTable;
    private DefaultTableModel cropsTableModel;
    private JTable landTable;
    private DefaultTableModel landTableModel;

    public ManageCropsPanel() {
        setTitle("Manage Crops");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this panel

        // Create the main panel with two columns: Form + Tables (land and crops)
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Form panel for adding/updating crop information
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.WEST);

        // Panel to hold the tables (land and crops) side by side
        JPanel tablePanel = new JPanel(new BorderLayout()); // Land table on the right, crops table at the bottom

        // Land table displayed on the right
        landTable = new JTable();
        loadLandData();
        JScrollPane landScrollPane = new JScrollPane(landTable);
        tablePanel.add(landScrollPane, BorderLayout.WEST);

        // Crops table displayed at the bottom
        cropsTable = new JTable();
        loadCropsData();
        JScrollPane cropsScrollPane = new JScrollPane(cropsTable);
        tablePanel.add(cropsScrollPane, BorderLayout.SOUTH);

        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Add the main panel to the JFrame
        add(mainPanel);

        setVisible(true);
    }

    // Creates the form panel for adding or updating crops
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add space between fields

        // Crop Name field
        JLabel cropNameLabel = new JLabel("Crop Name:");
        JTextField cropNameField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(cropNameLabel, gbc);
        gbc.gridx = 1;
        panel.add(cropNameField, gbc);

        // Ideal Temperature field
        JLabel temperatureLabel = new JLabel("Ideal Temperature:");
        JTextField temperatureField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(temperatureLabel, gbc);
        gbc.gridx = 1;
        panel.add(temperatureField, gbc);

        // Ideal Moisture field
        JLabel moistureLabel = new JLabel("Ideal Moisture:");
        JTextField moistureField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(moistureLabel, gbc);
        gbc.gridx = 1;
        panel.add(moistureField, gbc);

        // Ideal pH field
        JLabel phLabel = new JLabel("Ideal pH:");
        JTextField phField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(phLabel, gbc);
        gbc.gridx = 1;
        panel.add(phField, gbc);

        // Land ID field
        JLabel landIdLabel = new JLabel("Enter Land ID:");
        JTextField landIdField = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(landIdLabel, gbc);
        gbc.gridx = 1;
        panel.add(landIdField, gbc);

        // Buttons for adding, updating, and deleting crops
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Crop");
        JButton updateButton = new JButton("Update Crop");
        JButton deleteButton = new JButton("Delete Crop");

        // Add action listeners for the buttons
        addButton.addActionListener(e -> addCrop(cropNameField, temperatureField, moistureField, phField, landIdField));
        updateButton.addActionListener(e -> updateCrop(cropNameField, temperatureField, moistureField, phField, landIdField));
        deleteButton.addActionListener(e -> deleteCrop());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // Loads crops data from the database into the crops table
    private void loadCropsData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT crops.crop_id, crops.crop_name, crops.ideal_temperature, crops.ideal_moisture, crops.ideal_ph, crops.land_id " +
                    "FROM crops";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            cropsTableModel = buildTableModel(resultSet);
            cropsTable.setModel(cropsTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Loads land data from the database into the land table
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

    // Adds a new crop to the database
    private void addCrop(JTextField cropNameField, JTextField temperatureField, JTextField moistureField, JTextField phField, JTextField landIdField) {
        String cropName = cropNameField.getText();
        String temperatureStr = temperatureField.getText();
        String moistureStr = moistureField.getText();
        String phStr = phField.getText();
        String landIdStr = landIdField.getText();

        if (landIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a land ID.");
            return;
        }

        try {
            int temperature = Integer.parseInt(temperatureStr);
            int moisture = Integer.parseInt(moistureStr);
            double ph = Double.parseDouble(phStr);
            int landId = Integer.parseInt(landIdStr);

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO crops (crop_name, ideal_temperature, ideal_moisture, ideal_ph, land_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, cropName);
                statement.setInt(2, temperature);
                statement.setInt(3, moisture);
                statement.setDouble(4, ph);
                statement.setInt(5, landId);
                statement.executeUpdate();
                loadCropsData(); // Refresh table
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values for temperature, moisture, pH, and land ID.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Updates selected crop in the database
    private void updateCrop(JTextField cropNameField, JTextField temperatureField, JTextField moistureField, JTextField phField, JTextField landIdField) {
        int selectedRow = cropsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a crop to update.");
            return;
        }

        String cropId = cropsTable.getValueAt(selectedRow, 0).toString();
        String cropName = cropNameField.getText();
        String temperatureStr = temperatureField.getText();
        String moistureStr = moistureField.getText();
        String phStr = phField.getText();
        String landIdStr = landIdField.getText();

        if (landIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a land ID.");
            return;
        }

        try {
            int temperature = Integer.parseInt(temperatureStr);
            int moisture = Integer.parseInt(moistureStr);
            double ph = Double.parseDouble(phStr);
            int landId = Integer.parseInt(landIdStr);

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "UPDATE crops SET crop_name = ?, ideal_temperature = ?, ideal_moisture = ?, ideal_ph = ?, land_id = ? WHERE crop_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, cropName);
                statement.setInt(2, temperature);
                statement.setInt(3, moisture);
                statement.setDouble(4, ph);
                statement.setInt(5, landId);
                statement.setInt(6, Integer.parseInt(cropId));
                statement.executeUpdate();
                loadCropsData(); // Refresh table
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Deletes selected crop from the database
    private void deleteCrop() {
        int selectedRow = cropsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a crop to delete.");
            return;
        }

        String cropId = cropsTable.getValueAt(selectedRow, 0).toString();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM crops WHERE crop_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(cropId));
            statement.executeUpdate();
            loadCropsData(); // Refresh table
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Builds a TableModel for displaying ResultSet data in JTable
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
        SwingUtilities.invokeLater(ManageCropsPanel::new);
    }
}
