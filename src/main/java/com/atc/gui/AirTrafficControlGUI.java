package com.atc.gui;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.controllers.EmergencyController;
import com.atc.utils.AircraftGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AirTrafficControlGUI extends JFrame {
    private final List<Aircraft> activeAircraft;
    private final List<Runway> runways;
    private final EmergencyController emergencyController;
    
    private DefaultTableModel aircraftTableModel;
    private DefaultTableModel runwayTableModel;
    private DefaultTableModel emergencyTableModel;
    private JTextArea logArea;
    private JLabel statsLabel;

    public AirTrafficControlGUI(List<Aircraft> aircraft, List<Runway> runways, EmergencyController controller) {
        this.activeAircraft = aircraft;
        this.runways = runways;
        this.emergencyController = controller;
        
        setTitle("Air Traffic Control System");
        setSize(1400, 900);
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
        
        JLabel title = new JLabel("AIR TRAFFIC CONTROL SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);
        
        statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(statsLabel, BorderLayout.CENTER);
        
        panel.add(createControlPanel(), BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton addAircraftBtn = new JButton("Add Aircraft");
        addAircraftBtn.addActionListener(e -> addRandomAircraft());
        
        JButton fuelLowBtn = new JButton("Fuel Low Emergency");
        fuelLowBtn.addActionListener(e -> createEmergency(Aircraft.EmergencyType.FUEL_LOW));
        
        JButton fireBtn = new JButton("Fire Emergency");
        fireBtn.addActionListener(e -> createEmergency(Aircraft.EmergencyType.FIRE));
        
        JButton medicalBtn = new JButton("Medical Emergency");
        medicalBtn.addActionListener(e -> createEmergency(Aircraft.EmergencyType.MEDICAL));
        
        JButton securityBtn = new JButton("Security Threat");
        securityBtn.addActionListener(e -> createEmergency(Aircraft.EmergencyType.SECURITY));
        
        JButton weatherBtn = new JButton("Weather Storm");
        weatherBtn.addActionListener(e -> createEmergency(Aircraft.EmergencyType.WEATHER_STORM));
        
        panel.add(addAircraftBtn);
        panel.add(fuelLowBtn);
        panel.add(fireBtn);
        panel.add(medicalBtn);
        panel.add(securityBtn);
        panel.add(weatherBtn);
        
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(createAircraftPanel());
        panel.add(createRunwayPanel());
        panel.add(createEmergencyPanel());
        panel.add(createLogPanel());
        
        return panel;
    }

    private JPanel createAircraftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Active Aircraft"));
        
        String[] columns = {"Callsign", "Fuel", "Speed", "Distance", "Status", "Emergency", "Runway"};
        aircraftTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(aircraftTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createRunwayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Runway Status"));
        
        String[] columns = {"Runway ID", "Status", "Aircraft", "Weather"};
        runwayTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(runwayTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Emergency Alerts"));
        
        String[] columns = {"Callsign", "Type", "Priority", "Action"};
        emergencyTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(emergencyTableModel);
        table.setSelectionBackground(Color.RED);
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
        
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogBtn);
        
        return panel;
    }

    private void addRandomAircraft() {
        Aircraft aircraft = AircraftGenerator.generateRandomAircraft();
        activeAircraft.add(aircraft);
        log("New aircraft added: " + aircraft.getCallsign());
    }

    private void createEmergency(Aircraft.EmergencyType type) {
        Aircraft aircraft = AircraftGenerator.generateEmergencyAircraft(type);
        activeAircraft.add(aircraft);
        emergencyController.declareEmergency(aircraft, type);
        log("EMERGENCY: " + aircraft.getCallsign() + " - " + type);
    }

    private void updateDisplay() {
        updateAircraftTable();
        updateRunwayTable();
        updateEmergencyTable();
        updateStats();
    }

    private void updateAircraftTable() {
        aircraftTableModel.setRowCount(0);
        for (Aircraft aircraft : activeAircraft) {
            aircraftTableModel.addRow(new Object[]{
                aircraft.getCallsign(),
                String.format("%.1f", aircraft.getFuelLevel()),
                aircraft.getSpeed(),
                String.format("%.1f nm", aircraft.getDistanceToAirport()),
                aircraft.getStatus(),
                aircraft.getEmergencyType(),
                aircraft.getAssignedRunway() != null ? aircraft.getAssignedRunway() : "N/A"
            });
        }
    }

    private void updateRunwayTable() {
        runwayTableModel.setRowCount(0);
        for (Runway runway : runways) {
            runwayTableModel.addRow(new Object[]{
                runway.getRunwayId(),
                runway.getStatus(),
                runway.getCurrentAircraft() != null ? runway.getCurrentAircraft().getCallsign() : "None",
                runway.isWeatherAffected() ? "AFFECTED" : "Clear"
            });
        }
    }

    private void updateEmergencyTable() {
        emergencyTableModel.setRowCount(0);
        for (Aircraft aircraft : activeAircraft) {
            if (aircraft.isEmergency() && aircraft.getStatus() != Aircraft.Status.LANDED) {
                String action = getEmergencyAction(aircraft.getEmergencyType());
                emergencyTableModel.addRow(new Object[]{
                    aircraft.getCallsign(),
                    aircraft.getEmergencyType(),
                    aircraft.getPriority(),
                    action
                });
            }
        }
    }

    private String getEmergencyAction(Aircraft.EmergencyType type) {
        return switch(type) {
            case FIRE -> "Fire services positioned";
            case MEDICAL -> "Ambulance dispatched";
            case SECURITY -> "Law enforcement alerted";
            case FUEL_CRITICAL -> "Direct routing cleared";
            case FUEL_LOW -> "Priority vectors assigned";
            case WEATHER_STORM -> "Diversion in progress";
            default -> "Monitoring";
        };
    }

    private void updateStats() {
        int total = activeAircraft.size();
        long emergencies = activeAircraft.stream().filter(Aircraft::isEmergency).count();
        long landed = activeAircraft.stream().filter(a -> a.getStatus() == Aircraft.Status.LANDED).count();
        long activeRunways = runways.stream().filter(r -> !r.isOpen()).count();
        
        statsLabel.setText(String.format(
            "Total Aircraft: %d | Emergencies: %d | Landed: %d | Active Runways: %d/%d",
            total, emergencies, landed, activeRunways, runways.size()
        ));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%tT] %s\n", System.currentTimeMillis(), message));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startUpdateTimer() {
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> updateDisplay());
        timer.start();
    }
}
