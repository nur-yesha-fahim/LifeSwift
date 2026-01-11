package com.mycompany.maplocator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class NGOHelpRequestApp extends JFrame {

    private static final String REQUEST_FILE = "medical_requests.txt";
    private static final int REFRESH_INTERVAL_SECONDS = 8; // Refresh every 8 seconds

    private JTextArea displayArea;
    private boolean isNGOView;

    public NGOHelpRequestApp(boolean isNGOView) {
        this.isNGOView = isNGOView;

        setTitle(isNGOView ? "NGO Control Room - Incoming Requests" 
                          : "Request Medical Help");
        setSize(isNGOView ? 780 : 680, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 250, 255));
        setLayout(null);

        if (isNGOView) {
            createNGOView();
        } else {
            createPatientView();
        }

        setVisible(true);
    }

    // ───────────────────────────────
    //   PATIENT / HELP SEEKER VIEW
    // ───────────────────────────────
    private void createPatientView() {
        JLabel title = new JLabel("Request Medical Assistance", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBounds(60, 30, 560, 50);
        add(title);

        addLabel("Your Name:", 70, 110);
        JTextField nameField = new JTextField();
        nameField.setBounds(200, 110, 420, 38);
        add(nameField);

        addLabel("Your Area/Location:", 70, 170);
        JTextField locationField = new JTextField("Gazipur / Tongi / Boardbazar / ...");
        locationField.setBounds(200, 170, 420, 38);
        add(locationField);

        addLabel("What help do you need:", 70, 230);
        String[] helpTypes = {
            "Medical Team / Doctor visit",
            "Oxygen Cylinder",
            "Ambulance / Patient transport",
            "First Aid Kit & basic medicines",
            "Nebulizer machine",
            "Glucometer + test strips",
            "Wound dressing materials",
            "Other medical support"
        };
        JComboBox<String> helpCombo = new JComboBox<>(helpTypes);
        helpCombo.setBounds(200, 230, 420, 38);
        add(helpCombo);

        addLabel("How many / Quantity:", 70, 290);
        JTextField quantityField = new JTextField("1");
        quantityField.setBounds(200, 290, 120, 38);
        add(quantityField);

        addLabel("More details / condition:", 70, 340);
        JTextArea detailsArea = new JTextArea();
        detailsArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(detailsArea);
        scroll.setBounds(200, 340, 420, 140);
        add(scroll);

        JButton sendButton = new JButton("SEND HELP REQUEST NOW");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sendButton.setBackground(new Color(200, 50, 50));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBounds(200, 510, 420, 65);
        add(sendButton);

        sendButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please write your name", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss"));
            String entry = String.format(
                "[%s]  NAME: %s  |  LOCATION: %s  |  REQUEST: %s  |  QTY: %s  |  DETAILS: %s\n",
                timestamp,
                name,
                locationField.getText().trim(),
                helpCombo.getSelectedItem(),
                quantityField.getText().trim(),
                detailsArea.getText().trim().replace("\n", " ")
            );

            try (PrintWriter writer = new PrintWriter(new FileWriter(REQUEST_FILE, true))) {
                writer.println(entry);
                writer.println("─".repeat(80)); // separator line
                JOptionPane.showMessageDialog(this,
                    "Your request has been sent!\nNGO team will try to respond as soon as possible.",
                    "Request Sent", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields
                nameField.setText("");
                locationField.setText("");
                quantityField.setText("1");
                detailsArea.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving request:\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ───────────────────────────────
    //   NGO WORKER / CONTROL ROOM VIEW
    // ───────────────────────────────
    private void createNGOView() {
        JLabel title = new JLabel("Incoming Help Requests (Live)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBounds(60, 20, 660, 50);
        add(title);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        displayArea.setBackground(new Color(255, 252, 240));
        displayArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(displayArea);
        scroll.setBounds(30, 90, 720, 620);
        add(scroll);

        // Auto-refresh timer
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadRequests();
            }
        }, 0, REFRESH_INTERVAL_SECONDS * 1000);
    }

    private void loadRequests() {
        StringBuilder content = new StringBuilder();
        content.append("Last updated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");

        try (BufferedReader br = new BufferedReader(new FileReader(REQUEST_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            content.append("No requests received yet...\nWaiting for incoming help calls.");
        } catch (IOException e) {
            content.append("Error reading request file.");
        }

        SwingUtilities.invokeLater(() -> {
            displayArea.setText(content.toString());
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        });
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setBounds(x, y, 130, 35);
        add(lbl);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(null,
                "Are you NGO Worker / Control Room?\n\nYes = NGO View (see requests)\nNo = Patient / Help Seeker View",
                "Choose Your Role", JOptionPane.YES_NO_OPTION);

            boolean isNGO = (choice == JOptionPane.YES_OPTION);
            new NGOHelpRequestApp(isNGO);
        });
    }
}
