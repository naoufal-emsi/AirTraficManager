package com.atc.part2.services;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.dao.WeatherAlertDAO;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherService {
    // Fields
    private final ConcurrentHashMap<String, WeatherAlert> activeAlerts;
    private final List<String> airports;
    private final WeatherAlertDAO weatherDAO;
    private final NotificationService notificationService;

    // Constructor
    public WeatherService(List<String> airports, WeatherAlertDAO weatherDAO) {
        // TODO: Initialize all fields
        this.activeAlerts = null;
        this.airports = null;
        this.weatherDAO = null;
        this.notificationService = null;
    }

    // STREAMS & LAMBDAS METHODS
    public List<Flight> getAffectedFlights(WeatherAlert alert) {
        // TODO: STREAM USAGE: Filter flights by origin/destination matching alert airport
        // flights.stream()
        //     .filter(flight -> flight.getOrigin().equals(alert.getAffectedAirport()) ||
        //                      flight.getDestination().equals(alert.getAffectedAirport()))
        //     .filter(flight -> !flight.getStatus().equals("LANDED"))
        //     .collect(Collectors.toList());
        return null;
    }

    public List<Flight> getFlightsByWeatherSeverity(String severity) {
        // TODO: STREAM USAGE: Filter flights affected by specific weather severity
        // flights.stream()
        //     .filter(flight -> flight.getWeatherAlerts().stream()
        //         .anyMatch(alertId -> getAlertById(alertId).getSeverity().equals(severity)))
        //     .collect(Collectors.toList());
        return null;
    }

    public Map<String, List<Flight>> groupFlightsByAirport(List<Flight> flights) {
        // TODO: STREAM USAGE: Group flights by origin airport
        // flights.stream()
        //     .collect(Collectors.groupingBy(Flight::getOrigin));
        return null;
    }

    public List<Flight> getDelayedFlightsByWeather() {
        // TODO: STREAM USAGE: Find all flights delayed due to weather
        // flights.stream()
        //     .filter(Flight::isDelayed)
        //     .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
        //     .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
        //     .collect(Collectors.toList());
        return null;
    }

    public OptionalDouble getAverageDelayByWeather() {
        // TODO: STREAM USAGE: Calculate average weather delay
        // flights.stream()
        //     .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
        //     .mapToInt(Flight::getDelayMinutes)
        //     .average();
        return null;
    }

    public void applyWeatherDelays(WeatherAlert alert) {
        // TODO: STREAM USAGE: Apply delays to affected flights
        // getAffectedFlights(alert).stream()
        //     .forEach(flight -> {
        //         int delay = calculateDelayBySeverity(alert.getSeverity());
        //         flight.addDelay(delay, "WEATHER");
        //         updateFlightStatus(flight);
        //     });
    }

    // WEATHER OPERATIONS
    public WeatherAlert createWeatherAlert(String type, String severity, String airport) {
        // TODO: Create new weather alert
        return null;
    }

    public void activateAlert(String alertId) {
        // TODO: Activate weather alert
    }

    public void deactivateAlert(String alertId) {
        // TODO: Deactivate weather alert
    }

    public List<WeatherAlert> getActiveAlerts() {
        // TODO: Get all active weather alerts
        return null;
    }

    public void processWeatherUpdate(String airport, String condition) {
        // TODO: Process weather update for airport
    }

    // LAMBDA USAGE EXAMPLES
    public void notifyAffectedFlights(WeatherAlert alert) {
        // TODO: LAMBDA USAGE
        // getAffectedFlights(alert).forEach(flight -> 
        //     notificationService.sendWeatherNotification(flight, alert));
    }

    public List<String> getAirportsWithActiveAlerts() {
        // TODO: LAMBDA USAGE
        // activeAlerts.values().stream()
        //     .filter(WeatherAlert::isActive)
        //     .map(WeatherAlert::getAffectedAirport)
        //     .distinct()
        //     .collect(Collectors.toList());
        return null;
    }

    // Helper Methods
    private int calculateDelayBySeverity(String severity) {
        // TODO: Calculate delay minutes based on weather severity
        return 0;
    }

    private void updateFlightStatus(Flight flight) {
        // TODO: Update flight status in database
    }

    private WeatherAlert getAlertById(String alertId) {
        // TODO: Get weather alert by ID
        return null;
    }
}