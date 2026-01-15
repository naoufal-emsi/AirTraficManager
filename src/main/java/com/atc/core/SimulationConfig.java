package com.atc.core;

public class SimulationConfig {
    private double metersPerTimeUnit;
    private double timeStepSeconds;
    private double fuelBurnRatePerHour;
    private double fuelReserveMinutes;
    private double landingDurationSeconds;
    private double fuelLowThresholdMultiplier = 1.2;
    private double fuelCriticalThresholdMultiplier = 1.05;
    
    public SimulationConfig(double metersPerTimeUnit, double timeStepSeconds, double fuelBurnRatePerHour, 
                           double fuelReserveMinutes, double landingDurationSeconds) {
        this.metersPerTimeUnit = metersPerTimeUnit;
        this.timeStepSeconds = timeStepSeconds;
        this.fuelBurnRatePerHour = fuelBurnRatePerHour;
        this.fuelReserveMinutes = fuelReserveMinutes;
        this.landingDurationSeconds = landingDurationSeconds;
    }
    
    public double calculateDistanceTraveled(double speedMetersPerSecond, double timeSeconds) {
        return speedMetersPerSecond * timeSeconds;
    }
    
    public double calculateFuelBurned(double burnRatePerHour, double timeSeconds) {
        return (burnRatePerHour / 3600.0) * timeSeconds;
    }
    
    public double calculateETA(double distanceMeters, double speedMetersPerSecond) {
        if (speedMetersPerSecond <= 0) return 0;
        return distanceMeters / speedMetersPerSecond;
    }
    
    public double calculateFuelNeeded(double distanceMeters, double speedMetersPerSecond, double burnRate) {
        double flightTimeSeconds = distanceMeters / speedMetersPerSecond;
        double fuelForFlight = (burnRate / 3600.0) * flightTimeSeconds;
        double fuelForReserve = (fuelReserveMinutes / 60.0) * burnRate;
        return fuelForFlight + fuelForReserve;
    }
    
    public boolean isFuelLow(double currentFuel, double fuelNeeded) {
        return currentFuel < (fuelNeeded * fuelLowThresholdMultiplier);
    }
    
    public boolean isFuelCritical(double currentFuel, double fuelNeeded) {
        return currentFuel < (fuelNeeded * fuelCriticalThresholdMultiplier);
    }
    
    public double getMetersPerTimeUnit() { return metersPerTimeUnit; }
    public double getTimeStepSeconds() { return timeStepSeconds; }
    public double getFuelBurnRatePerHour() { return fuelBurnRatePerHour; }
    public double getFuelReserveMinutes() { return fuelReserveMinutes; }
    public double getLandingDurationSeconds() { return landingDurationSeconds; }
}
