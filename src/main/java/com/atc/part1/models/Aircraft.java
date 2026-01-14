package com.atc.part1.models;

import java.time.LocalDateTime;

public class Aircraft {
    private String aircraftId;
    private String callsign;
    private int fuelLevel;
    private String status;
    private boolean isEmergency;
    private int priority;
    private LocalDateTime requestTime;
    private String assignedRunway;

    public Aircraft(String aircraftId, String callsign, int fuelLevel) {
        this.aircraftId = aircraftId;
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.status = "SCHEDULED";
        this.isEmergency = false;
        this.priority = 3;
    }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }
    
    public String getCallsign() { return callsign; }
    public void setCallsign(String callsign) { this.callsign = callsign; }
    
    public int getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(int fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean emergency) { this.isEmergency = emergency; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    
    public String getAssignedRunway() { return assignedRunway; }
    public void setAssignedRunway(String assignedRunway) { this.assignedRunway = assignedRunway; }
}
