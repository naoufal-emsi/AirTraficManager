package com.atc.part1.controllers;

import com.atc.part1.models.Aircraft;
import com.atc.part1.models.LandingRequest;
import com.atc.part1.managers.RunwayManager;
import com.atc.part1.managers.ResourceManager;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;

public class LandingController {
    private final PriorityBlockingQueue<LandingRequest> landingQueue;
    private final RunwayManager runwayManager;
    private final ResourceManager resourceManager;
    private final ExecutorService workerPool;
    private final EmergencyController emergencyController;

    public LandingController(RunwayManager runwayManager, ResourceManager resourceManager) {
        this.landingQueue = new PriorityBlockingQueue<>();
        this.runwayManager = runwayManager;
        this.resourceManager = resourceManager;
        this.workerPool = Executors.newFixedThreadPool(3);
        this.emergencyController = new EmergencyController(this, runwayManager);
    }

    public void requestLanding(Aircraft aircraft) {
        aircraft.setRequestTime(LocalDateTime.now());
        aircraft.setStatus("APPROACHING");
        resourceManager.registerAircraft(aircraft);
        
        LandingRequest request = new LandingRequest(aircraft);
        landingQueue.offer(request);
        
        System.out.println("[LANDING] Aircraft " + aircraft.getCallsign() + " requested landing");
    }

    public void declareEmergency(String aircraftId) {
        Aircraft aircraft = resourceManager.getAircraft(aircraftId);
        if (aircraft != null) {
            aircraft.setEmergency(true);
            aircraft.setPriority(1);
            emergencyController.handleEmergency(aircraft);
        }
    }

    public void startLandingWorkers() {
        for (int i = 0; i < 3; i++) {
            workerPool.submit(new com.atc.part1.threads.LandingWorker(i, landingQueue, runwayManager, resourceManager));
        }
    }

    public void shutdown() {
        workerPool.shutdown();
    }

    public PriorityBlockingQueue<LandingRequest> getLandingQueue() {
        return landingQueue;
    }

    public RunwayManager getRunwayManager() {
        return runwayManager;
    }
}
