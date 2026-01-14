package com.atc.part1.managers;

import com.atc.part1.models.Aircraft;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceManager {
    private final ConcurrentHashMap<String, Aircraft> activeAircraft;
    private final AtomicInteger totalLandings;
    private final AtomicInteger emergencyLandings;

    public ResourceManager() {
        this.activeAircraft = new ConcurrentHashMap<>();
        this.totalLandings = new AtomicInteger(0);
        this.emergencyLandings = new AtomicInteger(0);
    }

    public void registerAircraft(Aircraft aircraft) {
        activeAircraft.put(aircraft.getAircraftId(), aircraft);
    }

    public void removeAircraft(String aircraftId) {
        activeAircraft.remove(aircraftId);
    }

    public Aircraft getAircraft(String aircraftId) {
        return activeAircraft.get(aircraftId);
    }

    public void recordLanding(boolean isEmergency) {
        totalLandings.incrementAndGet();
        if (isEmergency) {
            emergencyLandings.incrementAndGet();
        }
    }

    public int getTotalLandings() {
        return totalLandings.get();
    }

    public int getEmergencyLandings() {
        return emergencyLandings.get();
    }

    public ConcurrentHashMap<String, Aircraft> getActiveAircraft() {
        return activeAircraft;
    }
}
