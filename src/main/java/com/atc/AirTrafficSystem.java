package com.atc;

import com.atc.core.SimulationConfig;
import com.atc.core.SimulationManager;
import com.atc.controllers.EmergencyController;
import com.atc.database.DatabaseManager;
import com.atc.gui.AirTrafficControlGUI;
import com.atc.workers.*;
import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;

public class AirTrafficSystem {
    private static SimulationManager simulationManager;
    private static SimulationConfig currentConfig;
    private static AirTrafficControlGUI gui;
    private static EmergencyController emergencyController;
    private static List<Runnable> workerStoppers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== AIR TRAFFIC CONTROL SYSTEM STARTING ===");

        initializeDatabase();
        initializeRunways();
        initializeSimulation();
        launchGUI();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down system...");
            shutdown();
        }));
    }

    private static void initializeDatabase() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.clearAllRuntimeData(); // Clear old data
            dbManager.initializeAirportData(); // Load real airports
            dbManager.initializeAircraftTypes(); // Load real aircraft specs
            System.out.println("✓ Database connected with realistic data");
        } catch (Exception e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
        }
    }

    private static void initializeRunways() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.insertRunway("RWY-09L", "AVAILABLE", null, 500.0);
        dbManager.insertRunway("RWY-09R", "AVAILABLE", null, 1500.0);
        dbManager.insertRunway("RWY-27L", "AVAILABLE", null, 2500.0);
        dbManager.insertRunway("RWY-27R", "AVAILABLE", null, 3500.0);
        System.out.println("✓ Runways initialized in database with positions: 4");
    }

    private static void initializeSimulation() {
        currentConfig = new SimulationConfig(1000.0, 2.0, 1000.0, 30.0, 120.0);
        simulationManager = SimulationManager.getInstance();
        simulationManager.startSimulation(currentConfig);
        
        // Generate realistic aircraft directly in database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        for (int i = 0; i < 3; i++) {
            String callsign = dbManager.generateAndInsertRealisticFlight();
            if (callsign != null) {
                System.out.println("Generated flight: " + callsign);
            }
        }
        
        startWorkerThreads();
        System.out.println("✓ Realistic simulation initialized with database-only operations");
    }

    private static void startWorkerThreads() {
        AircraftUpdateWorker updateWorker = new AircraftUpdateWorker(currentConfig.getTimeStepSeconds());
        Thread updateThread = new Thread(updateWorker, "AircraftUpdate-Thread");
        updateThread.start();
        simulationManager.addWorkerThread(updateThread);
        workerStoppers.add(updateWorker::stop);

        FuelMonitoringWorker fuelWorker = new FuelMonitoringWorker(currentConfig.getTimeStepSeconds());
        Thread fuelThread = new Thread(fuelWorker, "FuelMonitor-Thread");
        fuelThread.start();
        simulationManager.addWorkerThread(fuelThread);
        workerStoppers.add(fuelWorker::stop);

        for (int i = 0; i < 4; i++) {
            RunwayManagerWorker runwayWorker = new RunwayManagerWorker();
            Thread runwayThread = new Thread(runwayWorker, "RunwayManager-" + i);
            runwayThread.start();
            simulationManager.addWorkerThread(runwayThread);
            workerStoppers.add(runwayWorker::stop);
        }
        
        EmergencyHandlerWorker emergencyWorker = new EmergencyHandlerWorker();
        Thread emergencyThread = new Thread(emergencyWorker, "EmergencyHandler-Thread");
        emergencyThread.start();
        simulationManager.addWorkerThread(emergencyThread);
        workerStoppers.add(emergencyWorker::stop);
        
        WeatherWorker weatherWorker = new WeatherWorker();
        Thread weatherThread = new Thread(weatherWorker, "Weather-Thread");
        weatherThread.start();
        simulationManager.addWorkerThread(weatherThread);
        workerStoppers.add(weatherWorker::stop);

        System.out.println("✓ 8 worker threads started (1 Aircraft Update, 1 Fuel Monitor, 4 Runway Managers, 1 Emergency Handler, 1 Weather)");
    }

    private static void launchGUI() {
        try {
            System.setProperty("java.awt.headless", "false");
            SwingUtilities.invokeLater(() -> {
                try {
                    gui = new AirTrafficControlGUI();
                    gui.setVisible(true);
                    System.out.println("✓ Database-driven GUI launched");
                } catch (Exception e) {
                    System.err.println("✗ GUI launch failed: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("✗ GUI initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updateGUI() {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.updateDisplay());
        }
    }

    public static void addAircraft(String callsign, String aircraftType, double fuel, double speed, double distance, String origin, String destination) {
        DatabaseManager.getInstance().insertActiveAircraft(callsign, aircraftType, fuel, speed, distance, origin, destination, "APPROACHING", "NONE", 100);
        updateGUI();
    }
    
    public static void restartSimulation(SimulationConfig newConfig) {
        workerStoppers.forEach(Runnable::run);
        workerStoppers.clear();
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.clearAllRuntimeData();
        
        for (int i = 0; i < 4; i++) {
            dbManager.insertRunway("RWY-0" + (i + 1), "AVAILABLE", null, (i + 1) * 1000.0);
        }
        
        simulationManager.stopSimulation();
        currentConfig = newConfig;
        simulationManager.startSimulation(newConfig);
        
        for (int i = 0; i < 5; i++) {
            dbManager.generateAndInsertRealisticFlight();
        }
        
        startWorkerThreads();
        updateGUI();
    }

    private static void shutdown() {
        workerStoppers.forEach(Runnable::run);
        if (simulationManager != null) {
            simulationManager.stopSimulation();
        }
        DatabaseManager.getInstance().close();
        System.out.println("✓ System shutdown complete");
    }

    public static List<org.bson.Document> getActiveAircraft() {
        return DatabaseManager.getInstance().getAllActiveAircraft();
    }

    public static List<org.bson.Document> getRunways() {
        return DatabaseManager.getInstance().getAllRunways();
    }
    
    public static SimulationConfig getCurrentConfig() {
        return currentConfig;
    }
}