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
    private ObservableList<Flight> flightData = FXCollections.observableArrayList();
    private ObservableList<com.atc.part1.models.Aircraft> aircraftData = FXCollections.observableArrayList();
    private Timeline statusUpdateTimeline;

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.connect();
            initializeServices();
            initializeControllers();
            startBackgroundThreads();
            
            VBox root = createMainLayout();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add("data:text/css," + getCSS());
            
            primaryStage.setTitle("Air Traffic Manager - Complete System");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
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
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        
        Label title = new Label("✈️ Air Traffic Manager - Complete System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        statsLabel = new Label("Statistics: Loading...");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        
        HBox buttonBox = createButtonPanel();
        
        HBox tablesBox = new HBox(15);
        VBox flightBox = new VBox(5);
        Label flightLabel = new Label("🛫 Active Flights");
        flightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        flightTable = createFlightTable();
        flightBox.getChildren().addAll(flightLabel, flightTable);
        
        VBox aircraftBox = new VBox(5);
        Label aircraftLabel = new Label("✈️ Aircraft Status");
        aircraftLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        aircraftTable = createAircraftTable();
        aircraftBox.getChildren().addAll(aircraftLabel, aircraftTable);
        
        tablesBox.getChildren().addAll(flightBox, aircraftBox);
        
        VBox logBox = new VBox(5);
        Label logLabel = new Label("📋 System Log");
        logLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        logList = new ListView<>(logMessages);
        logList.setPrefHeight(200);
        logList.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        logBox.getChildren().addAll(logLabel, logList);
        
        root.getChildren().addAll(title, statsLabel, buttonBox, tablesBox, logBox);
        
        return root;
    }

    private HBox createButtonPanel() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Button scheduleBtn = createStyledButton("🛫 Schedule Flight", "#3498db");
        Button weatherBtn = createStyledButton("⛈️ Weather Alert", "#e67e22");
        Button fuelBtn = createStyledButton("⛽ Low Fuel", "#e74c3c");
        Button landingBtn = createStyledButton("🛬 Request Landing", "#2ecc71");
        Button emergencyBtn = createStyledButton("🚨 Emergency", "#c0392b");
        Button statsBtn = createStyledButton("📊 Statistics", "#9b59b6");
        
        scheduleBtn.setOnAction(e -> handleScheduleFlight());
        weatherBtn.setOnAction(e -> handleWeatherAlert());
        fuelBtn.setOnAction(e -> handleFuelAlert());
        landingBtn.setOnAction(e -> handleLandingRequest());
        emergencyBtn.setOnAction(e -> handleEmergency());
        statsBtn.setOnAction(e -> showStatistics());
        
        buttonBox.getChildren().addAll(scheduleBtn, weatherBtn, fuelBtn, landingBtn, emergencyBtn, statsBtn);
        return buttonBox;
    }
    
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;", color));
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-opacity: 0.8;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-opacity: 0.8;", "")));
        return btn;
    }

    private TableView<Flight> createFlightTable() {
        TableView<Flight> table = new TableView<>(flightData);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        TableColumn<Flight, String> idCol = new TableColumn<>("Flight ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightId()));
        idCol.setPrefWidth(100);
        
        TableColumn<Flight, String> numberCol = new TableColumn<>("Flight Number");
        numberCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFlightNumber()));
        numberCol.setPrefWidth(120);
        
        TableColumn<Flight, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getOrigin() + " → " + data.getValue().getDestination()));
        routeCol.setPrefWidth(150);
        
        TableColumn<Flight, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch(item) {
                        case "DELAYED" -> "-fx-background-color: #ffe6e6; -fx-text-fill: #c0392b;";
                        case "SCHEDULED" -> "-fx-background-color: #e6f7ff; -fx-text-fill: #2980b9;";
                        case "IN_FLIGHT" -> "-fx-background-color: #e6ffe6; -fx-text-fill: #27ae60;";
                        case "LANDED" -> "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
                        case "BOARDING" -> "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
                        default -> "";
                    };
                    setStyle(color + " -fx-font-weight: bold; -fx-padding: 5;");
                }
            }
        });
        statusCol.setPrefWidth(120);
        
        TableColumn<Flight, String> delayCol = new TableColumn<>("Delay (min)");
        delayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDelayMinutes())));
        delayCol.setCellFactory(col -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    int delay = Integer.parseInt(item);
                    if (delay > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
        delayCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, numberCol, routeCol, statusCol, delayCol);
        table.setPrefHeight(350);
        
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
            flightData.setAll(flightScheduler.getActiveFlights().values());
            flightTable.refresh();
        });
    }

    private void startPeriodicUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            updateFlightTable();
            updateAircraftTable();
            updateStatistics();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Status simulation timeline
        statusUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> simulateStatusChanges()));
        statusUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        statusUpdateTimeline.play();
    }
    
    private void simulateStatusChanges() {
        // Update flight statuses
        for (Flight flight : flightData) {
            String currentStatus = flight.getStatus();
            switch (currentStatus) {
                case "SCHEDULED" -> {
                    if (random.nextDouble() < 0.3) {
                        flight.setStatus("BOARDING");
                        logAction(flight.getFlightNumber() + " now BOARDING");
                    }
                }
                case "BOARDING" -> {
                    if (random.nextDouble() < 0.4) {
                        flight.setStatus("IN_FLIGHT");
                        logAction(flight.getFlightNumber() + " departed - IN_FLIGHT");
                    }
                }
                case "IN_FLIGHT" -> {
                    if (random.nextDouble() < 0.2) {
                        flight.setStatus("LANDED");
                        logAction(flight.getFlightNumber() + " LANDED successfully");
                    }
                }
                case "DELAYED" -> {
                    if (random.nextDouble() < 0.15) {
                        flight.setStatus("BOARDING");
                        logAction(flight.getFlightNumber() + " delay cleared - BOARDING");
                    }
                }
            }
        }
        
        // Update aircraft statuses
        for (com.atc.part1.models.Aircraft aircraft : aircraftData) {
            String currentStatus = aircraft.getStatus();
            int fuel = aircraft.getFuelLevel();
            
            switch (currentStatus) {
                case "SCHEDULED" -> {
                    if (random.nextDouble() < 0.3) {
                        aircraft.setStatus("APPROACHING");
                        logAction(aircraft.getCallsign() + " APPROACHING airport");
                    }
                }
                case "APPROACHING" -> {
                    if (random.nextDouble() < 0.4) {
                        aircraft.setStatus("LANDING");
                        aircraft.setAssignedRunway("RWY-" + (random.nextInt(2) + 1));
                        logAction(aircraft.getCallsign() + " cleared for LANDING on " + aircraft.getAssignedRunway());
                    }
                }
                case "LANDING" -> {
                    if (random.nextDouble() < 0.5) {
                        aircraft.setStatus("LANDED");
                        logAction(aircraft.getCallsign() + " LANDED on " + aircraft.getAssignedRunway());
                    }
                }
            }
            
            // Decrease fuel over time
            if (!"LANDED".equals(currentStatus) && fuel > 0 && random.nextDouble() < 0.2) {
                aircraft.setFuelLevel(Math.max(0, fuel - random.nextInt(3)));
                if (aircraft.getFuelLevel() <= 10 && !aircraft.isEmergency()) {
                    aircraft.setEmergency(true);
                    aircraft.setStatus("EMERGENCY");
                    logAction("⚠️ EMERGENCY: " + aircraft.getCallsign() + " low fuel (" + aircraft.getFuelLevel() + "%)");
                }
            }
        }
        
        Platform.runLater(() -> {
            flightTable.refresh();
            aircraftTable.refresh();
        });
    }

    private void updateStatistics() {
        Platform.runLater(() -> {
            var stats = flightScheduler.getFlightStatistics();
            int activeAircraft = AirTrafficSystem.getResourceManager() != null ? 
                AirTrafficSystem.getResourceManager().getActiveAircraft().size() : 0;
            int availableRunways = runwayManager != null ? runwayManager.getAvailableRunways() : 0;
            int activeAlerts = weatherService.getActiveAlerts().size();
            
            statsLabel.setText(String.format(
                "🛫 Flights: %s | ✅ On-time: %.1f%% | ⏱️ Avg Delay: %.1f min | " +
                "✈️ Aircraft: %d | 🛬 Runways: %d/2 | ⛈️ Alerts: %d | 🔄 Live",
                stats.get("totalFlights"),
                stats.get("onTimePercentage"),
                stats.get("averageDelay"),
                activeAircraft,
                availableRunways,
                activeAlerts
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
        TableView<com.atc.part1.models.Aircraft> table = new TableView<>(aircraftData);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        TableColumn<com.atc.part1.models.Aircraft, String> idCol = new TableColumn<>("Aircraft ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAircraftId()));
        idCol.setPrefWidth(100);
        
        TableColumn<com.atc.part1.models.Aircraft, String> callsignCol = new TableColumn<>("Callsign");
        callsignCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCallsign()));
        callsignCol.setPrefWidth(100);
        
        TableColumn<com.atc.part1.models.Aircraft, String> fuelCol = new TableColumn<>("Fuel %");
        fuelCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getFuelLevel())));
        fuelCol.setCellFactory(col -> new TableCell<com.atc.part1.models.Aircraft, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + "%");
                    int fuel = Integer.parseInt(item);
                    String color = fuel <= 10 ? "-fx-background-color: #ffcccc; -fx-text-fill: #c0392b;" :
                                  fuel <= 20 ? "-fx-background-color: #fff3cd; -fx-text-fill: #e67e22;" :
                                  "-fx-background-color: #d4edda; -fx-text-fill: #27ae60;";
                    setStyle(color + " -fx-font-weight: bold; -fx-padding: 5;");
                }
            }
        });
        fuelCol.setPrefWidth(100);
        
        TableColumn<com.atc.part1.models.Aircraft, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<com.atc.part1.models.Aircraft, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch(item) {
                        case "EMERGENCY", "EMERGENCY_LANDING" -> "-fx-background-color: #ffcccc; -fx-text-fill: #c0392b;";
                        case "LANDING", "APPROACHING" -> "-fx-background-color: #fff3cd; -fx-text-fill: #e67e22;";
                        case "LANDED" -> "-fx-background-color: #d4edda; -fx-text-fill: #27ae60;";
                        default -> "-fx-background-color: #e6f7ff; -fx-text-fill: #2980b9;";
                    };
                    setStyle(color + " -fx-font-weight: bold; -fx-padding: 5;");
                }
            }
        });
        statusCol.setPrefWidth(150);
        
        TableColumn<com.atc.part1.models.Aircraft, String> runwayCol = new TableColumn<>("Runway");
        runwayCol.setCellValueFactory(data -> {
            String runway = data.getValue().getAssignedRunway() != null ? 
                data.getValue().getAssignedRunway() : "-";
            return new javafx.beans.property.SimpleStringProperty(runway);
        });
        runwayCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, callsignCol, fuelCol, statusCol, runwayCol);
        table.setPrefHeight(350);
        
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
                aircraftData.setAll(AirTrafficSystem.getResourceManager().getActiveAircraft().values());
                aircraftTable.refresh();
            });
        }
    }

    @Override
    public void stop() {
        if (statusUpdateTimeline != null) statusUpdateTimeline.stop();
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
    
    private String getCSS() {
        return ".table-view { -fx-background-color: white; -fx-background-radius: 5; } " +
               ".table-view .column-header { -fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; } " +
               ".table-view .table-cell { -fx-padding: 8; } " +
               ".list-view { -fx-background-color: white; -fx-background-radius: 5; } " +
               ".list-cell { -fx-padding: 5; } " +
               ".list-cell:odd { -fx-background-color: #f8f9fa; }";
    }
}