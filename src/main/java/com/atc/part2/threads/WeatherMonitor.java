package com.atc.part2.threads;

import com.atc.part2.services.WeatherService;
import com.atc.part2.models.WeatherAlert;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class WeatherMonitor implements Runnable {
    private final WeatherService weatherService;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;
    private final Random random;
    private final String[] airports = {"JFK", "LAX", "LHR", "CDG", "DXB"};
    private final String[] weatherTypes = {"STORM", "FOG", "WIND", "SNOW", "RAIN"};
    private final String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};

    public WeatherMonitor(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = true;
        this.random = new Random();
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(() -> {
            if (running) {
                checkWeatherConditions();
                updateExistingAlerts();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }

    private void checkWeatherConditions() {
        // Simulate weather monitoring
        if (random.nextDouble() < 0.3) { // 30% chance of weather event
            String airport = airports[random.nextInt(airports.length)];
            String weatherType = weatherTypes[random.nextInt(weatherTypes.length)];
            String severity = severities[random.nextInt(severities.length)];
            
            generateWeatherAlert(airport, weatherType, severity);
        }
    }

    private void generateWeatherAlert(String airport, String weatherType, String severity) {
        WeatherAlert alert = weatherService.createWeatherAlert(weatherType, severity, airport);
        alert.setDescription(String.format("%s %s affecting %s airport", severity, weatherType, airport));
        weatherService.activateAlert(alert.getAlertId());
        System.out.println("[WEATHER MONITOR] Generated alert: " + alert.getDescription());
    }

    private void updateExistingAlerts() {
        weatherService.getActiveAlerts().stream()
            .filter(alert -> random.nextDouble() < 0.1) // 10% chance to resolve
            .forEach(alert -> {
                weatherService.deactivateAlert(alert.getAlertId());
                System.out.println("[WEATHER MONITOR] Resolved alert: " + alert.getAlertId());
            });
    }
}