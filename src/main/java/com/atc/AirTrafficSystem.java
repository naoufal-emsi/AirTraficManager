package com.atc;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.core.SimulationConfig;
import com.atc.core.SimulationManager;
import com.atc.controllers.EmergencyController;
import com.atc.database.DatabaseManager;
import com.atc.gui.AirTrafficControlGUI;
import com.atc.utils.AircraftGenerator;
import com.atc.workers.*;
import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;

public class AirTrafficSystem {
    private static final List<Aircraft> activeAircraft = new CopyOnWriteArrayList<>();
    private static final List<Runway> runways = new CopyOnWriteArrayList<>();
    private static final PriorityBlockingQueue<Aircraft> landingQueue =
            new PriorityBlockingQueue<>(50, Comparator.comparingInt(Aircraft::getPriority));

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
            DatabaseManager.getInstance();
            System.out.println("✓ Database connected");
        } catch (Exception e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
        }
    }

    private static void initializeRunways() {
        runways.add(new Runway("RWY-09L"));
        runways.add(new Runway("RWY-09R"));
        runways.add(new Runway("RWY-27L"));
        runways.add(new Runway("RWY-27R"));
        System.out.println("✓ Runways initialized: " + runways.size());
    }

    private static void initializeSimulation() {
        currentConfig = new SimulationConfig(1000.0, 2.0, 1000.0, 30.0, 120.0);
        simulationManager = SimulationManager.getInstance();
        simulationManager.startSimulation(currentConfig);
        
        for (int i = 0; i < 5; i++) {
            Aircraft aircraft = AircraftGenerator.generateRandomAircraft(currentConfig);
            activeAircraft.add(aircraft);
            landingQueue.offer(aircraft);
        }
        
        startWorkerThreads();
        System.out.println("✓ Simulation initialized with " + activeAircraft.size() + " aircraft");
    }

    private static void startWorkerThreads() {
        AircraftUpdateWorker updateWorker = new AircraftUpdateWorker(activeAircraft, currentConfig.getTimeStepSeconds());
        Thread updateThread = new Thread(updateWorker, "AircraftUpdate-Thread");
        updateThread.start();
        simulationManager.addWorkerThread(updateThread);
        workerStoppers.add(updateWorker::stop);

        FuelMonitoringWorker fuelWorker = new FuelMonitoringWorker(activeAircraft, landingQueue, currentConfig);
        Thread fuelThread = new Thread(fuelWorker, "FuelMonitor-Thread");
        fuelThread.start();
        simulationManager.addWorkerThread(fuelThread);
        workerStoppers.add(fuelWorker::stop);

        for (int i = 0; i < 3; i++) {
            RunwayManagerWorker runwayWorker = new RunwayManagerWorker(runways, landingQueue, currentConfig);
            Thread runwayThread = new Thread(runwayWorker, "RunwayManager-" + i);
            runwayThread.start();
            simulationManager.addWorkerThread(runwayThread);
            workerStoppers.add(runwayWorker::stop);
        }

        EmergencyHandlerWorker emergencyWorker = new EmergencyHandlerWorker(activeAircraft, runways);
        Thread emergencyThread = new Thread(emergencyWorker, "EmergencyHandler-Thread");
        emergencyThread.start();
        simulationManager.addWorkerThread(emergencyThread);
        workerStoppers.add(emergencyWorker::stop);

        WeatherWorker weatherWorker = new WeatherWorker(runways, activeAircraft, landingQueue);
        Thread weatherThread = new Thread(weatherWorker, "Weather-Thread");
        weatherThread.start();
        simulationManager.addWorkerThread(weatherThread);
        workerStoppers.add(weatherWorker::stop);

        System.out.println("✓ Worker threads started");
    }

    private static void launchGUI() {
        try {
            System.setProperty("java.awt.headless", "false");
            SwingUtilities.invokeLater(() -> {
                try {
                    emergencyController = new EmergencyController(activeAircraft, landingQueue);
                    gui = new AirTrafficControlGUI(activeAircraft, runways, emergencyController, landingQueue, currentConfig);
                    gui.setVisible(true);
                    System.out.println("✓ GUI launched");
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

    public static void addAircraft(Aircraft aircraft) {
        activeAircraft.add(aircraft);
        landingQueue.offer(aircraft);
        updateGUI();
    }
    
    public static void restartSimulation(SimulationConfig newConfig) {
        workerStoppers.forEach(Runnable::run);
        workerStoppers.clear();
        
        simulationManager.restartSimulation(newConfig, activeAircraft, runways, landingQueue);
        currentConfig = newConfig;
        
        for (int i = 0; i < 5; i++) {
            Aircraft aircraft = AircraftGenerator.generateRandomAircraft(currentConfig);
            activeAircraft.add(aircraft);
            landingQueue.offer(aircraft);
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

    public static List<Aircraft> getActiveAircraft() {
        return activeAircraft;
    }

    public static PriorityBlockingQueue<Aircraft> getLandingQueue() {
        return landingQueue;
    }

    public static List<Runway> getRunways() {
        return runways;
    }
    
    public static SimulationConfig getCurrentConfig() {
        return currentConfig;
    }
}