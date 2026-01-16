package com.atc.controllers;

import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import org.bson.Document;

public class EmergencyController {

    public EmergencyController() {
    }

    public void declareEmergency(String callsign, String emergencyType) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        int priority = switch(emergencyType) {
            case "FIRE" -> 1;
            case "MEDICAL" -> 5;
            case "SECURITY" -> 8;
            case "FUEL_CRITICAL" -> 10;
            case "FUEL_LOW" -> 50;
            case "WEATHER_STORM" -> 30;
            default -> 100;
        };
        
        Document updates = new Document("emergency", emergencyType)
            .append("priority", priority);
        
        if ("FIRE".equals(emergencyType)) {
            updates.append("fireTimeRemaining", 180.0);
        }
        
        dbManager.updateActiveAircraft(callsign, updates);
        
        String message = switch(emergencyType) {
            case "FIRE" -> "MAYDAY MAYDAY - Fire indication, requesting immediate landing";
            case "MEDICAL" -> "PAN-PAN - Medical emergency, requesting priority landing";
            case "SECURITY" -> "Security threat onboard, discrete emergency code set";
            case "FUEL_CRITICAL" -> "MAYDAY - Fuel critical, minimum fuel declared";
            case "FUEL_LOW" -> "Requesting priority vectors, fuel below planned margin";
            case "WEATHER_STORM" -> "Unable to continue approach due to weather, requesting diversion";
            default -> "Emergency declared";
        };
        
        dbManager.saveEmergencyEvent(callsign, message);
        AirTrafficSystem.updateGUI();
    }

    public void handleDiversion(String callsign, String reason) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        Document updates = new Document("status", "DIVERTED");
        dbManager.updateActiveAircraft(callsign, updates);
        dbManager.saveEmergencyEvent(callsign, "Aircraft diverted: " + reason);
    }
}
