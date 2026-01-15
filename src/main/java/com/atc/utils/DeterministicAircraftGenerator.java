package com.atc.utils;

import com.atc.core.models.Aircraft;
import com.atc.core.models.AircraftType;
import com.atc.core.models.FlightPlan;
import com.atc.core.SimulationConfig;
import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.*;

public class DeterministicAircraftGenerator {
    private static final DatabaseManager db = DatabaseManager.getInstance();
    
    public static Aircraft createAircraft(String callsign) {
        Document template = db.getAircraftTemplate(callsign);
        if (template == null) {
            throw new IllegalArgumentException("Aircraft not found in database: " + callsign);
        }
        
        AircraftType type = AircraftType.valueOf(template.getString("aircraftType"));
        SimulationConfig config = new SimulationConfig(1000, 1, 800, 30, 120);
        
        return new Aircraft(
            template.getString("callsign"),
            template.getDouble("fuel"),
            template.getDouble("speed"),
            template.getDouble("distance"),
            template.getString("origin"),
            template.getString("destination"),
            config
        );
    }
    
    public static List<Aircraft> createInitialFleet() {
        List<Aircraft> fleet = new ArrayList<>();
        List<Document> templates = db.getAllAircraftTemplates();
        
        for (Document template : templates) {
            fleet.add(createAircraft(template.getString("callsign")));
        }
        return fleet;
    }
    
    public static Set<String> getAvailableFlights() {
        Set<String> flights = new HashSet<>();
        List<Document> templates = db.getAllAircraftTemplates();
        
        for (Document template : templates) {
            flights.add(template.getString("callsign"));
        }
        return flights;
    }
}