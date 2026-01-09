package com.atc.part2.dao;

import com.atc.part2.models.Flight;
// TODO: Import DatabaseManager from shared package when available
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FlightDAO {
    // TODO: Add MongoDB collection field when DatabaseManager is available
    // private final MongoCollection<Document> flightCollection;

    // Constructor
    public FlightDAO(Object dbManager) { // TODO: Change to DatabaseManager when available
        // TODO: Initialize MongoDB collection
        // this.flightCollection = dbManager.getCollection("flights");
    }

    // CRUD OPERATIONS
    public void saveFlight(Flight flight) {
        // TODO: Insert flight document to MongoDB
        // - Convert Flight object to Document
        // - Insert into flights collection
        // - Handle any errors
    }

    public Flight findFlightById(String flightId) {
        // TODO: Find flight by flightId
        // - Query MongoDB by flightId
        // - Convert Document to Flight object
        // - Return null if not found
        return null;
    }

    public List<Flight> findAllFlights() {
        // TODO: Get all flights from MongoDB
        // - Query all documents in flights collection
        // - Convert each Document to Flight object
        // - Return as List
        return null;
    }

    public void updateFlightStatus(String flightId, String status) {
        // TODO: Update flight status field
        // - Find flight by ID
        // - Update status field
        // - Update updatedAt timestamp
    }

    public void updateFlightDelay(String flightId, int delayMinutes, String reason) {
        // TODO: Update flight delay information
        // - Update delayMinutes field
        // - Update delayReason field
        // - Update updatedAt timestamp
    }

    public void deleteFlight(String flightId) {
        // TODO: Remove flight from MongoDB
        // - Delete document by flightId
        // - Handle any errors
    }

    // QUERIES WITH STREAMS INTEGRATION
    public List<Flight> findFlightsByStatus(String status) {
        // TODO: Filter flights by status
        // - Query MongoDB with status filter
        // - Convert results to Flight objects
        return null;
    }

    public List<Flight> findDelayedFlights() {
        // TODO: Find flights with delays > 0
        // - Query MongoDB where delayMinutes > 0
        // - Convert results to Flight objects
        return null;
    }

    public List<Flight> findFlightsByOrigin(String origin) {
        // TODO: Filter flights by origin airport
        // - Query MongoDB with origin filter
        // - Convert results to Flight objects
        return null;
    }

    public List<Flight> findFlightsByDestination(String destination) {
        // TODO: Filter flights by destination airport
        // - Query MongoDB with destination filter
        // - Convert results to Flight objects
        return null;
    }

    public List<Flight> findFlightsScheduledToday() {
        // TODO: Find flights scheduled for today
        // - Query MongoDB with date range for today
        // - Convert results to Flight objects
        return null;
    }

    public List<Flight> findFlightsByDateRange(LocalDateTime start, LocalDateTime end) {
        // TODO: Find flights in date range
        // - Query MongoDB with date range filter
        // - Convert results to Flight objects
        return null;
    }

    // STATISTICS QUERIES
    public long countFlightsByStatus(String status) {
        // TODO: Count flights in specific status
        // - Use MongoDB count query with status filter
        return 0;
    }

    public double getAverageDelayMinutes() {
        // TODO: Calculate average delay across all flights
        // - Use MongoDB aggregation pipeline
        // - Calculate average of delayMinutes field
        return 0.0;
    }

    public Map<String, Long> getFlightCountByAirport() {
        // TODO: Get flight counts per airport
        // - Use MongoDB aggregation to group by origin
        // - Count flights per airport
        return null;
    }

    public List<Flight> getTopDelayedFlights(int limit) {
        // TODO: Get most delayed flights
        // - Query MongoDB sorted by delayMinutes descending
        // - Limit results to specified number
        return null;
    }

    // WEATHER-RELATED QUERIES
    public List<Flight> findFlightsAffectedByWeather() {
        // TODO: Find flights with weather delays
        // - Query MongoDB where delayReason = "WEATHER"
        // - Convert results to Flight objects
        return null;
    }

    public void updateFlightWeatherStatus(String flightId, boolean affected) {
        // TODO: Update weather affected status
        // - Update isAffectedByWeather field
        // - Update updatedAt timestamp
    }

    public List<Flight> findFlightsByWeatherAlert(String alertId) {
        // TODO: Find flights affected by specific weather alert
        // - Query MongoDB where weatherAlerts array contains alertId
        // - Convert results to Flight objects
        return null;
    }

    // Helper Methods
    private Flight documentToFlight(Object document) { // TODO: Change to Document when MongoDB driver available
        // TODO: Convert MongoDB Document to Flight object
        return null;
    }

    private Object flightToDocument(Flight flight) { // TODO: Change to Document when MongoDB driver available
        // TODO: Convert Flight object to MongoDB Document
        return null;
    }
}