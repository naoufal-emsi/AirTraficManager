package com.atc.part1.models;

import java.time.LocalDateTime;

public class Runway {
    private String runwayId;
    private boolean isOpen;
    private String status;
    private String currentAircraft;
    private LocalDateTime lastUsed;

    public Runway(String runwayId) {
        this.runwayId = runwayId;
        this.isOpen = true;
        this.status = "FREE";
    }

    public String getRunwayId() { return runwayId; }
    public void setRunwayId(String runwayId) { this.runwayId = runwayId; }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { this.isOpen = open; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCurrentAircraft() { return currentAircraft; }
    public void setCurrentAircraft(String currentAircraft) { this.currentAircraft = currentAircraft; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
}
