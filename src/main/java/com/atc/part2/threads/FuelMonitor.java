package com.atc.part2.threads;

import com.atc.part2.services.FuelMonitoringService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class FuelMonitor implements Runnable {
    private final FuelMonitoringService fuelService;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;
    private final Random random;
    private final List<String> monitoredAircraft;

    public FuelMonitor(FuelMonitoringService fuelService) {
        this.fuelService = fuelService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = true;
        this.random = new Random();
        this.monitoredAircraft = new ArrayList<>();
        initializeAircraft();
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(() -> {
            if (running) {
                checkFuelLevels();
                simulateFuelConsumption();
                escalateEmergencies();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }

    public void addAircraft(String aircraftId, int initialFuel) {
        monitoredAircraft.add(aircraftId);
        fuelService.updateFuelLevel(aircraftId, initialFuel);
    }

    private void initializeAircraft() {
        // Initialize some test aircraft
        addAircraft("AC001", 75);
        addAircraft("AC002", 15);
        addAircraft("AC003", 5);
        addAircraft("AC004", 85);
        addAircraft("AC005", 45);
    }

    private void checkFuelLevels() {
        fuelService.checkAllFuelThresholds();
        fuelService.processLowFuelAircraft();
    }

    private void simulateFuelConsumption() {
        monitoredAircraft.forEach(aircraftId -> {
            if (random.nextDouble() < 0.7) { // 70% chance to consume fuel
                int currentFuel = getCurrentFuelLevel(aircraftId);
                int consumption = random.nextInt(3) + 1; // 1-3% consumption
                int newFuel = Math.max(0, currentFuel - consumption);
                fuelService.updateFuelLevel(aircraftId, newFuel);
            }
        });
    }

    private void escalateEmergencies() {
        fuelService.getCriticalFuelAircraft().forEach(aircraftId -> {
            fuelService.escalateToEmergency(aircraftId);
            System.out.println("[FUEL MONITOR] Emergency escalated for aircraft: " + aircraftId);
        });
    }

    private int getCurrentFuelLevel(String aircraftId) {
        // Simulate getting current fuel level
        return random.nextInt(100);
    }
}