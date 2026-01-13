package com.atc.part2.models;

import java.time.LocalDateTime;

public class FuelAlert {
    private String alertId;
    private String aircraftId;
    private String flightId;
    private int currentFuelLevel;
    private int criticalThreshold;
    private int lowThreshold;
    private LocalDateTime alertTime;
    private String alertLevel;
    private boolean isResolved;
    private String resolution;
    private boolean escalatedToEmergency;

    public FuelAlert(String alertId, String aircraftId, int currentFuelLevel, String alertLevel) {
        this.alertId = alertId;
        this.aircraftId = aircraftId;
        this.currentFuelLevel = currentFuelLevel;
        this.alertLevel = alertLevel;
        this.alertTime = LocalDateTime.now();
        this.isResolved = false;
        this.escalatedToEmergency = false;
        this.criticalThreshold = 10;
        this.lowThreshold = 20;
    }

    // Getters and setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    
    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }
    
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    
    public int getCurrentFuelLevel() { return currentFuelLevel; }
    public void setCurrentFuelLevel(int currentFuelLevel) { this.currentFuelLevel = currentFuelLevel; }
    
    public int getCriticalThreshold() { return criticalThreshold; }
    public void setCriticalThreshold(int criticalThreshold) { this.criticalThreshold = criticalThreshold; }
    
    public int getLowThreshold() { return lowThreshold; }
    public void setLowThreshold(int lowThreshold) { this.lowThreshold = lowThreshold; }
    
    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { this.isResolved = resolved; }
    
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    
    public boolean isEscalatedToEmergency() { return escalatedToEmergency; }
    public void setEscalatedToEmergency(boolean escalatedToEmergency) { this.escalatedToEmergency = escalatedToEmergency; }

    public void resolve(String resolution) {
        this.isResolved = true;
        this.resolution = resolution;
    }

    public void escalateToEmergency() {
        this.escalatedToEmergency = true;
        this.alertLevel = "EMERGENCY";
    }
}