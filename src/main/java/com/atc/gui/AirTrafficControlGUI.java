package com.atc.gui;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AirTrafficControlGUI extends JFrame {
    private DatabaseManager dbManager;
    private DefaultTableModel aircraftTableModel;
    private DefaultTableModel runwayTableModel;
    private JTextArea logArea;
    private JLabel statsLabel;

    public AirTrafficControlGUI() {
        this.dbManager = DatabaseManager.getInstance();
        
        setTitle("Air Traffic Control System - Database Driven");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        startUpdateTimer();
    }

    private void initComponents() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("AIR TRAFFIC CONTROL SYSTEM - DATABASE DRIVEN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);
        
        statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(statsLabel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addAircraftBtn = new JButton("Generate Aircraft");
        addAircraftBtn.addActionListener(e -> generateAircraft());
        
        JButton addRunwayBtn = new JButton("Add Runway");
        addRunwayBtn.addActionListener(e -> addRunway());
        
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        
        controlPanel.add(addAircraftBtn);
        controlPanel.add(addRunwayBtn);
        controlPanel.add(clearLogBtn);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(createAircraftPanel());
        panel.add(createRunwayPanel());
        panel.add(createLogPanel());
        
        return panel;
    }

    private JPanel createAircraftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Active Aircraft (Database)"));
        
        String[] columns = {"Callsign", "Type", "Fuel", "Distance", "Status", "Emergency"};
        aircraftTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(aircraftTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createRunwayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Runways (Database)"));
        
        String[] columns = {"Runway ID", "Status", "Aircraft"};
        runwayTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(runwayTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Emergency Scenarios"));
        
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton fireBtn = new JButton("FIRE Emergency");
        fireBtn.setBackground(new Color(255, 50, 50));
        fireBtn.addActionListener(e -> triggerEmergency("FIRE", 1));
        
        JButton medicalBtn = new JButton("MEDICAL Emergency");
        medicalBtn.setBackground(new Color(100, 150, 255));
        medicalBtn.addActionListener(e -> triggerEmergency("MEDICAL", 5));
        
        JButton securityBtn = new JButton("SECURITY Threat");
        securityBtn.setBackground(new Color(255, 100, 150));
        securityBtn.addActionListener(e -> triggerEmergency("SECURITY", 8));
        
        JButton fuelCriticalBtn = new JButton("FUEL CRITICAL");
        fuelCriticalBtn.setBackground(new Color(255, 100, 100));
        fuelCriticalBtn.addActionListener(e -> triggerEmergency("FUEL_CRITICAL", 10));
        
        JButton weatherBtn = new JButton("WEATHER STORM");
        weatherBtn.setBackground(new Color(150, 150, 200));
        weatherBtn.addActionListener(e -> triggerEmergency("WEATHER_STORM", 30));
        
        JButton fuelLowBtn = new JButton("FUEL LOW");
        fuelLowBtn.setBackground(new Color(255, 200, 100));
        fuelLowBtn.addActionListener(e -> triggerEmergency("FUEL_LOW", 50));
        
        buttonPanel.add(fireBtn);
        buttonPanel.add(medicalBtn);
        buttonPanel.add(securityBtn);
        buttonPanel.add(fuelCriticalBtn);
        buttonPanel.add(weatherBtn);
        buttonPanel.add(fuelLowBtn);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void triggerEmergency(String emergencyType, int priority) {
        List<Document> aircraft = dbManager.getAllActiveAircraft();
        if (aircraft.isEmpty()) {
            log("No aircraft available for emergency scenario");
            return;
        }
        
        Document selectedAircraft = aircraft.stream()
            .filter(ac -> "NONE".equals(ac.getString("emergency")))
            .filter(ac -> !"LANDED".equals(ac.getString("status")))
            .findFirst()
            .orElse(null);
        
        if (selectedAircraft == null) {
            log("No suitable aircraft for emergency scenario");
            return;
        }
        
        String callsign = selectedAircraft.getString("callsign");
        Document updates = new Document("emergency", emergencyType)
            .append("priority", priority)
            .append("emergencyTimestamp", System.currentTimeMillis());
        
        if (emergencyType.startsWith("FUEL")) {
            double currentFuel = selectedAircraft.getDouble("fuel");
            double newFuel = emergencyType.equals("FUEL_CRITICAL") ? 
                Math.min(currentFuel, 800.0) : Math.min(currentFuel, 2000.0);
            updates.append("fuel", newFuel);
            updates.append("status", "HOLDING");
        }
        
        dbManager.updateActiveAircraft(callsign, updates);
        
        String message = switch(emergencyType) {
            case "FIRE" -> "MAYDAY - " + callsign + " FIRE emergency, immediate landing required (Priority: " + priority + ")";
            case "MEDICAL" -> "PAN-PAN - " + callsign + " medical emergency (Priority: " + priority + ")";
            case "SECURITY" -> "SECURITY ALERT - " + callsign + " security threat (Priority: " + priority + ")";
            case "FUEL_CRITICAL" -> "MAYDAY - " + callsign + " FUEL CRITICAL, immediate landing required (Priority: " + priority + ")";
            case "FUEL_LOW" -> "FUEL LOW - " + callsign + " requesting priority landing (Priority: " + priority + ")";
            case "WEATHER_STORM" -> "WEATHER - " + callsign + " unable to continue approach (Priority: " + priority + ")";
            default -> callsign + " emergency declared";
        };
        
        dbManager.saveEmergencyEvent(callsign, message);
        log(message);
        updateDisplay();
    }

    private void generateAircraft() {
        String callsign = dbManager.generateAndInsertRealisticFlight();
        if (callsign != null) {
            log("Generated aircraft: " + callsign);
        } else {
            log("Failed to generate aircraft - check database");
        }
    }
    
    private void addRunway() {
        List<Document> runways = dbManager.getAllRunways();
        
        Set<String> existingIds = new HashSet<>();
        for (Document r : runways) {
            existingIds.add(r.getString("runwayId"));
        }
        
        String runwayId = null;
        for (int num = 1; num <= 36; num++) {
            for (String side : new String[]{"L", "R", "C"}) {
                String candidate = "RWY-" + String.format("%02d", num) + side;
                if (!existingIds.contains(candidate)) {
                    runwayId = candidate;
                    break;
                }
            }
            if (runwayId != null) break;
        }
        
        if (runwayId == null) {
            log("Cannot add more runways - all IDs used");
            return;
        }
        
        dbManager.insertRunway(runwayId, "AVAILABLE", null);
        log("Added runway: " + runwayId);
        updateDisplay();
    }

    public void updateDisplay() {
        updateAircraftTable();
        updateRunwayTable();
        updateStats();
    }

    private void updateAircraftTable() {
        aircraftTableModel.setRowCount(0);
        List<Document> aircraft = dbManager.getAllActiveAircraft();
        for (Document ac : aircraft) {
            aircraftTableModel.addRow(new Object[]{
                ac.getString("callsign"),
                ac.getString("aircraftType"),
                String.format("%.0f", ac.getDouble("fuel")),
                String.format("%.1f", ac.getDouble("distance")),
                ac.getString("status"),
                ac.getString("emergency")
            });
        }
    }

    private void updateRunwayTable() {
        runwayTableModel.setRowCount(0);
        List<Document> runways = dbManager.getAllRunways();
        for (Document runway : runways) {
            runwayTableModel.addRow(new Object[]{
                runway.getString("runwayId"),
                runway.getString("status"),
                runway.getString("currentAircraft") != null ? runway.getString("currentAircraft") : "None"
            });
        }
    }

    private void updateStats() {
        List<Document> aircraft = dbManager.getAllActiveAircraft();
        List<Document> runways = dbManager.getAllRunways();
        
        long emergencies = aircraft.stream().filter(ac -> !"NONE".equals(ac.getString("emergency"))).count();
        long landed = aircraft.stream().filter(ac -> "LANDED".equals(ac.getString("status"))).count();
        long occupiedRunways = runways.stream().filter(r -> "OCCUPIED".equals(r.getString("status"))).count();
        
        statsLabel.setText(String.format(
            "Aircraft: %d | Emergencies: %d | Landed: %d | Occupied Runways: %d/%d",
            aircraft.size(), emergencies, landed, occupiedRunways, runways.size()
        ));
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%tT] %s\n", System.currentTimeMillis(), message));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startUpdateTimer() {
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> updateDisplay());
        timer.start();
    }
}
