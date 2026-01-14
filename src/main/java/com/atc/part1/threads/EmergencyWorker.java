package com.atc.part1.threads;

import com.atc.part1.models.Aircraft;
import com.atc.part1.managers.RunwayManager;

public class EmergencyWorker implements Runnable {
    private final Aircraft aircraft;
    private final RunwayManager runwayManager;

    public EmergencyWorker(Aircraft aircraft, RunwayManager runwayManager) {
        this.aircraft = aircraft;
        this.runwayManager = runwayManager;
    }

    @Override
    public void run() {
        try {
            System.out.println("[EMERGENCY] Priority landing for " + aircraft.getCallsign());
            
            String runwayId = runwayManager.acquireRunway(aircraft);
            aircraft.setAssignedRunway(runwayId);
            aircraft.setStatus("EMERGENCY_LANDING");
            
            System.out.println("[EMERGENCY] " + aircraft.getCallsign() + " cleared for immediate landing on " + runwayId);
            
            Thread.sleep(2000); // Faster landing for emergency
            
            aircraft.setStatus("LANDED");
            runwayManager.releaseRunway(runwayId);
            
            System.out.println("[EMERGENCY] " + aircraft.getCallsign() + " landed safely");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
