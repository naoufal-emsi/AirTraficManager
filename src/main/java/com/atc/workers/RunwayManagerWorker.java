package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.database.DatabaseManager;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

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
                    assignedRunway.assignAircraft(aircraft);
                    aircraft.setStatus(Aircraft.Status.LANDING);
                    DatabaseManager.getInstance().saveRunwayEvent(assignedRunway, 
                        "Aircraft " + aircraft.getCallsign() + " assigned for landing");
                    
                    Thread.sleep(10000);
                    
                    aircraft.setStatus(Aircraft.Status.LANDED);
                    assignedRunway.releaseRunway();
                    DatabaseManager.getInstance().saveRunwayEvent(assignedRunway, 
                        "Aircraft " + aircraft.getCallsign() + " landed successfully");
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
