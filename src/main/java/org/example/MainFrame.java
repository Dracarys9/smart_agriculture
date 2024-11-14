package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame {
    private JFrame frame;

    public MainFrame() {
        frame = new JFrame("Smart Agriculture System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load and set background image
        JLabel bgImage = new JLabel(new ImageIcon(getClass().getResource("/img.png")));
        bgImage.setLayout(new BorderLayout()); // Allows adding components on top of the image
        frame.setContentPane(bgImage);

        // Title at the top of the frame
        JLabel title = new JLabel("Smart Agriculture System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE); // Ensure the text is visible over the background
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Space around the title
        frame.add(title, BorderLayout.NORTH);

        // Center panel to hold buttons in a simple grid layout
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 20, 20)); // 4 rows, 1 column, with spacing
        centerPanel.setOpaque(false); // Transparent panel so background shows through
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200)); // Padding around buttons

        // Buttons for sections
        JButton manageCropsBtn = createButton("Manage Crops", e -> new ManageCropsPanel());
        JButton manageLandBtn = createButton("Manage Land", e -> new ManageLandPanel());
        JButton manageSensorsBtn = createButton("Manage Sensors", e -> new ManageSensorsPanel());
        JButton manageIrrigationBtn = createButton("Manage Irrigation", e -> new ManageIrrigationPanel());

        // Add buttons to the center panel
        centerPanel.add(manageCropsBtn);
        centerPanel.add(manageLandBtn);
        centerPanel.add(manageSensorsBtn);
        centerPanel.add(manageIrrigationBtn);

        frame.add(centerPanel, BorderLayout.CENTER);

        // Make frame visible
        frame.setVisible(true);
    }

    // Creates a standard button
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 24));
        button.addActionListener(action);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
