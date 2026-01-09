package com.atc.part2.threads;

import com.atc.part2.services.FuelMonitoringService;
// TODO: Import Aircraft class from Part 1 when available
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class FuelMonitor implements Runnable {
    // Fields
    private final FuelMonitoringService fuelService;
    private final List<Object> monitoredAircraft; // TODO: Change to List<Aircraft> when Part 1 is ready
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;

    // Constructor
    public FuelMonitor(FuelMonitoringService fuelService, List<Object> aircraft) {
        // TODO: Initialize all fields
        this.fuelService = null;
        this.monitoredAircraft = null;
        this.scheduler = null;
    }

    // Thread Methods
    @Override
    public void run() {
        // TODO: Monitors fuel every 10 seconds
        // - Check all aircraft fuel levels
        // - Generate alerts for low fuel
        // - Escalate emergencies
        // - Simulate fuel consumption
    }

    public void stop() {
        // TODO: Graceful shutdown
        // - Set running = false
        // - Shutdown scheduler
    }

    // Business Methods
    private void checkFuelLevels() {
        // TODO: Check all aircraft fuel levels
        // - Iterate through monitored aircraft
        // - Check fuel thresholds
        // - Generate alerts as needed
    }

    private void generateFuelAlert(Object aircraft) { // TODO: Change to Aircraft when available
        // TODO: Create fuel alert for aircraft
        // - Determine alert level
        // - Create FuelAlert object
        // - Save to database
    }

    private void simulateFuelConsumption() {
        // TODO: Decrease fuel levels over time
        // - Reduce fuel for in-flight aircraft
        // - Simulate realistic consumption rates
        // - Update aircraft fuel levels
    }

    private void escalateEmergencies() {
        // TODO: Handle critical fuel situations
        // - Find aircraft with critical fuel
        // - Escalate to emergency status
        // - Notify Part 1 emergency system
    }
}