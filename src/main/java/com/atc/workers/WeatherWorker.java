package com.atc.workers;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class WeatherWorker implements Runnable {
    private final List<Runway> runways;
    private final List<Aircraft> activeAircraft;
    private final PriorityBlockingQueue<Aircraft> landingQueue;
    private volatile boolean running = true;
    private Random random = new Random();

    public WeatherWorker(List<Runway> runways, List<Aircraft> aircraft, PriorityBlockingQueue<Aircraft> queue) {
        this.runways = runways;
        this.activeAircraft = aircraft;
        this.landingQueue = queue;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(30000);
                if (random.nextDouble() < 0.3) {
                    generateWeatherEvent();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void generateWeatherEvent() {
        List<String> affectedRunways = new ArrayList<>();
        for (Runway runway : runways) {
            if (random.nextDouble() < 0.4) {
                runway.setWeatherAffected(true);
                affectedRunways.add(runway.getRunwayId());
            }
        }
        
        if (!affectedRunways.isEmpty()) {
            CompletableFuture.runAsync(() -> 
                DatabaseManager.getInstance().saveWeatherEvent(
                    "Thunderstorm affecting runways", affectedRunways));
            
            for (Aircraft aircraft : activeAircraft) {
                if (aircraft.getStatus() == Aircraft.Status.APPROACHING && random.nextDouble() < 0.3) {
                    synchronized(landingQueue) {
                        landingQueue.remove(aircraft);
                        aircraft.declareEmergency(Aircraft.EmergencyType.WEATHER_STORM);
                        aircraft.setStatus(Aircraft.Status.HOLDING);
                        landingQueue.offer(aircraft);
                    }
                }
            }
            AirTrafficSystem.updateGUI();
            
            new Thread(() -> {
                try {
                    Thread.sleep(20000);
                    for (Runway runway : runways) {
                        if (affectedRunways.contains(runway.getRunwayId())) {
                            runway.setWeatherAffected(false);
                        }
                    }
                    AirTrafficSystem.updateGUI();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public void stop() {
        running = false;
    }
}
