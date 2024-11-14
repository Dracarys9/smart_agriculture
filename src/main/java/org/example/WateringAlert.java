package org.example;

import javax.swing.*;
import java.sql.*;

public class WateringAlert {
    // Tolerance range for moisture level
    private static final int MOISTURE_TOLERANCE = 10;

    // This method will check a single sensor data and show alerts if watering conditions are out of range
    public void checkWateringStatusForSensor(int landId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Query to get the soil moisture and ideal moisture level for the given land ID
            String query =
                    "SELECT s.soil_moisture, c.ideal_moisture " +
                            "FROM sensors s " +
                            "JOIN crops c ON s.land_id = c.land_id " +
                            "WHERE s.land_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, landId);  // Set the land ID to check
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double moistureLevel = resultSet.getDouble("soil_moisture");
                int idealMoisture = resultSet.getInt("ideal_moisture");

                // Calculate the acceptable moisture range for the crop
                double lowerLimit = idealMoisture - MOISTURE_TOLERANCE;
                double upperLimit = idealMoisture + MOISTURE_TOLERANCE;

                // Determine if the moisture level is outside the acceptable range
                if (moistureLevel < lowerLimit) {
                    JOptionPane.showMessageDialog(null,
                            "Alert: Land ID " + landId + " has low moisture (" + moistureLevel + "%). " +
                                    "Moisture should be between " + lowerLimit + "% and " + upperLimit + "%.",
                            "Watering Alert", JOptionPane.WARNING_MESSAGE);
                } else if (moistureLevel > upperLimit) {
                    JOptionPane.showMessageDialog(null,
                            "Alert: Land ID " + landId + " has high moisture (" + moistureLevel + "%). " +
                                    "Moisture should be between " + lowerLimit + "% and " + upperLimit + "%.",
                            "Watering Alert", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // This will print the full stack trace for debugging
            JOptionPane.showMessageDialog(null, "Error checking watering status. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
