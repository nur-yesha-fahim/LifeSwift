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
    private static final int REFRESH_INTERVAL_MS = 8000;

    private JTextArea displayArea;
    private final boolean isNGOView;

    public NGOHelpRequestApp(boolean isNGOView) {
        this.isNGOView = isNGOView;

        setTitle(isNGOView ? "NGO Control Room - Incoming Requests" : "Request Medical Help");
        setSize(isNGOView ? 860 : 760, 860);
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

    // ───────────────────────────────────────────────
    //              PATIENT REQUEST FORM
    // ───────────────────────────────────────────────
    private void createPatientView() {
        int leftMargin = 80;
        int labelWidth = 220;     // ← wider labels so text is fully visible
        int fieldX = 320;         // ← moved fields to the right
        int fieldWidth = 380;

        add(createTitle("Request Medical / Emergency Support", 35));

        // 1. Name
        add(createLabel("Your Full Name *", leftMargin, 110));
        JTextField nameField = new JTextField();
        nameField.setBounds(fieldX, 110, fieldWidth, 42);
        add(nameField);

        // 2. Location
        add(createLabel("Your Area / Location *", leftMargin, 175));
        JTextField locationField = createPlaceholderField("Example: Tongi, Board Bazar, Gazipur Sadar, Uttara...");
        locationField.setBounds(fieldX, 175, fieldWidth, 42);
        add(locationField);

        // 3. People needed - NOW FULLY VISIBLE
        add(createLabel("How many people / team members needed?", leftMargin, 240));
        JTextField peopleField = new JTextField("2");
        peopleField.setBounds(fieldX, 240, 140, 42);
        add(peopleField);

        // 4. Main request - NOW FULLY VISIBLE
        add(createLabel("What kind of help / support do you need? *", leftMargin, 305));
        JTextArea requestArea = new JTextArea();
        requestArea.setLineWrap(true);
        requestArea.setWrapStyleWord(true);
        add(new JScrollPane(requestArea) {{
            setBounds(fieldX, 305, fieldWidth, 160);
        }});

        // 5. More details
        add(createLabel("More details / patient condition:", leftMargin, 490));
        JTextArea detailsArea = new JTextArea();
        detailsArea.setLineWrap(true);
        add(new JScrollPane(detailsArea) {{
            setBounds(fieldX, 490, fieldWidth, 130);
        }});

        // Send button
        JButton sendBtn = new JButton("SEND HELP REQUEST NOW");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sendBtn.setBackground(new Color(200, 40, 60));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setBounds(fieldX, 650, fieldWidth, 70);
        add(sendBtn);

        sendBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String location = getCleanText(locationField);
            String people = peopleField.getText().trim();
            String mainRequest = requestArea.getText().trim();

            if (name.isEmpty()) {
                showWarning("Please enter your name");
                return;
            }
            if (location.isEmpty() || location.contains("Example:")) {
                showWarning("Please enter your actual location");
                return;
            }
            if (mainRequest.isEmpty()) {
                showWarning("Please describe what kind of help you need");
                return;
            }

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String safeMain   = mainRequest.replace("\n", " ").replace("|", "/").trim();
            String safeDetail = detailsArea.getText().trim().replace("\n", " ").replace("|", "/");

            String entry = String.format(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "[%s]  STATUS: NEW\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Name:      %s\n" +
                "Location:  %s\n" +
                "People:    %s\n" +
                "Request:   %s\n" +
                "Details:   %s\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n",
                timestamp, name, location,
                people.isEmpty() ? "—" : people,
                safeMain.length() > 180 ? safeMain.substring(0, 177) + "..." : safeMain,
                safeDetail.isEmpty() ? "—" : safeDetail
            );

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(REQUEST_FILE, true))) {
                writer.write(entry);
                writer.newLine();
                JOptionPane.showMessageDialog(this, "Request sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                nameField.setText("");
                locationField.setText("");
                locationField.setForeground(new Color(140,140,140));
                peopleField.setText("1");
                requestArea.setText("");
                detailsArea.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Cannot save request\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ───────────────────────────────────────────────
    //              NGO DASHBOARD VIEW (unchanged)
    // ───────────────────────────────────────────────
    private void createNGOView() {
        add(createTitle("Incoming Requests – Live View", 30));

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        displayArea.setBackground(new Color(255, 253, 240));
        displayArea.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(displayArea);
        scroll.setBounds(30, 100, 800, 700);
        add(scroll);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadRequests();
            }
        }, 0, REFRESH_INTERVAL_MS);
    }

    private void loadRequests() {
        StringBuilder content = new StringBuilder();
        content.append("Last updated: ")
               .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
               .append("\n\n");

        try (BufferedReader br = new BufferedReader(new FileReader(REQUEST_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException ignored) {
            content.append("No requests received yet...\n");
        } catch (IOException e) {
            content.append("Error reading file\n");
        }

        SwingUtilities.invokeLater(() -> {
            displayArea.setText(content.toString());
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        });
    }

    // ───────────────────────────────────────────────
    //                   HELPERS
    // ───────────────────────────────────────────────
    private JLabel createTitle(String text, int y) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl.setBounds(40, y, 680, 60);
        return lbl;
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));   // ← slightly larger & clearer
        lbl.setBounds(x, y, 240, 42);
        return lbl;
    }

    private JTextField createPlaceholderField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setForeground(new Color(140, 140, 140));

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setForeground(new Color(140, 140, 140));
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private String getCleanText(JTextField field) {
        String text = field.getText().trim();
        return text.contains("Example:") ? "" : text;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Required Field", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(null,
                "Are you NGO Worker / Control Room?\n\nYes = View requests\nNo = Make request",
                "Select Role", JOptionPane.YES_NO_OPTION);
            new NGOHelpRequestApp(choice == JOptionPane.YES_OPTION);
        });
    }
}
