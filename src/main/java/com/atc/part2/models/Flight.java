package com.atc.part2.models;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class Flight {
    // Fields
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

    // Constructors
    public Flight(String flightId, String flightNumber, String aircraftId, 
                  String origin, String destination, LocalDateTime scheduledDeparture) {
        // TODO: Initialize all fields
    }

    public Flight(String flightId, String flightNumber, String origin, String destination) {
        // TODO: Initialize basic fields
    }

    // Getters and Setters
    public String getFlightId() {
        // TODO: Return flightId
        return null;
    }

    public void setFlightId(String flightId) {
        // TODO: Set flightId
    }

    public String getFlightNumber() {
        // TODO: Return flightNumber
        return null;
    }

    public void setFlightNumber(String flightNumber) {
        // TODO: Set flightNumber
    }

    public String getAircraftId() {
        // TODO: Return aircraftId
        return null;
    }

    public void setAircraftId(String aircraftId) {
        // TODO: Set aircraftId
    }

    public String getOrigin() {
        // TODO: Return origin
        return null;
    }

    public void setOrigin(String origin) {
        // TODO: Set origin
    }

    public String getDestination() {
        // TODO: Return destination
        return null;
    }

    public void setDestination(String destination) {
        // TODO: Set destination
    }

    public LocalDateTime getScheduledDeparture() {
        // TODO: Return scheduledDeparture
        return null;
    }

    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        // TODO: Set scheduledDeparture
    }

    public LocalDateTime getScheduledArrival() {
        // TODO: Return scheduledArrival
        return null;
    }

    public void setScheduledArrival(LocalDateTime scheduledArrival) {
        // TODO: Set scheduledArrival
    }

    public LocalDateTime getActualDeparture() {
        // TODO: Return actualDeparture
        return null;
    }

    public void setActualDeparture(LocalDateTime actualDeparture) {
        // TODO: Set actualDeparture
    }

    public LocalDateTime getActualArrival() {
        // TODO: Return actualArrival
        return null;
    }

    public void setActualArrival(LocalDateTime actualArrival) {
        // TODO: Set actualArrival
    }

    public String getStatus() {
        // TODO: Return status
        return null;
    }

    public void setStatus(String status) {
        // TODO: Set status
    }

    public int getDelayMinutes() {
        // TODO: Return delayMinutes
        return 0;
    }

    public void setDelayMinutes(int delayMinutes) {
        // TODO: Set delayMinutes
    }

    public String getDelayReason() {
        // TODO: Return delayReason
        return null;
    }

    public void setDelayReason(String delayReason) {
        // TODO: Set delayReason
    }

    public List<String> getWeatherAlerts() {
        // TODO: Return weatherAlerts
        return null;
    }

    public void setWeatherAlerts(List<String> weatherAlerts) {
        // TODO: Set weatherAlerts
    }

    public boolean isAffectedByWeather() {
        // TODO: Return isAffectedByWeather
        return false;
    }

    public void setAffectedByWeather(boolean affectedByWeather) {
        // TODO: Set isAffectedByWeather
    }

    // Business Methods
    public boolean isDelayed() {
        // TODO: Return delayMinutes > 0
        return false;
    }

    public boolean isScheduledToday() {
        // TODO: Check if scheduled for today
        return false;
    }

    public void addDelay(int minutes, String reason) {
        // TODO: Add delay and reason
    }

    public void cancelFlight(String reason) {
        // TODO: Set status to CANCELLED
    }

    public Duration getFlightDuration() {
        // TODO: Calculate flight duration
        return null;
    }

    public boolean isDeparted() {
        // TODO: Check if actualDeparture is set
        return false;
    }

    public boolean hasLanded() {
        // TODO: Check if actualArrival is set
        return false;
    }

    public void addWeatherAlert(String alertId) {
        // TODO: Add weather alert to list
    }

    @Override
    public String toString() {
        // TODO: Format: "Flight[FL001: BA101 JFK->LHR, Status: SCHEDULED]"
        return null;
    }
}