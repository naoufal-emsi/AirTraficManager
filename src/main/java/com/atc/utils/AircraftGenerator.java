package com.atc.utils;

import com.atc.core.models.Aircraft;
import com.atc.core.SimulationConfig;
import java.util.*;

public class AircraftGenerator {
    private static final String[] AIRLINES = {"UAL", "DAL", "AAL", "SWA", "JBU", "ASA", "FFT", "SKW"};
    private static final String[] AIRPORTS = {"JFK", "LAX", "ORD", "DFW", "ATL", "DEN", "SFO", "SEA"};
    private static final Random random = new Random();

    public static Aircraft generateRandomAircraft(SimulationConfig config) {
        String airline = AIRLINES[random.nextInt(AIRLINES.length)];
        int flightNumber = 100 + random.nextInt(9900);
        String callsign = airline + flightNumber;
        
        double fuel = 5000 + random.nextDouble() * 10000;
        double speedMetersPerSecond = 100 + random.nextDouble() * 100;
        double distanceMeters = config.getMetersPerTimeUnit() * (50 + random.nextDouble() * 200);
        
        String origin = AIRPORTS[random.nextInt(AIRPORTS.length)];
        String destination = AIRPORTS[random.nextInt(AIRPORTS.length)];
        
        return new Aircraft(callsign, fuel, speedMetersPerSecond, distanceMeters, origin, destination, config);
    }

    public static Aircraft generateEmergencyAircraft(Aircraft.EmergencyType type, SimulationConfig config) {
        Aircraft aircraft = generateRandomAircraft(config);
        
        switch(type) {
            case FUEL_LOW:
                aircraft.setFuelLevel(3000);
                break;
            case FUEL_CRITICAL:
                aircraft.setFuelLevel(1500);
                break;
            case FIRE:
            case MEDICAL:
            case SECURITY:
                aircraft.setFuelLevel(8000 + random.nextDouble() * 2000);
                break;
        }
        
        aircraft.declareEmergency(type);
        return aircraft;
    }
}
