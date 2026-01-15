package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.SimulationConfig;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class FuelMonitoringWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private final PriorityBlockingQueue<Aircraft> landingQueue;
    private final SimulationConfig config;
    private volatile boolean running = true;

    public FuelMonitoringWorker(List<Aircraft> aircraft, PriorityBlockingQueue<Aircraft> queue, SimulationConfig config) {
        this.activeAircraft = aircraft;
        this.landingQueue = queue;
        this.config = config;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    if (aircraft.getStatus() != Aircraft.Status.LANDED) {
                        if (aircraft.checkAndEscalateFuelStatus()) {
                            landingQueue.remove(aircraft);
                            landingQueue.offer(aircraft);
                            
                            CompletableFuture.runAsync(() -> 
                                DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                                    "Fuel escalation: " + aircraft.getEmergencyType()));
                            AirTrafficSystem.updateGUI();
                        }
                    }
                }
                Thread.sleep((long)(config.getTimeStepSeconds() * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }
}
