package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.database.DatabaseManager;
import java.util.List;

public class EmergencyHandlerWorker implements Runnable {
    private final List<Aircraft> activeAircraft;
    private volatile boolean running = true;

    public EmergencyHandlerWorker(List<Aircraft> aircraft) {
        this.activeAircraft = aircraft;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (Aircraft aircraft : activeAircraft) {
                    handleEmergency(aircraft);
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleEmergency(Aircraft aircraft) {
        switch (aircraft.getEmergencyType()) {
            case FIRE:
                handleFire(aircraft);
                break;
            case MEDICAL:
                handleMedical(aircraft);
                break;
            case SECURITY:
                handleSecurity(aircraft);
                break;
            case FUEL_CRITICAL:
                handleFuelCritical(aircraft);
                break;
            case WEATHER_STORM:
                handleWeatherStorm(aircraft);
                break;
        }
    }

    private void handleFire(Aircraft aircraft) {
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "FIRE EMERGENCY - Engine shutdown, fire suppression active, emergency services positioned");
    }

    private void handleMedical(Aircraft aircraft) {
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "MEDICAL EMERGENCY - PAN-PAN declared, ambulance dispatched");
    }

    private void handleSecurity(Aircraft aircraft) {
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "SECURITY THREAT - Discrete code set, law enforcement alerted");
    }

    private void handleFuelCritical(Aircraft aircraft) {
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "FUEL CRITICAL - Direct routing, no holding, immediate landing clearance");
    }

    private void handleWeatherStorm(Aircraft aircraft) {
        DatabaseManager.getInstance().saveEmergencyEvent(aircraft, 
            "WEATHER DIVERSION - Storm blocking approach, rerouting to alternate");
    }

    public void stop() {
        running = false;
    }
}
