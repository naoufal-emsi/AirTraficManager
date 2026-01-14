package com.atc;

import com.atc.shared.database.DatabaseManager;
import com.atc.part1.controllers.LandingController;
import com.atc.part1.managers.RunwayManager;
import com.atc.part1.managers.ResourceManager;
import com.atc.part2.controllers.FlightScheduler;
import com.atc.part2.controllers.WeatherController;
import com.atc.part2.services.WeatherService;
import com.atc.part2.services.FuelMonitoringService;
import com.atc.part2.services.NotificationService;
import com.atc.gui.AirTrafficGUI;

public class AirTrafficSystem {
    private static LandingController landingController;
    private static RunwayManager runwayManager;
    private static ResourceManager resourceManager;
    private static FlightScheduler flightScheduler;
    private static WeatherController weatherController;
    private static WeatherService weatherService;
    private static FuelMonitoringService fuelMonitoringService;
    private static NotificationService notificationService;
    private static boolean isInitialized = false;
    private static boolean isRunning = false;

    public static void main(String[] args) {
        try {
            initializeDatabase();
            initializeServices();
            initializeControllers();
            startBackgroundThreads();
            launchGUI(args);
            setupShutdownHook();
        } catch (Exception e) {
            System.err.println("Failed to start Air Traffic System: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeDatabase() {
        System.out.println("Initializing database...");
        DatabaseManager.connect();
        if (DatabaseManager.getInstance().testConnection()) {
            System.out.println("✅ Database connected");
        }
    }

    private static void initializeServices() {
        System.out.println("Initializing services...");
        notificationService = new NotificationService();
        com.atc.part2.dao.FlightDAO flightDAO = new com.atc.part2.dao.FlightDAO();
        weatherService = new WeatherService(flightDAO, notificationService);
        fuelMonitoringService = new FuelMonitoringService(notificationService);
        System.out.println("✅ Services initialized");
    }

    private static void initializeControllers() {
        System.out.println("Initializing controllers...");
        runwayManager = new RunwayManager();
        resourceManager = new ResourceManager();
        landingController = new LandingController(runwayManager, resourceManager);
        
        com.atc.part2.dao.FlightDAO flightDAO = new com.atc.part2.dao.FlightDAO();
        flightScheduler = new FlightScheduler(weatherService, fuelMonitoringService, notificationService, flightDAO);
        weatherController = new WeatherController(weatherService, flightScheduler);
        System.out.println("✅ Controllers initialized");
    }

    private static void startBackgroundThreads() {
        System.out.println("Starting background threads...");
        landingController.startLandingWorkers();
        flightScheduler.startFlightWorkers();
        weatherController.startWeatherProcessing();
        
        new Thread(new com.atc.part1.threads.RunwayMonitor(runwayManager)).start();
        new Thread(new com.atc.part2.threads.WeatherMonitor(weatherService)).start();
        new Thread(new com.atc.part2.threads.FuelMonitor(fuelMonitoringService)).start();
        
        isRunning = true;
        System.out.println("✅ Background threads started");
    }

    private static void launchGUI(String[] args) {
        System.out.println("Launching GUI...");
        isInitialized = true;
        javafx.application.Application.launch(AirTrafficGUI.class, args);
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            shutdown();
        }));
    }

    public static void shutdown() {
        if (!isRunning) return;
        landingController.shutdown();
        flightScheduler.shutdown();
        weatherController.shutdown();
        notificationService.shutdown();
        DatabaseManager.disconnect();
        isRunning = false;
        System.out.println("✅ Shutdown complete");
    }

    public static LandingController getLandingController() { return landingController; }
    public static RunwayManager getRunwayManager() { return runwayManager; }
    public static ResourceManager getResourceManager() { return resourceManager; }
    public static FlightScheduler getFlightScheduler() { return flightScheduler; }
    public static WeatherController getWeatherController() { return weatherController; }
    public static WeatherService getWeatherService() { return weatherService; }
    public static FuelMonitoringService getFuelMonitoringService() { return fuelMonitoringService; }
    public static NotificationService getNotificationService() { return notificationService; }
    public static boolean isInitialized() { return isInitialized; }
    public static boolean isRunning() { return isRunning; }
}