package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.concurrent.CompletableFuture;

public class AircraftThread implements Runnable {
    private final Aircraft aircraft;
    private volatile boolean running = true;
    private final double timeStepSeconds;

    public AircraftThread(Aircraft aircraft, double timeStepSeconds) {
        this.aircraft = aircraft;
        this.timeStepSeconds = timeStepSeconds;
    }

    @Override
    public void run() {
        while (running && aircraft.getStatus() != Aircraft.Status.LANDED) {
            try {
                aircraft.updatePosition(timeStepSeconds);
                CompletableFuture.runAsync(() -> DatabaseManager.getInstance().saveAircraft(aircraft));
                AirTrafficSystem.updateGUI();
                Thread.sleep((long)(timeStepSeconds * 1000));
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
