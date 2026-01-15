package com.atc.utils;

import com.atc.database.DatabaseManager;

public class AircraftCreator {
    private static final DatabaseManager db = DatabaseManager.getInstance();
    
    public static void createAircraft(String callsign, String aircraftType, double fuelLevel, 
                                    double speed, double distance, String origin, String destination) {
        db.createAircraft(callsign, aircraftType, fuelLevel, speed, distance, origin, destination);
    }
    
    public static void deleteAircraft(String callsign) {
        db.deleteAircraftTemplate(callsign);
    }
    
    // Quick creation methods
    public static void createSampleAircraft() {
        createAircraft("UAL123", "B777", 15000, 250, 50000, "JFK", "LAX");
        createAircraft("DAL456", "A330", 12000, 230, 35000, "ATL", "SEA");
        createAircraft("SWA101", "B737", 8000, 220, 25000, "DFW", "PHX");
    }
}