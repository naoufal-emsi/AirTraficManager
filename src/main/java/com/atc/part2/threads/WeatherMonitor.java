package com.atc.part2.threads;

import com.atc.part2.services.WeatherService;
import com.atc.part2.models.WeatherAlert;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherMonitor implements Runnable {
    // Fields
    private final WeatherService weatherService;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, WeatherAlert> activeAlerts;
    private volatile boolean running;

    // Constructor
    public WeatherMonitor(WeatherService weatherService) {
        // TODO: Initialize all fields
        this.weatherService = null;
        this.scheduler = null;
        this.activeAlerts = null;
    }

    // Thread Methods
    @Override
    public void run() {
        // TODO: Monitors weather every 30 seconds
        // - Check weather conditions
        // - Generate alerts if needed
        // - Update existing alerts
        // - Notify affected flights
    }

    public void stop() {
        // TODO: Graceful shutdown
        // - Set running = false
        // - Shutdown scheduler
    }

    // Business Methods
    private void checkWeatherConditions() {
        // TODO: Check current weather conditions
        // - Simulate weather data
        // - Detect severe conditions
        // - Generate alerts as needed
    }

    private void generateWeatherAlert(String airport, String condition) {
        // TODO: Create new weather alert
        // - Determine severity
        // - Set affected runways
        // - Activate alert
    }

    private void updateExistingAlerts() {
        // TODO: Update existing weather alerts
        // - Check if alerts should expire
        // - Update severity if changed
        // - Deactivate resolved alerts
    }

    private void notifyAffectedFlights(WeatherAlert alert) {
        // TODO: Notify flights affected by weather
        // - Find affected flights using streams
        // - Apply delays
        // - Send notifications
    }

    private void simulateWeatherChange() {
        // TODO: Random weather generation for testing
        // - Generate random weather events
        // - Vary severity levels
        // - Affect different airports
    }
}