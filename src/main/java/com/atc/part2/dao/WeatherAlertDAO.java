package com.atc.part2.dao;

import com.atc.part2.models.WeatherAlert;
// TODO: Import DatabaseManager from shared package when available
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WeatherAlertDAO {
    // TODO: Add MongoDB collection field when DatabaseManager is available
    // private final MongoCollection<Document> alertCollection;

    // Constructor
    public WeatherAlertDAO(Object dbManager) { // TODO: Change to DatabaseManager when available
        // TODO: Initialize MongoDB collection
        // this.alertCollection = dbManager.getCollection("weather_alerts");
    }

    // CRUD OPERATIONS
    public void saveWeatherAlert(WeatherAlert alert) {
        // TODO: Insert weather alert document to MongoDB
        // - Convert WeatherAlert object to Document
        // - Insert into weather_alerts collection
        // - Handle any errors
    }

    public WeatherAlert findAlertById(String alertId) {
        // TODO: Find weather alert by alertId
        // - Query MongoDB by alertId
        // - Convert Document to WeatherAlert object
        // - Return null if not found
        return null;
    }

    public List<WeatherAlert> findAllAlerts() {
        // TODO: Get all weather alerts from MongoDB
        // - Query all documents in weather_alerts collection
        // - Convert each Document to WeatherAlert object
        // - Return as List
        return null;
    }

    public void updateAlertStatus(String alertId, boolean isActive) {
        // TODO: Update alert active status
        // - Find alert by ID
        // - Update isActive field
        // - Update updatedAt timestamp
    }

    public void deleteAlert(String alertId) {
        // TODO: Remove weather alert from MongoDB
        // - Delete document by alertId
        // - Handle any errors
    }

    // QUERIES
    public List<WeatherAlert> findActiveAlerts() {
        // TODO: Find active weather alerts
        // - Query MongoDB where isActive = true
        // - Convert results to WeatherAlert objects
        return null;
    }

    public List<WeatherAlert> findAlertsByAirport(String airport) {
        // TODO: Filter alerts by airport
        // - Query MongoDB with affectedAirport filter
        // - Convert results to WeatherAlert objects
        return null;
    }

    public List<WeatherAlert> findAlertsBySeverity(String severity) {
        // TODO: Filter alerts by severity
        // - Query MongoDB with severity filter
        // - Convert results to WeatherAlert objects
        return null;
    }

    public List<WeatherAlert> findAlertsByType(String type) {
        // TODO: Filter alerts by alert type
        // - Query MongoDB with alertType filter
        // - Convert results to WeatherAlert objects
        return null;
    }

    public List<WeatherAlert> findAlertsInTimeRange(LocalDateTime start, LocalDateTime end) {
        // TODO: Find alerts in time range
        // - Query MongoDB with startTime/endTime range filter
        // - Convert results to WeatherAlert objects
        return null;
    }

    // STATISTICS
    public long countActiveAlerts() {
        // TODO: Count active weather alerts
        // - Use MongoDB count query with isActive = true filter
        return 0;
    }

    public Map<String, Long> getAlertCountBySeverity() {
        // TODO: Get alert counts by severity level
        // - Use MongoDB aggregation to group by severity
        // - Count alerts per severity level
        return null;
    }

    public Map<String, Long> getAlertCountByType() {
        // TODO: Get alert counts by alert type
        // - Use MongoDB aggregation to group by alertType
        // - Count alerts per type
        return null;
    }

    public List<WeatherAlert> getMostRecentAlerts(int limit) {
        // TODO: Get most recent weather alerts
        // - Query MongoDB sorted by startTime descending
        // - Limit results to specified number
        return null;
    }

    // Helper Methods
    private WeatherAlert documentToWeatherAlert(Object document) { // TODO: Change to Document when MongoDB driver available
        // TODO: Convert MongoDB Document to WeatherAlert object
        return null;
    }

    private Object weatherAlertToDocument(WeatherAlert alert) { // TODO: Change to Document when MongoDB driver available
        // TODO: Convert WeatherAlert object to MongoDB Document
        return null;
    }
}