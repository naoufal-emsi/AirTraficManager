package com.atc.core.models;

import com.atc.core.SimulationConfig;
import java.time.LocalDateTime;

public class Aircraft {
    public enum Status { APPROACHING, HOLDING, LANDING, LANDED, DIVERTED }
    public enum EmergencyType { NONE, FUEL_LOW, FUEL_CRITICAL, MEDICAL, FIRE, SECURITY, WEATHER_STORM }

    private String id;
    private String callsign;
    private volatile double fuelLevel;
    private double fuelBurnRate;
    private volatile double speedMetersPerSecond;
    private volatile double distanceToAirportMeters;
    private volatile LocalDateTime eta;
    private volatile Status status;
    private volatile EmergencyType emergencyType;
    private volatile String assignedRunway;
    private volatile LocalDateTime lastStateChange;
    private String origin;
    private String destination;
    private SimulationConfig config;
    private volatile double fireTimeRemainingSeconds;
    private static final double FIRE_INITIAL_TIME = 180.0;

    public Aircraft(String callsign, double fuelLevel, double speedMetersPerSecond, double distanceMeters, 
                   String origin, String destination, SimulationConfig config) {
        this.id = java.util.UUID.randomUUID().toString();
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.speedMetersPerSecond = speedMetersPerSecond;
        this.distanceToAirportMeters = distanceMeters;
        this.origin = origin;
        this.destination = destination;
        this.config = config;
        this.status = Status.APPROACHING;
        this.emergencyType = EmergencyType.NONE;
        this.fuelBurnRate = 800.0 + Math.random() * 400.0;
        this.lastStateChange = LocalDateTime.now();
        this.fireTimeRemainingSeconds = FIRE_INITIAL_TIME;
        calculateETA();
    }

    public Aircraft(String callsign, AircraftType type, FlightPlan plan) {
        this.id = java.util.UUID.randomUUID().toString();
        this.callsign = callsign;
        this.fuelLevel = plan.calculateFuelRequired(type);
        this.speedMetersPerSecond = type.getCruiseSpeedMs();
        this.distanceToAirportMeters = plan.getTotalDistanceKm() * 1000;
        this.origin = plan.getOrigin();
        this.destination = plan.getDestination();
        this.config = new SimulationConfig(1000, 1, type.getFuelBurnRateKgPerSecond() * 3600, 30, 120);
        this.status = Status.APPROACHING;
        this.emergencyType = EmergencyType.NONE;
        this.fuelBurnRate = type.getFuelBurnRateKgPerSecond() * 3600;
        this.lastStateChange = LocalDateTime.now();
        this.fireTimeRemainingSeconds = FIRE_INITIAL_TIME;
        calculateETA();
    }

    public synchronized void updateState() {
        updatePosition(1.0);
        checkAndEscalateFuelStatus();
    }

    public synchronized void updatePosition(double timeElapsedSeconds) {
        double distanceTraveled = config.calculateDistanceTraveled(speedMetersPerSecond, timeElapsedSeconds);
        double fuelBurned = config.calculateFuelBurned(fuelBurnRate, timeElapsedSeconds);
        
        distanceToAirportMeters = Math.max(0, distanceToAirportMeters - distanceTraveled);
        fuelLevel = Math.max(0, fuelLevel - fuelBurned);
        
        calculateETA();
    }

    private void calculateETA() {
        if (speedMetersPerSecond > 0 && distanceToAirportMeters > 0) {
            double secondsToArrival = config.calculateETA(distanceToAirportMeters, speedMetersPerSecond);
            eta = LocalDateTime.now().plusSeconds((long)secondsToArrival);
        } else {
            eta = LocalDateTime.now();
        }
    }

    public boolean checkAndEscalateFuelStatus() {
        if (speedMetersPerSecond <= 0 || distanceToAirportMeters <= 0) return false;
        
        double fuelNeeded = config.calculateFuelNeeded(distanceToAirportMeters, speedMetersPerSecond, fuelBurnRate);
        
        if (emergencyType == EmergencyType.NONE && config.isFuelLow(fuelLevel, fuelNeeded)) {
            escalateToFuelLow();
            return true;
        } else if (emergencyType == EmergencyType.FUEL_LOW && config.isFuelCritical(fuelLevel, fuelNeeded)) {
            escalateToFuelCritical();
            return true;
        }
        return false;
    }

    public synchronized void escalateToFuelLow() {
        if (emergencyType == EmergencyType.NONE) {
            emergencyType = EmergencyType.FUEL_LOW;
            speedMetersPerSecond *= 0.9;
            lastStateChange = LocalDateTime.now();
        }
    }

    public synchronized void escalateToFuelCritical() {
        if (emergencyType == EmergencyType.FUEL_LOW || emergencyType == EmergencyType.NONE) {
            emergencyType = EmergencyType.FUEL_CRITICAL;
            lastStateChange = LocalDateTime.now();
        }
    }

    public synchronized void declareEmergency(EmergencyType type) {
        this.emergencyType = type;
        if (type == EmergencyType.FIRE) {
            this.fireTimeRemainingSeconds = FIRE_INITIAL_TIME;
            // Fire emergency: increase speed to reach airport faster
            this.speedMetersPerSecond *= 1.3; // 30% faster
            // Higher speed means higher fuel burn
            this.fuelBurnRate *= 1.5; // 50% more fuel consumption
        }
        lastStateChange = LocalDateTime.now();
    }

    public double getThreatTimeSeconds() {
        switch (emergencyType) {
            case FIRE:
                return fireTimeRemainingSeconds;
            case FUEL_CRITICAL:
            case FUEL_LOW:
                if (speedMetersPerSecond <= 0 || fuelBurnRate <= 0) return Double.MAX_VALUE;
                double fuelTime = (fuelLevel / fuelBurnRate) * 3600;
                double runwayTime = distanceToAirportMeters / speedMetersPerSecond;
                return Math.min(fuelTime, runwayTime);
            default:
                if (speedMetersPerSecond <= 0) return Double.MAX_VALUE;
                return distanceToAirportMeters / speedMetersPerSecond;
        }
    }

    public synchronized void decreaseFireTimer(double seconds) {
        if (emergencyType == EmergencyType.FIRE) {
            fireTimeRemainingSeconds = Math.max(0, fireTimeRemainingSeconds - seconds);
        }
    }

    public boolean needsImmediateLanding() {
        return emergencyType == EmergencyType.FIRE || 
               emergencyType == EmergencyType.FUEL_CRITICAL ||
               emergencyType == EmergencyType.MEDICAL;
    }

    public String getId() { return id; }
    public String getCallsign() { return callsign; }
    public double getFuelLevel() { return fuelLevel; }
    public double getFuelBurnRate() { return fuelBurnRate; }
    public double getSpeed() { return speedMetersPerSecond; }
    public double getDistanceToAirport() { return distanceToAirportMeters; }
    public LocalDateTime getEta() { return eta; }
    public Status getStatus() { return status; }
    public Status getCurrentStatus() { return status; }
    public EmergencyType getEmergencyType() { return emergencyType; }
    public String getAssignedRunway() { return assignedRunway; }
    public LocalDateTime getLastStateChange() { return lastStateChange; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public boolean isEmergency() { return emergencyType != EmergencyType.NONE; }
    public double getFireTimeRemaining() { return fireTimeRemainingSeconds; }

    public synchronized void setFuelLevel(double fuelLevel) { this.fuelLevel = fuelLevel; }
    public synchronized void setSpeed(double speed) { this.speedMetersPerSecond = speed; }
    public synchronized void setStatus(Status status) { this.status = status; this.lastStateChange = LocalDateTime.now(); }
    public void setCurrentStatus(Status status) { setStatus(status); }
    public synchronized void setEmergencyType(EmergencyType type) { this.emergencyType = type; }
    public synchronized void setAssignedRunway(String runway) { this.assignedRunway = runway; this.lastStateChange = LocalDateTime.now(); }
    public void setLastStateChange(LocalDateTime time) { this.lastStateChange = time; }
    public void setEmergency(boolean emergency) { if (emergency && emergencyType == EmergencyType.NONE) emergencyType = EmergencyType.FUEL_CRITICAL; }
    public void setDistanceToAirport(double distance) { this.distanceToAirportMeters = distance; }
}
