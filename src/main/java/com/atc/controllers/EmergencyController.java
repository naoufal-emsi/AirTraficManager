package com.atc.controllers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class EmergencyController {
    private final List<Aircraft> activeAircraft;
    private final PriorityBlockingQueue<Aircraft> landingQueue;

    public EmergencyController(List<Aircraft> aircraft, PriorityBlockingQueue<Aircraft> queue) {
        this.activeAircraft = aircraft;
        this.landingQueue = queue;
    }

    public void declareEmergency(Aircraft aircraft, Aircraft.EmergencyType type) {
        synchronized(landingQueue) {
            landingQueue.remove(aircraft);
            aircraft.declareEmergency(type);
            landingQueue.offer(aircraft);
        }
        
        String message = switch(type) {
            case FIRE -> "MAYDAY MAYDAY - Fire indication, requesting immediate landing";
            case MEDICAL -> "PAN-PAN - Medical emergency, requesting priority landing";
            case SECURITY -> "Security threat onboard, discrete emergency code set";
            case FUEL_CRITICAL -> "MAYDAY - Fuel critical, minimum fuel declared";
            case FUEL_LOW -> "Requesting priority vectors, fuel below planned margin";
            case WEATHER_STORM -> "Unable to continue approach due to weather, requesting diversion";
            default -> "Emergency declared";
        };
        
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, message));
        AirTrafficSystem.updateGUI();
    }

    public void handleDiversion(Aircraft aircraft, String reason) {
        aircraft.setStatus(Aircraft.Status.DIVERTED);
        CompletableFuture.runAsync(() -> 
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "Aircraft diverted: " + reason));
    }
}
