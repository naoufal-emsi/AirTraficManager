package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class FuelMonitoringWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private final PriorityBlockingQueue<Aircraft> landingQueue;
    private volatile boolean running = true;

    public FuelMonitoringWorker(List<Aircraft> aircraft, PriorityBlockingQueue<Aircraft> queue) {
        this.activeAircraft = aircraft;
        this.landingQueue = queue;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    if (aircraft.getStatus() != Aircraft.Status.LANDED) {
                        checkFuelStatus(aircraft);
                    }
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void checkFuelStatus(Aircraft aircraft) {
        double fuelNeeded = (aircraft.getDistanceToAirport() / aircraft.getSpeed()) * 
                           aircraft.getFuelBurnRate() * 60 + 30;
        
        if (aircraft.getEmergencyType() == Aircraft.EmergencyType.NONE && 
            aircraft.getFuelLevel() < fuelNeeded * 1.2) {
            synchronized(landingQueue) {
                landingQueue.remove(aircraft);
                aircraft.escalateToFuelLow();
                landingQueue.offer(aircraft);
            }
            CompletableFuture.runAsync(() -> 
                DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                    "FUEL LOW - Requesting priority vectors"));
            AirTrafficSystem.updateGUI();
        } else if (aircraft.getEmergencyType() == Aircraft.EmergencyType.FUEL_LOW && 
                   aircraft.getFuelLevel() < fuelNeeded * 1.05) {
            synchronized(landingQueue) {
                landingQueue.remove(aircraft);
                aircraft.escalateToFuelCritical();
                landingQueue.offer(aircraft);
            }
            CompletableFuture.runAsync(() -> 
                DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                    "FUEL CRITICAL - MAYDAY declared"));
            AirTrafficSystem.updateGUI();
        }
    }

    public void stop() {
        running = false;
    }
}
