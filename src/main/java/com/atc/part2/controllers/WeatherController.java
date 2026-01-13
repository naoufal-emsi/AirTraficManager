package com.atc.part2.controllers;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.services.WeatherService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WeatherController {
    private final WeatherService weatherService;
    private final FlightScheduler flightScheduler;
    private final ExecutorService weatherPool;

    public WeatherController(WeatherService weatherService, FlightScheduler flightScheduler) {
        this.weatherService = weatherService;
        this.flightScheduler = flightScheduler;
        this.weatherPool = Executors.newFixedThreadPool(2);
    }

    public void processWeatherAlert(WeatherAlert alert) {
        weatherService.activateAlert(alert.getAlertId());
        applyWeatherDelays(alert);
        flightScheduler.handleWeatherImpact(alert);
    }

    public List<Flight> findAffectedFlights(WeatherAlert alert) {
        return flightScheduler.getActiveFlights().values().stream()
            .filter(flight -> isFlightAffected(flight, alert))
            .collect(Collectors.toList());
    }

    public void applyWeatherDelays(WeatherAlert alert) {
        findAffectedFlights(alert).stream()
            .forEach(flight -> {
                int delay = calculateWeatherDelay(alert.getSeverity());
                flight.addDelay(delay, "WEATHER");
                flight.addWeatherAlert(alert.getAlertId());
            });
    }

    public Map<String, Integer> getWeatherImpactStatistics() {
        return weatherService.getActiveAlerts().stream()
            .collect(Collectors.toMap(
                WeatherAlert::getAffectedAirport,
                alert -> findAffectedFlights(alert).size()));
    }

    public void startWeatherProcessing() {
        weatherPool.submit(this::processWeatherUpdates);
    }

    public void processWeatherUpdates() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                weatherService.getActiveAlerts().stream()
                    .forEach(this::processWeatherAlert);
                Thread.sleep(30000); // Check every 30 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        weatherPool.shutdown();
    }

    private boolean isFlightAffected(Flight flight, WeatherAlert alert) {
        return alert.getAffectedAirport().equals(flight.getOrigin()) ||
               alert.getAffectedAirport().equals(flight.getDestination());
    }

    private int calculateWeatherDelay(String severity) {
        return switch (severity) {
            case "LOW" -> 15;
            case "MEDIUM" -> 30;
            case "HIGH" -> 60;
            case "CRITICAL" -> 120;
            default -> 0;
        };
    }
}