package com.atc.controllers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import java.util.List;

public class EmergencyController {
    private final List<Aircraft> activeAircraft;

    public EmergencyController(List<Aircraft> aircraft) {
        this.activeAircraft = aircraft;
    }

    public void declareEmergency(Aircraft aircraft, Aircraft.EmergencyType type) {
        aircraft.declareEmergency(type);
        
        String message = switch(type) {
            case FIRE -> "MAYDAY MAYDAY - Fire indication, requesting immediate landing";
            case MEDICAL -> "PAN-PAN - Medical emergency, requesting priority landing";
            case SECURITY -> "Security threat onboard, discrete emergency code set";
            case FUEL_CRITICAL -> "MAYDAY - Fuel critical, minimum fuel declared";
            case FUEL_LOW -> "Requesting priority vectors, fuel below planned margin";
            case WEATHER_STORM -> "Unable to continue approach due to weather, requesting diversion";
            default -> "Emergency declared";
        };
        
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, message);
    }

    public void handleDiversion(Aircraft aircraft, String reason) {
        aircraft.setStatus(Aircraft.Status.DIVERTED);
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "Aircraft diverted: " + reason);
    }
}
