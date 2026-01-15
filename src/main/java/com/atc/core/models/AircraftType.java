package com.atc.core.models;

public enum AircraftType {
    B737(150, 5500, 0.8, 2.5, 1.8),
    A320(160, 6400, 0.82, 2.8, 2.0),
    B777(250, 11000, 0.84, 3.2, 2.2),
    A330(230, 11750, 0.82, 3.0, 2.1),
    B787(210, 15200, 0.85, 2.9, 2.0);
    
    private final double cruiseSpeedMs;
    private final double fuelCapacityKg;
    private final double fuelBurnRateKgPerSecond;
    private final double climbRateMs;
    private final double descentRateMs;
    
    AircraftType(double cruiseSpeedMs, double fuelCapacityKg, double fuelBurnRateKgPerSecond, 
                 double climbRateMs, double descentRateMs) {
        this.cruiseSpeedMs = cruiseSpeedMs;
        this.fuelCapacityKg = fuelCapacityKg;
        this.fuelBurnRateKgPerSecond = fuelBurnRateKgPerSecond;
        this.climbRateMs = climbRateMs;
        this.descentRateMs = descentRateMs;
    }
    
    public double getCruiseSpeedMs() { return cruiseSpeedMs; }
    public double getFuelCapacityKg() { return fuelCapacityKg; }
    public double getFuelBurnRateKgPerSecond() { return fuelBurnRateKgPerSecond; }
    public double getClimbRateMs() { return climbRateMs; }
    public double getDescentRateMs() { return descentRateMs; }
}