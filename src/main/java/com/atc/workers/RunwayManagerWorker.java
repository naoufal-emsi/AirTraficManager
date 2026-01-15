package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class RunwayManagerWorker implements Runnable {
    private final List<Runway> runways;
    private final PriorityBlockingQueue<Aircraft> landingQueue;
    private volatile boolean running = true;

    public RunwayManagerWorker(List<Runway> runways, PriorityBlockingQueue<Aircraft> queue) {
        this.runways = runways;
        this.landingQueue = queue;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Aircraft aircraft = landingQueue.take();
                Runway assignedRunway = findBestRunway(aircraft);
                
                if (assignedRunway != null) {
                    synchronized(assignedRunway) {
                        assignedRunway.assignAircraft(aircraft);
                        aircraft.setStatus(Aircraft.Status.LANDING);
                    }
                    CompletableFuture.runAsync(() -> 
                        DatabaseManager.getInstance().saveRunwayEvent(assignedRunway, 
                            "Aircraft " + aircraft.getCallsign() + " assigned for landing"));
                    AirTrafficSystem.updateGUI();
                    
                    Thread.sleep(10000);
                    
                    synchronized(assignedRunway) {
                        aircraft.setStatus(Aircraft.Status.LANDED);
                        assignedRunway.releaseRunway();
                    }
                    CompletableFuture.runAsync(() -> 
                        DatabaseManager.getInstance().saveRunwayEvent(assignedRunway, 
                            "Aircraft " + aircraft.getCallsign() + " landed successfully"));
                    AirTrafficSystem.updateGUI();
                } else {
                    landingQueue.offer(aircraft);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Runway findBestRunway(Aircraft aircraft) {
        if (aircraft.needsImmediateLanding()) {
            for (Runway runway : runways) {
                if (runway.isAvailableForEmergency()) {
                    return runway;
                }
            }
        }
        
        for (Runway runway : runways) {
            if (runway.isOpen() && !runway.isWeatherAffected()) {
                return runway;
            }
        }
        return null;
    }

    public void stop() {
        running = false;
    }
}
