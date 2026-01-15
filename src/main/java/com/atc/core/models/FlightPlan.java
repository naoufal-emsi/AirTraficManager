package com.atc.core.models;

public class FlightPlan {
    private final String origin;
    private final String destination;
    private final double totalDistanceKm;
    private final String[] waypoints;
    private final double[] waypointDistances;
    
    public FlightPlan(String origin, String destination, double totalDistanceKm, 
                     String[] waypoints, double[] waypointDistances) {
        this.origin = origin;
        this.destination = destination;
        this.totalDistanceKm = totalDistanceKm;
        this.waypoints = waypoints;
        this.waypointDistances = waypointDistances;
    }
    
    public double calculateFuelRequired(AircraftType type) {
        double flightTimeSeconds = (totalDistanceKm * 1000) / type.getCruiseSpeedMs();
        double fuelForFlight = flightTimeSeconds * type.getFuelBurnRateKgPerSecond();
        double reserveFuel = type.getFuelCapacityKg() * 0.1; // 10% reserve
        return fuelForFlight + reserveFuel;
    }
    
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public String[] getWaypoints() { return waypoints; }
    public double[] getWaypointDistances() { return waypointDistances; }
}