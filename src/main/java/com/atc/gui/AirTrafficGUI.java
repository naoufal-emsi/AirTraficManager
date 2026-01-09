package com.atc.gui;

import com.atc.part2.controllers.FlightScheduler;
import com.atc.part2.controllers.WeatherController;
// TODO: Import LandingController from Part 1 when available
import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
// TODO: Import Aircraft from Part 1 when available
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class AirTrafficGUI extends Application {
    // Fields
    private FlightScheduler flightScheduler;
    private WeatherController weatherController;
    private Object landingController;  // TODO: Change to LandingController when Part 1 is ready

    // GUI Components
    private TableView<Flight> flightTable;
    private TableView<Object> aircraftTable; // TODO: Change to TableView<Aircraft> when Part 1 is ready
    private ListView<WeatherAlert> weatherAlertList;
    private ListView<String> logList;

    // Buttons for scenarios
    private Button scheduleFlight;
    private Button createWeatherAlert;
    private Button simulateFuelLow;
    private Button requestLanding;
    private Button declareEmergency;

    @Override
    public void start(Stage primaryStage) {
        // TODO: Initialize all controllers
        // - Create FlightScheduler instance
        // - Create WeatherController instance
        // - Create LandingController instance (from Part 1)
        // - Initialize database connections

        // TODO: Create GUI layout
        // - Set up main window
        // - Create tables and lists
        // - Create buttons
        // - Set up event handlers

        // TODO: Start background threads
        // - Start flight processing threads
        // - Start weather monitoring
        // - Start fuel monitoring
        // - Start periodic GUI updates
    }

    // EVENT HANDLERS
    private void handleScheduleFlight() {
        // TODO: Create new flight with random data
        // - Generate random flight number
        // - Set random origin/destination
        // - Call flightScheduler.scheduleFlight()
        // - Update flight table
        // - Log action
    }

    private void handleWeatherAlert() {
        // TODO: Create weather alert dialog
        // - Show dialog for weather type selection
        // - Generate weather alert with user input
        // - Process affected flights using streams
        // - Update weather alert list
        // - Log action
    }

    private void handleFuelAlert() {
        // TODO: Select random aircraft and reduce fuel
        // - Find aircraft with normal fuel
        // - Reduce fuel level to critical (5-10%)
        // - Generate fuel alert
        // - Update aircraft table
        // - Log action
    }

    private void handleLandingRequest() {
        // TODO: Create new aircraft requesting landing
        // - Generate random aircraft
        // - Call landingController.requestLanding() (Part 1)
        // - Update aircraft table
        // - Log action
    }

    private void handleEmergency() {
        // TODO: Select aircraft and declare emergency
        // - Find aircraft in approach or airborne
        // - Call landingController.declareEmergency() (Part 1)
        // - Update emergency status in GUI
        // - Log action
    }

    // TABLE UPDATE METHODS
    private void updateFlightTable() {
        // TODO: Refresh flight data using streams
        // - Get all flights from flightScheduler
        // - Filter and sort using streams
        // - Update ObservableList for table
        // - Refresh table view
    }

    private void updateAircraftTable() {
        // TODO: Refresh aircraft data
        // - Get all aircraft from database
        // - Update fuel level displays
        // - Update status information
        // - Refresh table view
    }

    private void updateWeatherAlerts() {
        // TODO: Refresh weather alerts
        // - Get active weather alerts
        // - Update alert list
        // - Highlight critical alerts
    }

    private void updateLogMessages() {
        // TODO: Refresh log messages
        // - Get recent system events
        // - Add to log list
        // - Limit to last 50 messages
    }

    // BACKGROUND TASKS
    private void startPeriodicUpdates() {
        // TODO: Schedule GUI updates every 2 seconds
        // - Create Timeline for periodic updates
        // - Update all tables with latest data
        // - Refresh statistics displays
        // - Handle any GUI exceptions
    }

    // TABLE INITIALIZATION
    private void initializeFlightTable() {
        // TODO: Set up flight table columns
        // - Flight ID column
        // - Flight number column
        // - Origin/Destination columns
        // - Status column with color coding
        // - Delay column with progress bar
    }

    private void initializeAircraftTable() {
        // TODO: Set up aircraft table columns
        // - Aircraft ID column
        // - Callsign column
        // - Fuel level column with progress bar
        // - Status column with color coding
        // - Emergency flag column
    }

    private void initializeWeatherAlertList() {
        // TODO: Set up weather alert list
        // - Custom cell factory for alert display
        // - Color coding by severity
        // - Show affected airports
    }

    // UTILITY METHODS
    private void showAlert(String title, String message) {
        // TODO: Show information dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logAction(String action) {
        // TODO: Log user action to system
        // - Add to log list
        // - Save to database
        // - Update log display
    }

    // MAIN METHOD
    public static void main(String[] args) {
        launch(args);
    }
}