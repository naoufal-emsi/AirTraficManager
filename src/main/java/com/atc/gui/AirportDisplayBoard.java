package com.atc.gui;

import com.atc.part2.models.Flight;
import com.atc.part1.models.Aircraft;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.atc.AirTrafficSystem;
import com.atc.part2.controllers.FlightScheduler;
import com.atc.part2.controllers.WeatherController;
import com.atc.part2.services.*;
import com.atc.part2.dao.FlightDAO;
import com.atc.part2.threads.*;
import com.atc.part2.models.WeatherAlert;
import com.atc.shared.database.DatabaseManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AirportDisplayBoard extends Application {
    private FlightScheduler flightScheduler;
    private WeatherController weatherController;
    private com.atc.part1.controllers.LandingController landingController;
    private com.atc.part1.managers.RunwayManager runwayManager;
    private WeatherService weatherService;
    private FuelMonitoringService fuelService;
    private NotificationService notificationService;
    
    private TableView<FlightDisplay> arrivalsTable;
    private TableView<FlightDisplay> departuresTable;
    private ListView<String> runwayStatusList;
    private Label clockLabel;
    private Label weatherLabel;
    private Random random = new Random();
    
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.connect();
        initializeServices();
        initializeControllers();
        startBackgroundThreads();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Main display - split between arrivals and departures
        HBox mainDisplay = new HBox(20);
        mainDisplay.setPadding(new Insets(20));
        mainDisplay.setStyle("-fx-background-color: #000000;");
        
        VBox arrivalsBox = createArrivalsBoard();
        VBox departuresBox = createDeparturesBoard();
        
        mainDisplay.getChildren().addAll(arrivalsBox, departuresBox);
        root.setCenter(mainDisplay);
        
        // Bottom - Runway status
        VBox bottom = createRunwayStatus();
        root.setBottom(bottom);
        
        Scene scene = new Scene(root, 1600, 900);
        primaryStage.setTitle("Airport Flight Information Display System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        startUpdates();
        startAutoGenerateFlights();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #FFD700; -fx-border-width: 0 0 3 0;");
        
        Label title = new Label("✈ INTERNATIONAL AIRPORT - FLIGHT INFORMATION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #FFD700;");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        
        HBox infoBar = new HBox(50);
        infoBar.setAlignment(Pos.CENTER);
        
        clockLabel = new Label();
        clockLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        clockLabel.setStyle("-fx-text-fill: #00FF00;");
        
        weatherLabel = new Label("⛅ CLEAR");
        weatherLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        weatherLabel.setStyle("-fx-text-fill: #00FF00;");
        
        infoBar.getChildren().addAll(clockLabel, weatherLabel);
        
        header.getChildren().addAll(title, infoBar);
        return header;
    }

    private VBox createArrivalsBoard() {
        VBox box = new VBox(10);
        box.setPrefWidth(750);
        
        Label title = new Label("🛬 ARRIVALS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #00FF00; -fx-background-color: #1a1a1a; -fx-padding: 10;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        
        arrivalsTable = createFlightTable();
        
        box.getChildren().addAll(title, arrivalsTable);
        return box;
    }

    private VBox createDeparturesBoard() {
        VBox box = new VBox(10);
        box.setPrefWidth(750);
        
        Label title = new Label("🛫 DEPARTURES");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #FFD700; -fx-background-color: #1a1a1a; -fx-padding: 10;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        
        departuresTable = createFlightTable();
        
        box.getChildren().addAll(title, departuresTable);
        return box;
    }

    private TableView<FlightDisplay> createFlightTable() {
        TableView<FlightDisplay> table = new TableView<>();
        table.setStyle("-fx-background-color: #000000; -fx-control-inner-background: #000000;");
        table.setFixedCellSize(45);
        
        TableColumn<FlightDisplay, String> timeCol = new TableColumn<>("TIME");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());
        timeCol.setPrefWidth(80);
        styleColumn(timeCol, "#FFFFFF");
        
        TableColumn<FlightDisplay, String> flightCol = new TableColumn<>("FLIGHT");
        flightCol.setCellValueFactory(data -> data.getValue().flightNumberProperty());
        flightCol.setPrefWidth(100);
        styleColumn(flightCol, "#FFD700");
        
        TableColumn<FlightDisplay, String> cityCol = new TableColumn<>("CITY");
        cityCol.setCellValueFactory(data -> data.getValue().cityProperty());
        cityCol.setPrefWidth(150);
        styleColumn(cityCol, "#FFFFFF");
        
        TableColumn<FlightDisplay, String> gateCol = new TableColumn<>("GATE/RWY");
        gateCol.setCellValueFactory(data -> data.getValue().gateProperty());
        gateCol.setPrefWidth(100);
        styleColumn(gateCol, "#00BFFF");
        
        TableColumn<FlightDisplay, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<FlightDisplay, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = getStatusColor(item);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: CENTER;");
                }
            }
        });
        
        TableColumn<FlightDisplay, String> remarksCol = new TableColumn<>("REMARKS");
        remarksCol.setCellValueFactory(data -> data.getValue().remarksProperty());
        remarksCol.setPrefWidth(170);
        styleColumn(remarksCol, "#FF6B6B");
        
        table.getColumns().addAll(timeCol, flightCol, cityCol, gateCol, statusCol, remarksCol);
        table.setPrefHeight(600);
        
        return table;
    }

    private void styleColumn(TableColumn<FlightDisplay, String> col, String color) {
        col.setStyle("-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold;");
        col.setCellFactory(column -> new TableCell<FlightDisplay, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: CENTER;");
                }
            }
        });
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "ON TIME", "LANDED", "BOARDING" -> "#00FF00";
            case "DELAYED", "FINAL CALL" -> "#FFD700";
            case "CANCELLED", "EMERGENCY" -> "#FF0000";
            case "APPROACHING", "LANDING" -> "#00BFFF";
            default -> "#FFFFFF";
        };
    }

    private VBox createRunwayStatus() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #FFD700; -fx-border-width: 3 0 0 0;");
        
        Label title = new Label("🛬 RUNWAY STATUS & ACTIVE AIRCRAFT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #FFD700;");
        
        runwayStatusList = new ListView<>();
        runwayStatusList.setStyle("-fx-background-color: #000000; -fx-control-inner-background: #000000; " +
                                 "-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00FF00;");
        runwayStatusList.setPrefHeight(120);
        
        box.getChildren().addAll(title, runwayStatusList);
        return box;
    }

    private void startUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            updateClock();
            updateFlightBoards();
            updateRunwayStatus();
            updateWeather();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateClock() {
        Platform.runLater(() -> {
            clockLabel.setText("🕐 " + LocalDateTime.now().format(fullFormat));
        });
    }

    private void updateFlightBoards() {
        Platform.runLater(() -> {
            // Arrivals - show aircraft landing
            ObservableList<FlightDisplay> arrivals = FXCollections.observableArrayList();
            if (AirTrafficSystem.getResourceManager() != null) {
                AirTrafficSystem.getResourceManager().getActiveAircraft().values().stream()
                    .sorted(Comparator.comparing(Aircraft::getRequestTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .forEach(aircraft -> {
                        FlightDisplay fd = new FlightDisplay();
                        fd.setTime(aircraft.getRequestTime() != null ? 
                            aircraft.getRequestTime().format(timeFormat) : "--:--");
                        fd.setFlightNumber(aircraft.getCallsign());
                        fd.setCity(getRandomCity());
                        fd.setGate(aircraft.getAssignedRunway() != null ? aircraft.getAssignedRunway() : "HOLDING");
                        fd.setStatus(formatStatus(aircraft.getStatus()));
                        fd.setRemarks(getFuelRemark(aircraft.getFuelLevel()) + 
                            (aircraft.isEmergency() ? " ⚠ PRIORITY" : ""));
                        arrivals.add(fd);
                    });
            }
            arrivalsTable.setItems(arrivals);
            
            // Departures - show scheduled flights
            ObservableList<FlightDisplay> departures = FXCollections.observableArrayList();
            if (flightScheduler != null) {
                flightScheduler.getActiveFlights().values().stream()
                    .limit(15)
                    .forEach(flight -> {
                        FlightDisplay fd = new FlightDisplay();
                        fd.setTime(flight.getScheduledDeparture() != null ? 
                            flight.getScheduledDeparture().format(timeFormat) : "--:--");
                        fd.setFlightNumber(flight.getFlightNumber());
                        fd.setCity(flight.getDestination());
                        fd.setGate("GATE " + (random.nextInt(20) + 1));
                        fd.setStatus(formatFlightStatus(flight.getStatus()));
                        fd.setRemarks(flight.getDelayMinutes() > 0 ? 
                            "DELAYED +" + flight.getDelayMinutes() + "min" : "");
                        departures.add(fd);
                    });
            }
            departuresTable.setItems(departures);
        });
    }

    private void updateRunwayStatus() {
        Platform.runLater(() -> {
            ObservableList<String> status = FXCollections.observableArrayList();
            
            if (runwayManager != null) {
                runwayManager.getRunways().values().forEach(runway -> {
                    String state = runway.getStatus().equals("FREE") ? "🟢 AVAILABLE" : "🔴 OCCUPIED";
                    String aircraft = runway.getCurrentAircraft() != null ? 
                        " - Aircraft: " + runway.getCurrentAircraft() : "";
                    status.add(String.format("%-8s %s %s", runway.getRunwayId(), state, aircraft));
                });
            }
            
            status.add("─────────────────────────────────────────────────────────────");
            
            if (AirTrafficSystem.getResourceManager() != null) {
                int total = AirTrafficSystem.getResourceManager().getActiveAircraft().size();
                int landing = (int) AirTrafficSystem.getResourceManager().getActiveAircraft().values().stream()
                    .filter(a -> "LANDING".equals(a.getStatus())).count();
                int holding = (int) AirTrafficSystem.getResourceManager().getActiveAircraft().values().stream()
                    .filter(a -> "APPROACHING".equals(a.getStatus())).count();
                
                status.add(String.format("📊 Total Aircraft: %d  |  Landing: %d  |  Holding: %d", 
                    total, landing, holding));
            }
            
            runwayStatusList.setItems(status);
        });
    }

    private void updateWeather() {
        Platform.runLater(() -> {
            if (weatherService != null) {
                List<WeatherAlert> alerts = weatherService.getActiveAlerts();
                if (alerts.isEmpty()) {
                    weatherLabel.setText("⛅ CLEAR - ALL OPERATIONS NORMAL");
                    weatherLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold; -fx-font-size: 20px;");
                } else {
                    WeatherAlert alert = alerts.get(0);
                    weatherLabel.setText("⚠ " + alert.getAlertType() + " - " + alert.getSeverity());
                    weatherLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-weight: bold; -fx-font-size: 20px;");
                }
            }
        });
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "APPROACHING" -> "APPROACHING";
            case "LANDING" -> "LANDING";
            case "LANDED" -> "LANDED";
            case "EMERGENCY", "EMERGENCY_LANDING" -> "EMERGENCY";
            default -> status;
        };
    }

    private String formatFlightStatus(String status) {
        return switch (status) {
            case "SCHEDULED" -> "ON TIME";
            case "DELAYED" -> "DELAYED";
            case "IN_FLIGHT" -> "DEPARTED";
            default -> status;
        };
    }

    private String getFuelRemark(int fuel) {
        if (fuel <= 10) return "⛽ CRITICAL";
        if (fuel <= 20) return "⛽ LOW FUEL";
        return "";
    }

    private String getRandomCity() {
        String[] cities = {"NEW YORK", "LONDON", "PARIS", "DUBAI", "TOKYO", "SINGAPORE", 
                          "LOS ANGELES", "FRANKFURT", "AMSTERDAM", "HONG KONG"};
        return cities[random.nextInt(cities.length)];
    }

    private void startAutoGenerateFlights() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(15), e -> {
            // Auto-generate arrivals
            if (random.nextDouble() < 0.7) {
                String aircraftId = "AC" + String.format("%03d", random.nextInt(1000));
                String callsign = "FL" + (100 + random.nextInt(900));
                int fuel = 30 + random.nextInt(70);
                
                com.atc.part1.models.Aircraft aircraft = 
                    new com.atc.part1.models.Aircraft(aircraftId, callsign, fuel);
                landingController.requestLanding(aircraft);
            }
            
            // Auto-generate departures
            if (random.nextDouble() < 0.5) {
                String[] origins = {"JFK", "LAX", "LHR", "CDG", "DXB"};
                String[] destinations = {"SIN", "HKG", "NRT", "FRA", "AMS"};
                
                String flightId = "FL" + String.format("%03d", random.nextInt(1000));
                String flightNumber = "AA" + (100 + random.nextInt(900));
                
                Flight flight = new Flight(flightId, flightNumber, "AC" + random.nextInt(100),
                    origins[random.nextInt(origins.length)],
                    destinations[random.nextInt(destinations.length)],
                    LocalDateTime.now().plusMinutes(random.nextInt(120)));
                
                flightScheduler.scheduleFlight(flight);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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
        com.atc.part1.managers.ResourceManager resourceManager = 
            new com.atc.part1.managers.ResourceManager();
        AirTrafficSystem.setResourceManager(resourceManager);
        landingController = new com.atc.part1.controllers.LandingController(runwayManager, resourceManager);
        flightScheduler = new FlightScheduler(weatherService, fuelService, notificationService, flightDAO);
        weatherController = new WeatherController(weatherService, flightScheduler);
    }

    private void startBackgroundThreads() {
        landingController.startLandingWorkers();
        new Thread(new WeatherMonitor(weatherService)).start();
        new Thread(new FuelMonitor(fuelService)).start();
        new Thread(new com.atc.part1.threads.RunwayMonitor(runwayManager)).start();
        flightScheduler.startFlightWorkers();
        weatherController.startWeatherProcessing();
    }

    @Override
    public void stop() {
        DatabaseManager.disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Inner class for display data
    public static class FlightDisplay {
        private javafx.beans.property.SimpleStringProperty time = new javafx.beans.property.SimpleStringProperty();
        private javafx.beans.property.SimpleStringProperty flightNumber = new javafx.beans.property.SimpleStringProperty();
        private javafx.beans.property.SimpleStringProperty city = new javafx.beans.property.SimpleStringProperty();
        private javafx.beans.property.SimpleStringProperty gate = new javafx.beans.property.SimpleStringProperty();
        private javafx.beans.property.SimpleStringProperty status = new javafx.beans.property.SimpleStringProperty();
        private javafx.beans.property.SimpleStringProperty remarks = new javafx.beans.property.SimpleStringProperty();

        public javafx.beans.property.SimpleStringProperty timeProperty() { return time; }
        public void setTime(String value) { time.set(value); }

        public javafx.beans.property.SimpleStringProperty flightNumberProperty() { return flightNumber; }
        public void setFlightNumber(String value) { flightNumber.set(value); }

        public javafx.beans.property.SimpleStringProperty cityProperty() { return city; }
        public void setCity(String value) { city.set(value); }

        public javafx.beans.property.SimpleStringProperty gateProperty() { return gate; }
        public void setGate(String value) { gate.set(value); }

        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
        public void setStatus(String value) { status.set(value); }

        public javafx.beans.property.SimpleStringProperty remarksProperty() { return remarks; }
        public void setRemarks(String value) { remarks.set(value); }
    }
}
