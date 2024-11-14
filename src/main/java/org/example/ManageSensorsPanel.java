package org.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageSensorsPanel extends JFrame {
    private JTable landTable;
    private JTable cropsTable;
    private JTable sensorsTable;
    private DefaultTableModel landTableModel, cropsTableModel, tableModel;
    private JTextField landIdField, temperatureField, soilMoistureField, phField;

    public ManageSensorsPanel() {
        setTitle("Manage Sensors");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Fullscreen mode
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this panel

        // Initialize components for form input
        landIdField = new JTextField(10);
        temperatureField = new JTextField(10);
        soilMoistureField = new JTextField(10);
        phField = new JTextField(10);

        // Create the form panel (top row with input fields)
        JPanel formPanel = createFormPanel();

        // Create the button panel (button below the form fields)
        JPanel buttonPanel = createButtonPanel();

        // Create the Land Table panel
        JPanel landTablePanel = new JPanel();
        landTable = new JTable();
        loadLandData();
        landTablePanel.add(new JScrollPane(landTable));

        // Create the Crops Table panel
        JPanel cropsTablePanel = new JPanel();
        cropsTable = new JTable();
        loadCropsData();
        cropsTablePanel.add(new JScrollPane(cropsTable));

        // Create the Sensor Data Table panel
        JPanel sensorsTablePanel = new JPanel();
        sensorsTable = new JTable();
        loadSensorsData();
        sensorsTablePanel.add(new JScrollPane(sensorsTable));

        // Set up the layout for the whole window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20)); // Use BorderLayout with spacing
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add margin around the edges

        // Panel for input fields and button
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));  // Vertical layout
        topPanel.add(formPanel);
        topPanel.add(buttonPanel);

        // Panel for tables side by side
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(1, 3, 20, 20)); // GridLayout with 3 columns (Land, Crops, Sensor Tables)
        tablePanel.add(landTablePanel);
        tablePanel.add(cropsTablePanel);
        tablePanel.add(sensorsTablePanel);

        // Add top panel and table panel to the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Add the main panel to the frame
        add(mainPanel);

        setVisible(true);
    }

    // Creates and returns the form panel with input fields
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));  // Horizontal layout with space between components

        // Add Land ID Label and Field
        panel.add(new JLabel("Land ID:"));
        panel.add(landIdField);

        // Add Temperature Label and Field
        panel.add(new JLabel("Temperature:"));
        panel.add(temperatureField);

        // Add Soil Moisture Label and Field
        panel.add(new JLabel("Soil Moisture:"));
        panel.add(soilMoistureField);

        // Add pH Label and Field
        panel.add(new JLabel("pH:"));
        panel.add(phField);

        return panel;
    }

    // Creates and returns the button panel below the form fields
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Center button horizontally
        JButton saveButton = new JButton("Save Sensor Data");
        saveButton.addActionListener(e -> saveSensorData());
        panel.add(saveButton);
        return panel;
    }

    // Loads land data from the database into the table
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

    // Loads crops data from the database into the table
    private void loadCropsData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM crops";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            cropsTableModel = buildTableModel(resultSet);
            cropsTable.setModel(cropsTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Loads sensor data from the database into the table
    private void loadSensorsData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM sensors";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel = buildTableModel(resultSet);
            sensorsTable.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Saves the sensor data from the form into the database
    private void saveSensorData() {
        String landId = landIdField.getText();
        String temperature = temperatureField.getText();
        String soilMoisture = soilMoistureField.getText();
        String ph = phField.getText();

        if (landId.isEmpty() || temperature.isEmpty() || soilMoisture.isEmpty() || ph.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields.");
            return;
        }

        try {
            // Convert values to appropriate types
            int landIdInt = Integer.parseInt(landId);
            double temperatureDouble = Double.parseDouble(temperature);
            double soilMoistureDouble = Double.parseDouble(soilMoisture);
            double phDouble = Double.parseDouble(ph);

            // Insert the sensor data into the database
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO sensors (land_id, temperature, soil_moisture, ph) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, landIdInt);
                statement.setDouble(2, temperatureDouble);
                statement.setDouble(3, soilMoistureDouble);
                statement.setDouble(4, phDouble);
                statement.executeUpdate();

                // Refresh the table with new data
                loadSensorsData();

                // Clear form fields
                landIdField.setText("");
                temperatureField.setText("");
                soilMoistureField.setText("");
                phField.setText("");
                JOptionPane.showMessageDialog(this, "Sensor data saved successfully.");

                // Trigger watering alert check
                WateringAlert wateringAlert = new WateringAlert();
                wateringAlert.checkWateringStatusForSensor(landIdInt);  // Check for specific landId
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save sensor data.");
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

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManageSensorsPanel::new);
    }
}
