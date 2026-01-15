package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.database.DatabaseManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmergencyHandlerWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private final List<Runway> runways;
    private volatile boolean running = true;

    public EmergencyHandlerWorker(List<Aircraft> aircraft, List<Runway> runways) {
        this.activeAircraft = aircraft;
        this.runways = runways;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    if (aircraft.isEmergency() && aircraft.needsImmediateLanding()) {
                        reserveEmergencyRunway(aircraft);
                    }
                    if (aircraft.isEmergency()) {
                        handleEmergency(aircraft);
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void reserveEmergencyRunway(Aircraft aircraft) {
        for (Runway runway : runways) {
            if (runway.isAvailableForEmergency()) {
                runway.setEmergencyServicesReady(true);
                CompletableFuture.runAsync(() -> 
                    DatabaseManager.getInstance().saveRunwayEvent(runway, 
                        "Emergency services positioned for " + aircraft.getCallsign()));
                break;
            }
        }
    }

    private void handleEmergency(Aircraft aircraft) {
        switch (aircraft.getEmergencyType()) {
            case FIRE -> handleFire(aircraft);
            case MEDICAL -> handleMedical(aircraft);
            case SECURITY -> handleSecurity(aircraft);
            case FUEL_CRITICAL -> handleFuelCritical(aircraft);
            case WEATHER_STORM -> handleWeatherStorm(aircraft);
        }
    }

    private void handleFire(Aircraft aircraft) {
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "FIRE EMERGENCY - Engine shutdown, fire suppression active, emergency services positioned"));
    }

    private void handleMedical(Aircraft aircraft) {
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "MEDICAL EMERGENCY - PAN-PAN declared, ambulance dispatched"));
    }

    private void handleSecurity(Aircraft aircraft) {
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "SECURITY THREAT - Discrete code set, law enforcement alerted"));
    }

    private void handleFuelCritical(Aircraft aircraft) {
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "FUEL CRITICAL - Direct routing, no holding, immediate landing clearance"));
    }

    private void handleWeatherStorm(Aircraft aircraft) {
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "WEATHER DIVERSION - Storm blocking approach, rerouting to alternate"));
    }

    public void stop() {
        running = false;
    }
}
