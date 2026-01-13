package com.atc.part2.models;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Predicate;
import java.util.Comparator;

public class Flight {
    private String flightId;
    private String flightNumber;
    private String aircraftId;
    private String origin;
    private String destination;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private String status;
    private int delayMinutes;
    private String delayReason;
    private List<String> weatherAlerts;
    private boolean isAffectedByWeather;

    public Flight(String flightId, String flightNumber, String aircraftId, 
                  String origin, String destination, LocalDateTime scheduledDeparture) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.aircraftId = aircraftId;
        this.origin = origin;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.status = "SCHEDULED";
        this.delayMinutes = 0;
        this.weatherAlerts = new ArrayList<>();
        this.isAffectedByWeather = false;
    }

    // Stream processing predicates
    public static final Predicate<Flight> IS_DELAYED = flight -> flight.delayMinutes > 0;
    public static final Predicate<Flight> IS_WEATHER_AFFECTED = flight -> flight.isAffectedByWeather;
    public static final Predicate<Flight> IS_CANCELLED = flight -> "CANCELLED".equals(flight.status);
    public static final Predicate<Flight> IS_IN_FLIGHT = flight -> "IN_FLIGHT".equals(flight.status);
    
    // Stream processing methods
    public static List<Flight> filterDelayed(List<Flight> flights) {
        return flights.stream().filter(IS_DELAYED).collect(Collectors.toList());
    }
    
    public static List<Flight> filterByOrigin(List<Flight> flights, String origin) {
        return flights.stream().filter(f -> origin.equals(f.origin)).collect(Collectors.toList());
    }
    
    public static Map<String, List<Flight>> groupByStatus(List<Flight> flights) {
        return flights.stream().collect(Collectors.groupingBy(Flight::getStatus));
    }
    
    public static double getAverageDelay(List<Flight> flights) {
        return flights.stream().mapToInt(Flight::getDelayMinutes).average().orElse(0.0);
    }
    
    public static List<Flight> sortByDelay(List<Flight> flights) {
        return flights.stream().sorted(Comparator.comparingInt(Flight::getDelayMinutes).reversed()).collect(Collectors.toList());
    }

    // Getters and setters
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    
    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }
    
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(LocalDateTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }
    
    public LocalDateTime getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(LocalDateTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }
    
    public LocalDateTime getActualDeparture() { return actualDeparture; }
    public void setActualDeparture(LocalDateTime actualDeparture) { this.actualDeparture = actualDeparture; }
    
    public LocalDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }
    
    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }
    
    public List<String> getWeatherAlerts() { return weatherAlerts; }
    public void setWeatherAlerts(List<String> weatherAlerts) { this.weatherAlerts = weatherAlerts; }
    
    public boolean isAffectedByWeather() { return isAffectedByWeather; }
    public void setAffectedByWeather(boolean affectedByWeather) { this.isAffectedByWeather = affectedByWeather; }

    public void addDelay(int minutes, String reason) {
        this.delayMinutes += minutes;
        this.delayReason = reason;
        if ("SCHEDULED".equals(this.status)) {
            this.status = "DELAYED";
        }
    }

    public void addWeatherAlert(String alertId) {
        if (weatherAlerts == null) weatherAlerts = new ArrayList<>();
        if (!weatherAlerts.contains(alertId)) {
            weatherAlerts.add(alertId);
            this.isAffectedByWeather = true;
        }
    }

    @Override
    public String toString() {
        return String.format("Flight[%s: %s %s->%s, Status: %s]", 
                           flightId, flightNumber, origin, destination, status);
    }
}