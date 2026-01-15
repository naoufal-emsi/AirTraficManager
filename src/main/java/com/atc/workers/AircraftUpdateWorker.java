package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import java.util.List;

public class AircraftUpdateWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private final double timeStepSeconds;
    private volatile boolean running = true;

    public AircraftUpdateWorker(List<Aircraft> aircraft, double timeStepSeconds) {
        this.activeAircraft = aircraft;
        this.timeStepSeconds = timeStepSeconds;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    if (aircraft.getStatus() != Aircraft.Status.LANDED) {
                        aircraft.updatePosition(timeStepSeconds);
                        DatabaseManager.getInstance().saveAircraft(aircraft);
                    }
                }
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
