package com.atc.part1.managers;

import com.atc.part1.models.Runway;
import com.atc.part1.models.Aircraft;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

public class RunwayManager {
    private final Semaphore runwaySemaphore;
    private final ConcurrentHashMap<String, Runway> runways;
    private final int MAX_RUNWAYS = 2;

    public RunwayManager() {
        this.runwaySemaphore = new Semaphore(MAX_RUNWAYS);
        this.runways = new ConcurrentHashMap<>();
        initializeRunways();
    }

    private void initializeRunways() {
        runways.put("RW01", new Runway("RW01"));
        runways.put("RW02", new Runway("RW02"));
    }

    public String acquireRunway(Aircraft aircraft) throws InterruptedException {
        runwaySemaphore.acquire();
        
        for (Runway runway : runways.values()) {
            if ("FREE".equals(runway.getStatus()) && runway.isOpen()) {
                runway.setStatus("OCCUPIED");
                runway.setCurrentAircraft(aircraft.getAircraftId());
                runway.setLastUsed(LocalDateTime.now());
                return runway.getRunwayId();
            }
        }
        return null;
    }

    public void releaseRunway(String runwayId) {
        Runway runway = runways.get(runwayId);
        if (runway != null) {
            runway.setStatus("FREE");
            runway.setCurrentAircraft(null);
            runwaySemaphore.release();
        }
    }

    public void closeRunway(String runwayId) {
        Runway runway = runways.get(runwayId);
        if (runway != null) {
            runway.setOpen(false);
            runway.setStatus("CLOSED");
        }
    }

    public void openRunway(String runwayId) {
        Runway runway = runways.get(runwayId);
        if (runway != null) {
            runway.setOpen(true);
            runway.setStatus("FREE");
        }
    }

    public ConcurrentHashMap<String, Runway> getRunways() {
        return runways;
    }

    public int getAvailableRunways() {
        return runwaySemaphore.availablePermits();
    }
}
