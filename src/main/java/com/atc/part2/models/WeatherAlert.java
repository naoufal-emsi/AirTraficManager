package com.atc.part2.models;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

public class WeatherAlert {
    // Fields
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

    // Constructors
    public WeatherAlert(String alertType, String severity, String affectedAirport) {
        // TODO: Initialize basic fields
    }

    public WeatherAlert(String alertType, String severity, String affectedAirport, 
                       LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: Initialize all fields
    }

    // Getters and Setters
    public String getAlertId() {
        // TODO: Return alertId
        return null;
    }

    public void setAlertId(String alertId) {
        // TODO: Set alertId
    }

    public String getAlertType() {
        // TODO: Return alertType
        return null;
    }

    public void setAlertType(String alertType) {
        // TODO: Set alertType
    }

    public String getSeverity() {
        // TODO: Return severity
        return null;
    }

    public void setSeverity(String severity) {
        // TODO: Set severity
    }

    public String getAffectedAirport() {
        // TODO: Return affectedAirport
        return null;
    }

    public void setAffectedAirport(String affectedAirport) {
        // TODO: Set affectedAirport
    }

    public List<String> getAffectedRunways() {
        // TODO: Return affectedRunways
        return null;
    }

    public void setAffectedRunways(List<String> affectedRunways) {
        // TODO: Set affectedRunways
    }

    public LocalDateTime getStartTime() {
        // TODO: Return startTime
        return null;
    }

    public void setStartTime(LocalDateTime startTime) {
        // TODO: Set startTime
    }

    public LocalDateTime getEndTime() {
        // TODO: Return endTime
        return null;
    }

    public void setEndTime(LocalDateTime endTime) {
        // TODO: Set endTime
    }

    public String getDescription() {
        // TODO: Return description
        return null;
    }

    public void setDescription(String description) {
        // TODO: Set description
    }

    public boolean isActive() {
        // TODO: Return isActive
        return false;
    }

    public void setActive(boolean active) {
        // TODO: Set isActive
    }

    public List<String> getAffectedFlights() {
        // TODO: Return affectedFlights
        return null;
    }

    public void setAffectedFlights(List<String> affectedFlights) {
        // TODO: Set affectedFlights
    }

    // Business Methods
    public boolean isCritical() {
        // TODO: Return severity.equals("CRITICAL")
        return false;
    }

    public boolean isCurrentlyActive() {
        // TODO: Check if current time is within start/end
        return false;
    }

    public void activate() {
        // TODO: Set isActive=true, startTime=now
    }

    public void deactivate() {
        // TODO: Set isActive=false, endTime=now
    }

    public void addAffectedFlight(String flightId) {
        // TODO: Add flight to affected list
    }

    public Duration getDuration() {
        // TODO: Calculate alert duration
        return null;
    }

    public boolean affectsRunway(String runwayId) {
        // TODO: Check if runway is affected
        return false;
    }
}