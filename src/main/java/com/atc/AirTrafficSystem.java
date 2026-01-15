package com.atc;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
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
    
    private static ExecutorService executorService;
    private static List<Thread> workerThreads = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== AIR TRAFFIC CONTROL SYSTEM STARTING ===");
        
        initializeDatabase();
        initializeRunways();
        initializeAircraft();
        startWorkerThreads();
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

    private static void initializeAircraft() {
        for (int i = 0; i < 5; i++) {
            Aircraft aircraft = AircraftGenerator.generateRandomAircraft();
            activeAircraft.add(aircraft);
            landingQueue.offer(aircraft);
        }
        System.out.println("✓ Initial aircraft created: " + activeAircraft.size());
    }

    private static void startWorkerThreads() {
        executorService = Executors.newFixedThreadPool(10);
        
        Thread aircraftUpdateThread = new Thread(new AircraftUpdateWorker(activeAircraft));
        aircraftUpdateThread.setName("AircraftUpdate-Thread");
        aircraftUpdateThread.start();
        workerThreads.add(aircraftUpdateThread);
        
        Thread fuelMonitorThread = new Thread(new FuelMonitoringWorker(activeAircraft));
        fuelMonitorThread.setName("FuelMonitor-Thread");
        fuelMonitorThread.start();
        workerThreads.add(fuelMonitorThread);
        
        for (int i = 0; i < 3; i++) {
            Thread runwayThread = new Thread(new RunwayManagerWorker(runways, landingQueue));
            runwayThread.setName("RunwayManager-" + i);
            runwayThread.start();
            workerThreads.add(runwayThread);
        }
        
        Thread emergencyThread = new Thread(new EmergencyHandlerWorker(activeAircraft));
        emergencyThread.setName("EmergencyHandler-Thread");
        emergencyThread.start();
        workerThreads.add(emergencyThread);
        
        Thread weatherThread = new Thread(new WeatherWorker(runways));
        weatherThread.setName("Weather-Thread");
        weatherThread.start();
        workerThreads.add(weatherThread);
        
        System.out.println("✓ Worker threads started: " + workerThreads.size());
    }

    private static void launchGUI() {
        try {
            System.setProperty("java.awt.headless", "false");
            SwingUtilities.invokeLater(() -> {
                try {
                    EmergencyController emergencyController = new EmergencyController(activeAircraft);
                    AirTrafficControlGUI gui = new AirTrafficControlGUI(activeAircraft, runways, emergencyController);
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

    private static void shutdown() {
        for (Thread thread : workerThreads) {
            thread.interrupt();
        }
        if (executorService != null) {
            executorService.shutdownNow();
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
}
