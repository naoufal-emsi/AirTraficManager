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
import java.util.concurrent.PriorityBlockingQueue;

public class AirTrafficControlGUI extends JFrame {
    private final List<Aircraft> activeAircraft;
    private final List<Runway> runways;
    private final EmergencyController emergencyController;
    private final PriorityBlockingQueue<Aircraft> landingQueue;
    
    private DefaultTableModel aircraftTableModel;
    private DefaultTableModel runwayTableModel;
    private DefaultTableModel queueTableModel;
    private JTable aircraftTable;
    private JTextArea logArea;
    private JTextArea selectedAircraftInfo;
    private JLabel statsLabel;
    private Aircraft selectedAircraft;

    public AirTrafficControlGUI(List<Aircraft> aircraft, List<Runway> runways, EmergencyController controller, PriorityBlockingQueue<Aircraft> queue) {
        this.activeAircraft = aircraft;
        this.runways = runways;
        this.emergencyController = controller;
        this.landingQueue = queue;
        
        setTitle("Air Traffic Control System");
        setSize(1600, 1000);
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
        
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        
        panel.add(addAircraftBtn);
        panel.add(clearLogBtn);
        
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createAircraftPanel(), createSelectedAircraftPanel());
        leftSplit.setResizeWeight(0.5);
        
        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        rightPanel.add(createRunwayPanel());
        rightPanel.add(createQueuePanel());
        rightPanel.add(createLogPanel());
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightPanel);
        mainSplit.setResizeWeight(0.5);
        panel.add(mainSplit, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createAircraftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Active Aircraft (Click to Select)"));
        
        String[] columns = {"Callsign", "Fuel", "Speed", "Distance", "Status", "Emergency", "Priority", "Runway"};
        aircraftTableModel = new DefaultTableModel(columns, 0);
        aircraftTable = new JTable(aircraftTableModel);
        aircraftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        aircraftTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = aircraftTable.getSelectedRow();
                if (row >= 0 && row < activeAircraft.size()) {
                    selectedAircraft = activeAircraft.get(row);
                    updateSelectedAircraftInfo();
                }
            }
        });
        panel.add(new JScrollPane(aircraftTable), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createSelectedAircraftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selected Aircraft Details"));
        
        selectedAircraftInfo = new JTextArea();
        selectedAircraftInfo.setEditable(false);
        selectedAircraftInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(selectedAircraftInfo), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton fuelLowBtn = new JButton("FUEL_LOW");
        fuelLowBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.FUEL_LOW));
        
        JButton fuelCriticalBtn = new JButton("FUEL_CRITICAL");
        fuelCriticalBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.FUEL_CRITICAL));
        
        JButton medicalBtn = new JButton("MEDICAL");
        medicalBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.MEDICAL));
        
        JButton fireBtn = new JButton("FIRE");
        fireBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.FIRE));
        
        JButton securityBtn = new JButton("SECURITY");
        securityBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.SECURITY));
        
        JButton weatherBtn = new JButton("WEATHER_STORM");
        weatherBtn.addActionListener(e -> triggerScenario(Aircraft.EmergencyType.WEATHER_STORM));
        
        buttonPanel.add(fuelLowBtn);
        buttonPanel.add(fuelCriticalBtn);
        buttonPanel.add(medicalBtn);
        buttonPanel.add(fireBtn);
        buttonPanel.add(securityBtn);
        buttonPanel.add(weatherBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Landing Queue (Priority Order)"));
        
        String[] columns = {"Position", "Callsign", "Priority", "Emergency", "ETA"};
        queueTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(queueTableModel);
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

    private void addRandomAircraft() {
        Aircraft aircraft = AircraftGenerator.generateRandomAircraft();
        com.atc.AirTrafficSystem.addAircraft(aircraft);
        log("New aircraft added: " + aircraft.getCallsign());
    }
    
    private void triggerScenario(Aircraft.EmergencyType type) {
        if (selectedAircraft == null) {
            JOptionPane.showMessageDialog(this, "Please select an aircraft first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedAircraft.getStatus() == Aircraft.Status.LANDED) {
            JOptionPane.showMessageDialog(this, "Aircraft has already landed", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        emergencyController.declareEmergency(selectedAircraft, type);
        log("SCENARIO TRIGGERED: " + selectedAircraft.getCallsign() + " - " + type);
    }
    
    private void updateSelectedAircraftInfo() {
        if (selectedAircraft == null) {
            selectedAircraftInfo.setText("No aircraft selected");
            return;
        }
        StringBuilder info = new StringBuilder();
        info.append("Callsign: ").append(selectedAircraft.getCallsign()).append("\n");
        info.append("Fuel Level: ").append(String.format("%.2f", selectedAircraft.getFuelLevel())).append(" units\n");
        info.append("ETA: ").append(selectedAircraft.getEta()).append("\n");
        info.append("Status: ").append(selectedAircraft.getStatus()).append("\n");
        info.append("Priority: ").append(selectedAircraft.getPriority()).append("\n");
        info.append("Assigned Runway: ").append(selectedAircraft.getAssignedRunway() != null ? selectedAircraft.getAssignedRunway() : "N/A").append("\n");
        info.append("Emergency Type: ").append(selectedAircraft.getEmergencyType()).append("\n");
        info.append("Speed: ").append(selectedAircraft.getSpeed()).append(" knots\n");
        info.append("Distance: ").append(String.format("%.2f", selectedAircraft.getDistanceToAirport())).append(" nm\n");
        selectedAircraftInfo.setText(info.toString());
    }

    public void updateDisplay() {
        updateAircraftTable();
        updateRunwayTable();
        updateQueueTable();
        updateStats();
        if (selectedAircraft != null) {
            updateSelectedAircraftInfo();
        }
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
                aircraft.getPriority(),
                aircraft.getAssignedRunway() != null ? aircraft.getAssignedRunway() : "N/A"
            });
        }
    }

    private void updateQueueTable() {
        queueTableModel.setRowCount(0);
        List<Aircraft> queueList = new ArrayList<>(landingQueue);
        queueList.sort(Comparator.comparingInt(Aircraft::getPriority));
        int position = 1;
        for (Aircraft aircraft : queueList) {
            if (aircraft.getStatus() != Aircraft.Status.LANDED && aircraft.getStatus() != Aircraft.Status.LANDING) {
                queueTableModel.addRow(new Object[]{
                    position++,
                    aircraft.getCallsign(),
                    aircraft.getPriority(),
                    aircraft.getEmergencyType(),
                    aircraft.getEta()
                });
            }
        }
    }

    private void updateRunwayTable() {
        runwayTableModel.setRowCount(0);
        for (Runway runway : runways) {
            String availability = runway.isOpen() ? "AVAILABLE" : "OCCUPIED";
            if (runway.isWeatherAffected()) availability = "BLOCKED";
            runwayTableModel.addRow(new Object[]{
                runway.getRunwayId(),
                availability,
                runway.getCurrentAircraft() != null ? runway.getCurrentAircraft().getCallsign() : "None",
                runway.isWeatherAffected() ? "STORM" : "Clear"
            });
        }
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

    public void log(String message) {
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
