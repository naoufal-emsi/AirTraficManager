package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.SimulationClock;

public class AircraftTask implements Runnable {
    private final Aircraft aircraft;
    
    public AircraftTask(Aircraft aircraft) {
        this.aircraft = aircraft;
    }
    
    @Override
    public void run() {
        if (aircraft.getStatus() != Aircraft.Status.LANDED) {
            aircraft.updateState();
        }
    }
    
    public Aircraft getAircraft() {
        return aircraft;
    }
}