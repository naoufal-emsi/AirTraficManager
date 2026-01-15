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
        
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        
        controlPanel.add(addAircraftBtn);
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private void generateAircraft() {
        String callsign = dbManager.generateAndInsertRealisticFlight();
        if (callsign != null) {
            log("Generated aircraft: " + callsign);
        } else {
            log("Failed to generate aircraft - check database");
        }
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
