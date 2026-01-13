package com.atc.part2.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class WeatherAlert {
    private String alertId;
    private String alertType;
    private String severity;
    private String affectedAirport;
    private List<String> affectedRunways;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private boolean isActive;
    private List<String> affectedFlights;

    public WeatherAlert(String alertId, String alertType, String severity, String affectedAirport) {
        this.alertId = alertId;
        this.alertType = alertType;
        this.severity = severity;
        this.affectedAirport = affectedAirport;
        this.startTime = LocalDateTime.now();
        this.isActive = true;
        this.affectedRunways = new ArrayList<>();
        this.affectedFlights = new ArrayList<>();
    }

    // Getters and setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getAffectedAirport() { return affectedAirport; }
    public void setAffectedAirport(String affectedAirport) { this.affectedAirport = affectedAirport; }
    
    public List<String> getAffectedRunways() { return affectedRunways; }
    public void setAffectedRunways(List<String> affectedRunways) { this.affectedRunways = affectedRunways; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public List<String> getAffectedFlights() { return affectedFlights; }
    public void setAffectedFlights(List<String> affectedFlights) { this.affectedFlights = affectedFlights; }

    public void addAffectedFlight(String flightId) {
        if (!affectedFlights.contains(flightId)) {
            affectedFlights.add(flightId);
        }
    }

    public void resolve() {
        this.isActive = false;
        this.endTime = LocalDateTime.now();
    }
}