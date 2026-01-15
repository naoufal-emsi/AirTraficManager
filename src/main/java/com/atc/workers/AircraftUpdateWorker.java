package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AircraftUpdateWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private volatile boolean running = true;

    public AircraftUpdateWorker(List<Aircraft> aircraft) {
        this.activeAircraft = aircraft;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    if (aircraft.getStatus() != Aircraft.Status.LANDED) {
                        aircraft.updatePosition(5.0);
                        DatabaseManager.getInstance().saveAircraft(aircraft);
                    }
                }
                Thread.sleep(5000);
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
