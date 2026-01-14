package com.atc.part1.threads;

import com.atc.part1.models.LandingRequest;
import com.atc.part1.models.Aircraft;
import com.atc.part1.managers.RunwayManager;
import com.atc.part1.managers.ResourceManager;
import java.util.concurrent.PriorityBlockingQueue;

public class LandingWorker implements Runnable {
    private final int workerId;
    private final PriorityBlockingQueue<LandingRequest> landingQueue;
    private final RunwayManager runwayManager;
    private final ResourceManager resourceManager;

    public LandingWorker(int workerId, PriorityBlockingQueue<LandingRequest> landingQueue, 
                        RunwayManager runwayManager, ResourceManager resourceManager) {
        this.workerId = workerId;
        this.landingQueue = landingQueue;
        this.runwayManager = runwayManager;
        this.resourceManager = resourceManager;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                LandingRequest request = landingQueue.take();
                Aircraft aircraft = request.getAircraft();
                
                System.out.println("[WORKER-" + workerId + "] Processing landing for " + aircraft.getCallsign());
                
                String runwayId = runwayManager.acquireRunway(aircraft);
                aircraft.setAssignedRunway(runwayId);
                aircraft.setStatus("LANDING");
                
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " assigned to " + runwayId);
                
                Thread.sleep(3000); // Simulate landing time
                
                aircraft.setStatus("LANDED");
                runwayManager.releaseRunway(runwayId);
                resourceManager.recordLanding(aircraft.isEmergency());
                resourceManager.removeAircraft(aircraft.getAircraftId());
                
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " landed successfully");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
