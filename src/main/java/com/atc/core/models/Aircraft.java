package com.atc.core.models;

import java.time.LocalDateTime;

public class Aircraft {
    public enum Status { APPROACHING, HOLDING, LANDING, LANDED, DIVERTED }
    public enum EmergencyType { NONE, FUEL_LOW, FUEL_CRITICAL, MEDICAL, FIRE, SECURITY, WEATHER_STORM }

    private String id;
    private String callsign;
    private double fuelLevel;
    private double fuelBurnRate;
    private int speed;
    private double distanceToAirport;
    private LocalDateTime eta;
    private Status status;
    private EmergencyType emergencyType;
    private int priority;
    private String assignedRunway;
    private LocalDateTime lastStateChange;
    private String origin;
    private String destination;

    public Aircraft(String callsign, double fuelLevel, int speed, double distance, String origin, String destination) {
        this.id = java.util.UUID.randomUUID().toString();
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.speed = speed;
        this.distanceToAirport = distance;
        this.origin = origin;
        this.destination = destination;
        this.status = Status.APPROACHING;
        this.emergencyType = EmergencyType.NONE;
        this.priority = 100;
        this.fuelBurnRate = 0.5 + Math.random() * 0.5;
        this.lastStateChange = LocalDateTime.now();
        calculateETA();
    }

    public void updatePosition(double timeElapsed) {
        distanceToAirport -= (speed * timeElapsed / 3600.0);
        fuelLevel -= (fuelBurnRate * timeElapsed / 60.0);
        if (distanceToAirport < 0) distanceToAirport = 0;
        if (fuelLevel < 0) fuelLevel = 0;
        calculateETA();
        checkFuelStatus();
    }

    private void calculateETA() {
        if (speed > 0 && distanceToAirport > 0) {
            double hoursToArrival = distanceToAirport / speed;
            eta = LocalDateTime.now().plusMinutes((long)(hoursToArrival * 60));
        } else {
            eta = LocalDateTime.now();
        }
    }

    private void checkFuelStatus() {
        double fuelNeeded = (distanceToAirport / speed) * fuelBurnRate * 60 + 30;
        if (emergencyType == EmergencyType.NONE && fuelLevel < fuelNeeded * 1.2) {
            escalateToFuelLow();
        } else if (emergencyType == EmergencyType.FUEL_LOW && fuelLevel < fuelNeeded * 1.05) {
            escalateToFuelCritical();
        }
    }

    public void escalateToFuelLow() {
        emergencyType = EmergencyType.FUEL_LOW;
        priority = 50;
        speed = (int)(speed * 0.9);
        lastStateChange = LocalDateTime.now();
    }

    public void escalateToFuelCritical() {
        emergencyType = EmergencyType.FUEL_CRITICAL;
        priority = 10;
        lastStateChange = LocalDateTime.now();
    }

    public void declareEmergency(EmergencyType type) {
        this.emergencyType = type;
        switch(type) {
            case FIRE: priority = 1; break;
            case MEDICAL: priority = 5; break;
            case SECURITY: priority = 8; break;
            case FUEL_CRITICAL: priority = 10; break;
            case FUEL_LOW: priority = 50; break;
            case WEATHER_STORM: priority = 30; break;
            default: priority = 100;
        }
        lastStateChange = LocalDateTime.now();
    }

    public boolean needsImmediateLanding() {
        return emergencyType == EmergencyType.FIRE || 
               emergencyType == EmergencyType.FUEL_CRITICAL ||
               (emergencyType == EmergencyType.MEDICAL && priority < 10);
    }

    // Getters
    public String getId() { return id; }
    public String getCallsign() { return callsign; }
    public double getFuelLevel() { return fuelLevel; }
    public double getFuelBurnRate() { return fuelBurnRate; }
    public int getSpeed() { return speed; }
    public double getDistanceToAirport() { return distanceToAirport; }
    public LocalDateTime getEta() { return eta; }
    public Status getStatus() { return status; }
    public Status getCurrentStatus() { return status; }
    public EmergencyType getEmergencyType() { return emergencyType; }
    public int getPriority() { return priority; }
    public String getAssignedRunway() { return assignedRunway; }
    public LocalDateTime getLastStateChange() { return lastStateChange; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public boolean isEmergency() { return emergencyType != EmergencyType.NONE; }

    // Setters
    public void setFuelLevel(double fuelLevel) { this.fuelLevel = fuelLevel; }
    public void setSpeed(int speed) { this.speed = speed; }
    public void setStatus(Status status) { this.status = status; this.lastStateChange = LocalDateTime.now(); }
    public void setCurrentStatus(Status status) { setStatus(status); }
    public void setEmergencyType(EmergencyType type) { this.emergencyType = type; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setAssignedRunway(String runway) { this.assignedRunway = runway; this.lastStateChange = LocalDateTime.now(); }
    public void setLastStateChange(LocalDateTime time) { this.lastStateChange = time; }
    public void setEmergency(boolean emergency) { if (emergency && emergencyType == EmergencyType.NONE) emergencyType = EmergencyType.FUEL_CRITICAL; }
}
