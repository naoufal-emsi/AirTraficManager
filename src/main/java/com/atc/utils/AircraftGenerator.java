package com.atc.utils;

import com.atc.core.models.Aircraft;
import java.util.*;

public class AircraftGenerator {
    private static final String[] AIRLINES = {"UAL", "DAL", "AAL", "SWA", "JBU", "ASA", "FFT", "SKW"};
    private static final String[] AIRPORTS = {"JFK", "LAX", "ORD", "DFW", "ATL", "DEN", "SFO", "SEA"};
    private static final Random random = new Random();

    public static Aircraft generateRandomAircraft() {
        String airline = AIRLINES[random.nextInt(AIRLINES.length)];
        int flightNumber = 100 + random.nextInt(9900);
        String callsign = airline + flightNumber;
        
        double fuel = 50 + random.nextDouble() * 150;
        int speed = 400 + random.nextInt(200);
        double distance = 50 + random.nextDouble() * 500;
        
        String origin = AIRPORTS[random.nextInt(AIRPORTS.length)];
        String destination = AIRPORTS[random.nextInt(AIRPORTS.length)];
        
        return new Aircraft(callsign, fuel, speed, distance, origin, destination);
    }

    public static Aircraft generateEmergencyAircraft(Aircraft.EmergencyType type) {
        Aircraft aircraft = generateRandomAircraft();
        
        switch(type) {
            case FUEL_LOW:
                aircraft.setFuelLevel(30);
                break;
            case FUEL_CRITICAL:
                aircraft.setFuelLevel(15);
                break;
            case FIRE:
            case MEDICAL:
            case SECURITY:
                aircraft.setFuelLevel(80 + random.nextDouble() * 20);
                break;
        }
        
        aircraft.declareEmergency(type);
        return aircraft;
    }
}
