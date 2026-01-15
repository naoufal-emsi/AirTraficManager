package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import java.util.List;

public class FuelMonitoringWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private volatile boolean running = true;

    public FuelMonitoringWorker(List<Aircraft> aircraft) {
        this.activeAircraft = aircraft;
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
            aircraft.escalateToFuelLow();
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "FUEL LOW - Requesting priority vectors");
        } else if (aircraft.getEmergencyType() == Aircraft.EmergencyType.FUEL_LOW && 
                   aircraft.getFuelLevel() < fuelNeeded * 1.05) {
            aircraft.escalateToFuelCritical();
            DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
                "FUEL CRITICAL - MAYDAY declared");
        }
    }

    public void stop() {
        running = false;
    }
}
