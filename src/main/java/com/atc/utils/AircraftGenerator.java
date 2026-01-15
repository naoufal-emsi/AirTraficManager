package com.atc.utils;

import com.atc.database.DatabaseManager;

public class AircraftGenerator {
    
    public static String generateRealisticAircraft() {
        return DatabaseManager.getInstance().generateAndInsertRealisticFlight();
    }
    
    public static void generateEmergencyAircraft(String emergencyType) {
        String callsign = generateRealisticAircraft();
        if (callsign != null) {
            DatabaseManager.getInstance().updateActiveAircraft(callsign, 
                new org.bson.Document("emergency", emergencyType)
                    .append("priority", emergencyType.equals("FUEL_CRITICAL") ? 1 : 50)
                    .append("fuel", emergencyType.equals("FUEL_CRITICAL") ? 1500 : 3000)
            );
        }
    }
}
