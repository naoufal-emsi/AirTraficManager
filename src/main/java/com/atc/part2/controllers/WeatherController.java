package com.atc.part2.controllers;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.services.WeatherService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class WeatherController {
    // Fields
    private final WeatherService weatherService;
    private final FlightScheduler flightScheduler;
    private final ExecutorService weatherPool;           // 2 threads for weather processing

    // Constructor
    public WeatherController(WeatherService weatherService, FlightScheduler flightScheduler) {
        // TODO: Initialize all fields
        this.weatherService = null;
        this.flightScheduler = null;
        this.weatherPool = null;
    }

    // WEATHER OPERATIONS WITH STREAMS
    public void processWeatherAlert(WeatherAlert alert) {
        // TODO: Process weather alert
        // - Activate alert in system
        // - Find affected flights using streams
        // - Apply delays/cancellations
        // - Notify passengers
    }

    public List<Flight> findAffectedFlights(WeatherAlert alert) {
        // TODO: STREAM USAGE: Find flights affected by weather
        // flightScheduler.getActiveFlights().stream()
        //     .filter(flight -> isFlightAffected(flight, alert))
        //     .collect(Collectors.toList());
        return null;
    }

    public void applyWeatherDelays(WeatherAlert alert) {
        // TODO: STREAM USAGE: Apply delays based on weather severity
        // findAffectedFlights(alert).stream()
        //     .forEach(flight -> {
        //         int delay = calculateWeatherDelay(alert.getSeverity());
        //         flight.addDelay(delay, "WEATHER");
        //     });
    }

    public void closeRunwaysForWeather(WeatherAlert alert) {
        // TODO: Integration with Part 1: Close runways due to weather
        // Call runwayManager.closeRunway() for affected runways
    }

    public Map<String, Integer> getWeatherImpactStatistics() {
        // TODO: STREAM USAGE: Calculate weather impact statistics
        // weatherService.getActiveAlerts().stream()
        //     .collect(Collectors.toMap(
        //         WeatherAlert::getAffectedAirport,
        //         alert -> findAffectedFlights(alert).size()));
        return null;
    }

    // THREAD MANAGEMENT
    public void startWeatherProcessing() {
        // TODO: Start weather processing threads
    }

    public void processWeatherUpdates() {
        // TODO: Continuous weather monitoring
    }

    // Helper Methods
    private boolean isFlightAffected(Flight flight, WeatherAlert alert) {
        // TODO: Check if flight is affected by weather alert
        return false;
    }

    private int calculateWeatherDelay(String severity) {
        // TODO: Calculate delay based on weather severity
        return 0;
    }
}