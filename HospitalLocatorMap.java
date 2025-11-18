/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.maplocator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

public class HospitalLocatorMap extends JFrame {

    private JTextField locationField;
    private JButton findButton;
    private JFXPanel mapPanel;

    public HospitalLocatorMap() {
        setTitle("🏥 Hospital Locator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(173, 216, 230)); // sky blue

        JLabel title = new JLabel("Find Nearby Hospitals", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBounds(230, 10, 320, 40);
        add(title);

        locationField = new JTextField();
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        locationField.setBounds(200, 60, 280, 35);
        locationField.setToolTipText("Enter your city (or leave blank for auto-detect)");
        add(locationField);

        findButton = new JButton("Find Hospitals");
        findButton.setBackground(Color.RED);
        findButton.setForeground(Color.WHITE);
        findButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        findButton.setBounds(500, 60, 160, 35);
        add(findButton);

        // Initialize map area (JavaFX inside Swing)
        mapPanel = new JFXPanel();
        mapPanel.setBounds(50, 120, 680, 400);
        add(mapPanel);

        // Button click action
        findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String location = locationField.getText().trim();
                if (location.isEmpty()) {
                    location = detectLocation();
                }
                if (location == null || location.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Could not detect your location!");
                } else {
                    loadMap(location);
                }
            }
        });

        // Load default map at start
        Platform.runLater(() -> loadMap("Bangladesh"));

        setVisible(true);
    }

    private String detectLocation() {
        try {
            URL url = new URL("https://ipinfo.io/json");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String json = response.toString();
            String city = extractJsonValue(json, "city");
            String country = extractJsonValue(json, "country");
            return city + " " + country;

        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private void loadMap(String location) {
        Platform.runLater(() -> {
            try {
                String query = URLEncoder.encode("hospitals near " + location, "UTF-8");
                String mapURL = "https://www.google.com/maps/search/" + query;
                WebView webView = new WebView();
                webView.getEngine().load(mapURL);
                Scene scene = new Scene(webView);
                mapPanel.setScene(scene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalLocatorMap());
    }
}
