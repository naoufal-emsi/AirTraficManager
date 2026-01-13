package com.atc.part2.test;

import com.atc.part2.models.Flight;
import com.atc.part2.services.*;
import com.atc.part2.dao.FlightDAO;
import com.atc.shared.database.DatabaseManager;
import java.time.LocalDateTime;
import java.util.List;

public class Part2Test {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Testing Part 2 Components...");
            
            // Initialize database
            DatabaseManager.connect();
            
            // Test services
            testServices();
            
            // Test streams/lambdas
            testStreamsAndLambdas();
            
            System.out.println("✅ All Part 2 tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.disconnect();
        }
    }
    
    private static void testServices() {
        System.out.println("\n📋 Testing Services...");
        
        NotificationService notificationService = new NotificationService();
        FlightDAO flightDAO = new FlightDAO();
        WeatherService weatherService = new WeatherService(flightDAO, notificationService);
        FuelMonitoringService fuelService = new FuelMonitoringService(notificationService);
        
        // Test weather service
        var alert = weatherService.createWeatherAlert("STORM", "HIGH", "JFK");
        System.out.println("✓ Weather alert created: " + alert.getAlertId());
        
        // Test fuel service
        fuelService.updateFuelLevel("AC001", 15);
        var lowFuelAircraft = fuelService.getLowFuelAircraft();
        System.out.println("✓ Low fuel aircraft detected: " + lowFuelAircraft.size());
        
        notificationService.shutdown();
    }
    
    private static void testStreamsAndLambdas() {
        System.out.println("\n🌊 Testing Streams & Lambdas...");
        
        // Create test flights
        List<Flight> flights = List.of(
            new Flight("FL001", "AA100", "AC001", "JFK", "LAX", LocalDateTime.now()),
            new Flight("FL002", "UA200", "AC002", "LAX", "JFK", LocalDateTime.now()),
            new Flight("FL003", "BA300", "AC003", "LHR", "JFK", LocalDateTime.now())
        );
        
        // Add delays to some flights
        flights.get(0).addDelay(30, "WEATHER");
        flights.get(2).addDelay(45, "WEATHER");
        
        // Test stream operations
        var delayedFlights = Flight.filterDelayed(flights);
        System.out.println("✓ Delayed flights found: " + delayedFlights.size());
        
        var flightsByStatus = Flight.groupByStatus(flights);
        System.out.println("✓ Flights grouped by status: " + flightsByStatus.keySet());
        
        var averageDelay = Flight.getAverageDelay(flights);
        System.out.println("✓ Average delay calculated: " + averageDelay + " minutes");
        
        var sortedByDelay = Flight.sortByDelay(flights);
        System.out.println("✓ Flights sorted by delay: " + sortedByDelay.size());
    }
}