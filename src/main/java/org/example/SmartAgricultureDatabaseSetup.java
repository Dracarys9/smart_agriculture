package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class SmartAgricultureDatabaseSetup {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/smart_agriculture";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    public static void main(String[] args) {
        try {
            // Explicitly load MySQL JDBC driver (if needed)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // Step 1: Create Land Table first
                String createLandsTableSQL = """
                        CREATE TABLE IF NOT EXISTS lands (
                            land_id INT AUTO_INCREMENT PRIMARY KEY,
                            sector_name VARCHAR(50) NOT NULL,
                            soil_type VARCHAR(50) NOT NULL,
                            area DECIMAL(10, 2) NOT NULL
                        );
                        """;
                stmt.executeUpdate(createLandsTableSQL);
                System.out.println("Table 'lands' created or already exists.");

                // Step 2: Create Crops Table
                String createCropsTableSQL = """
                        CREATE TABLE IF NOT EXISTS crops (
                                crop_id INT AUTO_INCREMENT PRIMARY KEY,
                                crop_name VARCHAR(50) NOT NULL,
                                ideal_temperature INT NOT NULL,
                                ideal_moisture INT NOT NULL,
                                ideal_ph DECIMAL(3, 1) NOT NULL,
                                land_id INT NOT NULL,
                                FOREIGN KEY (land_id) REFERENCES lands(land_id) ON DELETE CASCADE
                            );
                        """;
                stmt.executeUpdate(createCropsTableSQL);
                System.out.println("Table 'crops' created or already exists.");

                // Step 3: Create Sensors Table
                String createSensorsTableSQL = """
                        CREATE TABLE IF NOT EXISTS sensors (
                              sensor_id INT AUTO_INCREMENT PRIMARY KEY,
                              land_id INT,
                              temperature DECIMAL(5, 2),
                              soil_moisture DECIMAL(5, 2),
                              ph DECIMAL(3, 1),
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (land_id) REFERENCES lands(land_id) ON DELETE CASCADE
                          );
                        """;
                stmt.executeUpdate(createSensorsTableSQL);
                System.out.println("Table 'sensors' created or already exists.");

                // Step 4: Create Irrigation Table
                String createIrrigationTableSQL = """
                        CREATE TABLE IF NOT EXISTS irrigation (
                            irrigation_id INT AUTO_INCREMENT PRIMARY KEY,
                            land_id INT NOT NULL,
                            start_time TIME NOT NULL,
                            end_time TIME NOT NULL,
                            FOREIGN KEY (land_id) REFERENCES lands(land_id)
                        );
                        """;
                stmt.executeUpdate(createIrrigationTableSQL);
                System.out.println("Table 'irrigation' created or already exists.");

                System.out.println("All tables created successfully in the 'smart_agriculture' database.");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("An error occurred while setting up the database and tables.");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("MySQL JDBC driver not found.");
        }
    }
}
