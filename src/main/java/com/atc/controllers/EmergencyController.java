package com.atc.controllers;

import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import org.bson.Document;
import java.util.List;

public class EmergencyController {

    public EmergencyController() {
    }

    public void declareEmergency(String callsign, String emergencyType) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        Document updates = new Document("emergency", emergencyType);
        
        if ("FIRE".equals(emergencyType)) {
            updates.append("fireTimeRemaining", 180.0);
            
            // Get current aircraft data to modify speed and fuel burn
            List<Document> aircraft = dbManager.getAllActiveAircraft();
            for (Document ac : aircraft) {
                if (callsign.equals(ac.getString("callsign"))) {
                    double currentSpeed = ac.getDouble("speed");
                    double currentBurnRate = ac.getDouble("fuelBurnRate");
                    
                    // Increase speed by 30% and fuel burn by 50%
                    updates.append("speed", currentSpeed * 1.3);
                    updates.append("fuelBurnRate", currentBurnRate * 1.5);
                    break;
                }
            }
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
