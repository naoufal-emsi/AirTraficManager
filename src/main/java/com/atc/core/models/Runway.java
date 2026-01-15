package com.atc.core.models;

import java.time.LocalDateTime;

public class Runway {
    private String runwayId;
    private boolean isOpen;
    private String status;
    private Aircraft currentAircraft;
    private LocalDateTime lastUsed;
    private boolean weatherAffected;
    private boolean emergencyServicesReady;

    public Runway(String runwayId) {
        this.runwayId = runwayId;
        this.isOpen = true;
        this.status = "FREE";
        this.currentAircraft = null;
        this.lastUsed = null;
        this.weatherAffected = false;
        this.emergencyServicesReady = true;
    }

    public void assignAircraft(Aircraft aircraft) {
        this.currentAircraft = aircraft;
        this.isOpen = false;
        this.status = "OCCUPIED";
        this.lastUsed = LocalDateTime.now();
        aircraft.setAssignedRunway(this.runwayId);
        if (aircraft.isEmergency()) {
            this.emergencyServicesReady = true;
        }
    }

    public void releaseRunway() {
        this.currentAircraft = null;
        this.isOpen = true;
        this.status = "FREE";
    }

    public boolean isAvailableForEmergency() {
        return isOpen && !weatherAffected;
    }

    // Getters
    public String getRunwayId() { return runwayId; }
    public boolean isIsOpen() { return isOpen; }
    public boolean isOpen() { return isOpen; }
    public String getStatus() { return status; }
    public Aircraft getCurrentAircraft() { return currentAircraft; }
    public LocalDateTime getLastUsed() { return lastUsed; }
    public boolean isWeatherAffected() { return weatherAffected; }
    public boolean isEmergencyServicesReady() { return emergencyServicesReady; }

    // Setters
    public void setIsOpen(boolean open) { this.isOpen = open; }
    public void setStatus(String status) { this.status = status; }
    public void setWeatherAffected(boolean affected) { this.weatherAffected = affected; }
    public void setEmergencyServicesReady(boolean ready) { this.emergencyServicesReady = ready; }
    public void setCurrentAircraft(Aircraft aircraft) { this.currentAircraft = aircraft; }
    public void setLastUsed(LocalDateTime time) { this.lastUsed = time; }
}
