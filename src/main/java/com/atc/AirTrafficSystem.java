package com.atc;

import com.atc.shared.database.DatabaseManager;
import com.atc.part2.controllers.FlightScheduler;
import com.atc.part2.controllers.WeatherController;
import com.atc.part2.services.WeatherService;
import com.atc.part2.services.FuelMonitoringService;
import com.atc.part2.services.NotificationService;
// TODO: Import Part 1 classes when available
// import com.atc.part1.controllers.LandingController;
// import com.atc.part1.managers.RunwayManager;
import com.atc.gui.AirTrafficGUI;
import javafx.application.Application;

public class AirTrafficSystem {
    // Core Controllers
    private static FlightScheduler flightScheduler;
    private static WeatherController weatherController;
    private static Object landingController; // TODO: Change to LandingController when Part 1 is ready
    private static Object runwayManager;     // TODO: Change to RunwayManager when Part 1 is ready

    // Services
    private static WeatherService weatherService;
    private static FuelMonitoringService fuelMonitoringService;
    private static NotificationService notificationService;

    // System State
    private static boolean isInitialized = false;
    private static boolean isRunning = false;

    public static void main(String[] args) {
        try {
            // TODO: 1. Initialize MongoDB connection
            initializeDatabase();

            // TODO: 2. Initialize core services
            initializeServices();

            // TODO: 3. Initialize controllers
            initializeControllers();

            // TODO: 4. Start background threads
            startBackgroundThreads();

            // TODO: 5. Launch GUI
            launchGUI(args);

            // TODO: 6. Setup shutdown hook
            setupShutdownHook();

        } catch (Exception e) {
            System.err.println("Failed to start Air Traffic System: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // INITIALIZATION METHODS
    private static void initializeDatabase() {
        System.out.println("Initializing database...");
        try {
            DatabaseManager.connect();
            if (DatabaseManager.getInstance().testConnection()) {
                System.out.println("✅ Database connection verified");
            } else {
                throw new RuntimeException("Database connection test failed");
            }
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw e;
        }
    }

    private static void initializeServices() {
        System.out.println("Initializing services...");
        try {
            notificationService = new com.atc.part2.services.NotificationService();
            com.atc.part2.dao.FlightDAO flightDAO = new com.atc.part2.dao.FlightDAO();
            weatherService = new com.atc.part2.services.WeatherService(flightDAO, notificationService);
            fuelMonitoringService = new com.atc.part2.services.FuelMonitoringService(notificationService);
            System.out.println("✅ Services initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Service initialization failed: " + e.getMessage());
            throw e;
        }
    }

    private static void initializeControllers() {
        System.out.println("Initializing controllers...");
        try {
            com.atc.part2.dao.FlightDAO flightDAO = new com.atc.part2.dao.FlightDAO();
            flightScheduler = new com.atc.part2.controllers.FlightScheduler(
                weatherService, fuelMonitoringService, notificationService, flightDAO);
            weatherController = new com.atc.part2.controllers.WeatherController(
                weatherService, flightScheduler);
            System.out.println("✅ Controllers initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Controller initialization failed: " + e.getMessage());
            throw e;
        }
    }

    private static void startBackgroundThreads() {
        System.out.println("Starting background threads...");
        try {
            flightScheduler.startFlightWorkers();
            weatherController.startWeatherProcessing();
            
            // Start monitoring threads
            com.atc.part2.threads.WeatherMonitor weatherMonitor = 
                new com.atc.part2.threads.WeatherMonitor(weatherService);
            com.atc.part2.threads.FuelMonitor fuelMonitor = 
                new com.atc.part2.threads.FuelMonitor(fuelMonitoringService);
            
            new Thread(weatherMonitor).start();
            new Thread(fuelMonitor).start();
            
            isRunning = true;
            System.out.println("✅ Background threads started successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to start background threads: " + e.getMessage());
            throw e;
        }
    }

    private static void launchGUI(String[] args) {
        System.out.println("Launching GUI...");
        try {
            isInitialized = true;
            javafx.application.Application.launch(com.atc.gui.AirTrafficGUI.class, args);
        } catch (Exception e) {
            System.err.println("❌ GUI launch failed: " + e.getMessage());
            throw e;
        }
    }

    private static void setupShutdownHook() {
        // TODO: Setup graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Air Traffic System...");
            shutdown();
        }));
    }

    // SYSTEM CONTROL METHODS
    public static void shutdown() {
        // TODO: Graceful system shutdown
        if (!isRunning) {
            return;
        }

        try {
            // TODO: Stop all threads
            stopBackgroundThreads();

            // TODO: Close database connections
            closeDatabaseConnections();

            // TODO: Save system state
            saveSystemState();

            // TODO: Cleanup resources
            cleanupResources();

            isRunning = false;
            System.out.println("Air Traffic System shutdown complete.");

        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void stopBackgroundThreads() {
        // TODO: Stop all background threads gracefully
        // - Stop flight scheduler threads
        // - Stop weather monitoring
        // - Stop fuel monitoring
        // - Stop runway monitoring (Part 1)
        // - Wait for threads to complete
        System.out.println("Stopping background threads...");
    }

    private static void closeDatabaseConnections() {
        System.out.println("Closing database connections...");
        try {
            DatabaseManager.disconnect();
        } catch (Exception e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    private static void saveSystemState() {
        // TODO: Save current system state
        // - Save active flights
        // - Save active alerts
        // - Save system statistics
        System.out.println("Saving system state...");
    }

    private static void cleanupResources() {
        // TODO: Cleanup system resources
        // - Clear caches
        // - Release memory
        // - Close file handles
        System.out.println("Cleaning up resources...");
    }

    // SYSTEM STATUS METHODS
    public static boolean isInitialized() {
        return isInitialized;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static String getSystemStatus() {
        // TODO: Return system status information
        // - Database connection status
        // - Thread status
        // - Active flights count
        // - Active alerts count
        return "System Status: " + (isRunning ? "Running" : "Stopped");
    }

    // GETTER METHODS FOR CONTROLLERS (for GUI access)
    public static FlightScheduler getFlightScheduler() {
        return flightScheduler;
    }

    public static WeatherController getWeatherController() {
        return weatherController;
    }

    public static Object getLandingController() { // TODO: Change return type when Part 1 is ready
        return landingController;
    }

    public static Object getRunwayManager() { // TODO: Change return type when Part 1 is ready
        return runwayManager;
    }

    // SERVICE GETTERS
    public static WeatherService getWeatherService() {
        return weatherService;
    }

    public static FuelMonitoringService getFuelMonitoringService() {
        return fuelMonitoringService;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

    // SYSTEM CONFIGURATION
    public static void setDatabaseConnectionString(String connectionString) {
        // TODO: Set MongoDB connection string
    }

    public static void setThreadPoolSizes(int flightThreads, int weatherThreads, int monitorThreads) {
        // TODO: Configure thread pool sizes
    }

    public static void enableDebugMode(boolean enabled) {
        // TODO: Enable/disable debug logging
    }

    // ERROR HANDLING
    public static void handleSystemError(Exception e) {
        // TODO: Handle system-wide errors
        // - Log error details
        // - Attempt recovery
        // - Notify administrators
        // - Graceful degradation if needed
        System.err.println("System error: " + e.getMessage());
        e.printStackTrace();
    }

    // HEALTH CHECK
    public static boolean performHealthCheck() {
        // TODO: Perform system health check
        // - Check database connectivity
        // - Check thread status
        // - Check memory usage
        // - Check system responsiveness
        return false;
    }
}