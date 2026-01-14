package com.atc.gui;

import com.atc.AirTrafficSystem;
import com.atc.part2.controllers.FlightScheduler;
import com.atc.part2.controllers.WeatherController;
import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.services.*;
import com.atc.part2.dao.FlightDAO;
import com.atc.part2.threads.*;
import com.atc.shared.database.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class AirTrafficGUI extends Application {
    private FlightScheduler flightScheduler;
    private WeatherController weatherController;
    private com.atc.part1.controllers.LandingController landingController;
    private com.atc.part1.managers.RunwayManager runwayManager;
    private WeatherService weatherService;
    private FuelMonitoringService fuelService;
    private NotificationService notificationService;
    private WeatherMonitor weatherMonitor;
    private FuelMonitor fuelMonitor;
    
    private TableView<Flight> flightTable;
    private TableView<com.atc.part1.models.Aircraft> aircraftTable;
    private ListView<String> logList;
    private Label statsLabel;
    private Random random = new Random();
    private ObservableList<String> logMessages = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.connect();
            initializeServices();
            initializeControllers();
            startBackgroundThreads();
            
            VBox root = createMainLayout();
            Scene scene = new Scene(root, 1000, 700);
            
            primaryStage.setTitle("Air Traffic Manager - Part 2");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            startPeriodicUpdates();
            logAction("System started successfully");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void initializeServices() {
        notificationService = new NotificationService();
        FlightDAO flightDAO = new FlightDAO();
        weatherService = new WeatherService(flightDAO, notificationService);
        fuelService = new FuelMonitoringService(notificationService);
    }

    private void initializeControllers() {
        FlightDAO flightDAO = new FlightDAO();
        runwayManager = new com.atc.part1.managers.RunwayManager();
        com.atc.part1.managers.ResourceManager resourceManager = new com.atc.part1.managers.ResourceManager();
        landingController = new com.atc.part1.controllers.LandingController(runwayManager, resourceManager);
        flightScheduler = new FlightScheduler(weatherService, fuelService, notificationService, flightDAO);
        weatherController = new WeatherController(weatherService, flightScheduler);
    }

    private void startBackgroundThreads() {
        landingController.startLandingWorkers();
        weatherMonitor = new WeatherMonitor(weatherService);
        fuelMonitor = new FuelMonitor(fuelService);
        
        new Thread(weatherMonitor).start();
        new Thread(fuelMonitor).start();
        new Thread(new com.atc.part1.threads.RunwayMonitor(runwayManager)).start();
        flightScheduler.startFlightWorkers();
        weatherController.startWeatherProcessing();
    }

    private VBox createMainLayout() {
        VBox root = new VBox(10);
        
        Label title = new Label("Air Traffic Manager - Complete System");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        statsLabel = new Label("Statistics: Loading...");
        
        HBox buttonBox = createButtonPanel();
        
        flightTable = createFlightTable();
        aircraftTable = createAircraftTable();
        
        logList = new ListView<>(logMessages);
        logList.setPrefHeight(150);
        
        root.getChildren().addAll(title, statsLabel, buttonBox, 
            new Label("Active Flights:"), flightTable,
            new Label("Aircraft:"), aircraftTable,
            new Label("System Log:"), logList);
        
        return root;
    }

    private HBox createButtonPanel() {
        Button scheduleBtn = new Button("Schedule Flight");
        Button weatherBtn = new Button("Create Weather Alert");
        Button fuelBtn = new Button("Simulate Low Fuel");
        Button landingBtn = new Button("Request Landing");
        Button emergencyBtn = new Button("Declare Emergency");
        Button statsBtn = new Button("Show Statistics");
        
        scheduleBtn.setOnAction(e -> handleScheduleFlight());
        weatherBtn.setOnAction(e -> handleWeatherAlert());
        fuelBtn.setOnAction(e -> handleFuelAlert());
        landingBtn.setOnAction(e -> handleLandingRequest());
        emergencyBtn.setOnAction(e -> handleEmergency());
        statsBtn.setOnAction(e -> showStatistics());
        
        return new HBox(10, scheduleBtn, weatherBtn, fuelBtn, landingBtn, emergencyBtn, statsBtn);
    }

    private TableView<Flight> createFlightTable() {
        TableView<Flight> table = new TableView<>();
        
        TableColumn<Flight, String> idCol = new TableColumn<>("Flight ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightId()));
        
        TableColumn<Flight, String> numberCol = new TableColumn<>("Flight Number");
        numberCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightNumber()));
        
        TableColumn<Flight, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getOrigin() + " → " + data.getValue().getDestination()));
        
        TableColumn<Flight, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        
        TableColumn<Flight, String> delayCol = new TableColumn<>("Delay (min)");
        delayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(data.getValue().getDelayMinutes())));
        
        table.getColumns().addAll(idCol, numberCol, routeCol, statusCol, delayCol);
        table.setPrefHeight(300);
        
        return table;
    }

    private void handleScheduleFlight() {
        String[] origins = {"JFK", "LAX", "LHR", "CDG", "DXB"};
        String[] destinations = {"JFK", "LAX", "LHR", "CDG", "DXB"};
        
        String origin = origins[random.nextInt(origins.length)];
        String destination = destinations[random.nextInt(destinations.length)];
        while (destination.equals(origin)) {
            destination = destinations[random.nextInt(destinations.length)];
        }
        
        String flightId = "FL" + String.format("%03d", random.nextInt(1000));
        String flightNumber = "AA" + (100 + random.nextInt(900));
        
        Flight flight = new Flight(flightId, flightNumber, "AC" + random.nextInt(100), 
                                 origin, destination, LocalDateTime.now().plusHours(1));
        
        flightScheduler.scheduleFlight(flight);
        updateFlightTable();
        logAction("Scheduled flight: " + flightNumber + " from " + origin + " to " + destination);
    }

    private void handleWeatherAlert() {
        String[] types = {"STORM", "FOG", "WIND", "SNOW"};
        String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        String[] airports = {"JFK", "LAX", "LHR", "CDG", "DXB"};
        
        String type = types[random.nextInt(types.length)];
        String severity = severities[random.nextInt(severities.length)];
        String airport = airports[random.nextInt(airports.length)];
        
        WeatherAlert alert = weatherService.createWeatherAlert(type, severity, airport);
        alert.setDescription(severity + " " + type + " at " + airport);
        weatherController.processWeatherAlert(alert);
        
        updateFlightTable();
        logAction("Created weather alert: " + alert.getDescription());
    }

    private void handleFuelAlert() {
        String aircraftId = "AC" + String.format("%03d", random.nextInt(100));
        int lowFuel = 5 + random.nextInt(10); // 5-15% fuel
        
        fuelService.updateFuelLevel(aircraftId, lowFuel);
        logAction("Simulated low fuel for aircraft: " + aircraftId + " (" + lowFuel + "%)");
    }

    private void handleEmergency() {
        String aircraftId = "AC" + String.format("%03d", random.nextInt(100));
        fuelService.escalateToEmergency(aircraftId);
        landingController.declareEmergency(aircraftId);
        logAction("Declared emergency for aircraft: " + aircraftId);
    }

    private void showStatistics() {
        var stats = flightScheduler.getFlightStatistics();
        String message = String.format(
            "Total Flights: %s\nOn-time Percentage: %.1f%%\nAverage Delay: %.1f minutes",
            stats.get("totalFlights"),
            stats.get("onTimePercentage"),
            stats.get("averageDelay")
        );
        showAlert("Flight Statistics", message);
    }

    private void updateFlightTable() {
        Platform.runLater(() -> {
            ObservableList<Flight> flights = FXCollections.observableArrayList(
                flightScheduler.getActiveFlights().values());
            flightTable.setItems(flights);
        });
    }

    private void startPeriodicUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            updateFlightTable();
            updateAircraftTable();
            updateStatistics();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateStatistics() {
        Platform.runLater(() -> {
            var stats = flightScheduler.getFlightStatistics();
            statsLabel.setText(String.format(
                "Flights: %s | On-time: %.1f%% | Avg Delay: %.1f min | Active Alerts: %d",
                stats.get("totalFlights"),
                stats.get("onTimePercentage"),
                stats.get("averageDelay"),
                weatherService.getActiveAlerts().size()
            ));
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logAction(String action) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logMessages.add(0, "[" + timestamp + "] " + action);
            if (logMessages.size() > 50) {
                logMessages.remove(50, logMessages.size());
            }
        });
    }

    private TableView<com.atc.part1.models.Aircraft> createAircraftTable() {
        TableView<com.atc.part1.models.Aircraft> table = new TableView<>();
        
        TableColumn<com.atc.part1.models.Aircraft, String> idCol = new TableColumn<>("Aircraft ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAircraftId()));
        
        TableColumn<com.atc.part1.models.Aircraft, String> callsignCol = new TableColumn<>("Callsign");
        callsignCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCallsign()));
        
        TableColumn<com.atc.part1.models.Aircraft, String> fuelCol = new TableColumn<>("Fuel %");
        fuelCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getFuelLevel())));
        
        TableColumn<com.atc.part1.models.Aircraft, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        
        TableColumn<com.atc.part1.models.Aircraft, String> runwayCol = new TableColumn<>("Runway");
        runwayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getAssignedRunway() != null ? data.getValue().getAssignedRunway() : "-"));
        
        table.getColumns().addAll(idCol, callsignCol, fuelCol, statusCol, runwayCol);
        table.setPrefHeight(200);
        
        return table;
    }

    private void handleLandingRequest() {
        String aircraftId = "AC" + String.format("%03d", random.nextInt(1000));
        String callsign = "FL" + (100 + random.nextInt(900));
        int fuel = 50 + random.nextInt(50);
        
        com.atc.part1.models.Aircraft aircraft = new com.atc.part1.models.Aircraft(aircraftId, callsign, fuel);
        landingController.requestLanding(aircraft);
        
        updateAircraftTable();
        logAction("Landing requested for " + callsign + " (Fuel: " + fuel + "%)");
    }

    private void updateAircraftTable() {
        if (AirTrafficSystem.getResourceManager() != null) {
            Platform.runLater(() -> {
                ObservableList<com.atc.part1.models.Aircraft> aircraft = FXCollections.observableArrayList(
                    AirTrafficSystem.getResourceManager().getActiveAircraft().values());
                aircraftTable.setItems(aircraft);
            });
        }
    }

    @Override
    public void stop() {
        if (weatherMonitor != null) weatherMonitor.stop();
        if (fuelMonitor != null) fuelMonitor.stop();
        if (flightScheduler != null) flightScheduler.shutdown();
        if (weatherController != null) weatherController.shutdown();
        if (landingController != null) landingController.shutdown();
        DatabaseManager.disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}