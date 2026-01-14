package com.atc.part1.controllers;

import com.atc.part1.models.Aircraft;
import com.atc.part1.managers.RunwayManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmergencyController {
    private final LandingController landingController;
    private final RunwayManager runwayManager;
    private final ExecutorService emergencyPool;

    public EmergencyController(LandingController landingController, RunwayManager runwayManager) {
        this.landingController = landingController;
        this.runwayManager = runwayManager;
        this.emergencyPool = Executors.newFixedThreadPool(2);
    }

    public void handleEmergency(Aircraft aircraft) {
        emergencyPool.submit(new com.atc.part1.threads.EmergencyWorker(aircraft, runwayManager));
        System.out.println("[EMERGENCY] Handling emergency for " + aircraft.getCallsign());
    }

    public void shutdown() {
        emergencyPool.shutdown();
    }
}
