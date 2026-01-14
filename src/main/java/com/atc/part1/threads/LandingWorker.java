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
                
                // Burn fuel while waiting for runway (holding pattern)
                int waitFuelBurn = (int)(Math.random() * 3) + 2; // 2-5% fuel burn
                aircraft.setFuelLevel(Math.max(0, aircraft.getFuelLevel() - waitFuelBurn));
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " holding - fuel: " + aircraft.getFuelLevel() + "%");
                
                String runwayId = runwayManager.acquireRunway(aircraft);
                aircraft.setAssignedRunway(runwayId);
                aircraft.setStatus("LANDING");
                
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " assigned to " + runwayId);
                
                // Realistic landing sequence
                Thread.sleep(8000);  // Approach: 8s (scaled from 8 min)
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " on final approach");
                
                Thread.sleep(6000);  // Touchdown to exit: 6s (scaled from 60s)
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " clearing runway");
                
                Thread.sleep(3000);  // Taxi to gate: 3s (scaled from 3 min)
                
                // Burn fuel during landing
                int landingFuelBurn = (int)(Math.random() * 2) + 1; // 1-3% fuel burn
                aircraft.setFuelLevel(Math.max(0, aircraft.getFuelLevel() - landingFuelBurn));
                
                aircraft.setStatus("LANDED");
                runwayManager.releaseRunway(runwayId);
                resourceManager.recordLanding(aircraft.isEmergency());
                
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " landed successfully (Final fuel: " + aircraft.getFuelLevel() + "%)");
                
                // Keep aircraft visible for 10 seconds before removing
                Thread.sleep(10000);
                resourceManager.removeAircraft(aircraft.getAircraftId());
                System.out.println("[WORKER-" + workerId + "] " + aircraft.getCallsign() + " departed to gate");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
