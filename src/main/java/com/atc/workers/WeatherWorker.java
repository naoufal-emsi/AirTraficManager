package com.atc.workers;

import com.atc.core.models.Runway;
import com.atc.database.DatabaseManager;
import java.util.*;

public class WeatherWorker implements Runnable {
    private final List<Runway> runways;
    private volatile boolean running = true;
    private Random random = new Random();

    public WeatherWorker(List<Runway> runways) {
        this.runways = runways;
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
            DatabaseManager.getInstance().saveWeatherEvent(
                "Thunderstorm affecting runways", affectedRunways);
            
            new Thread(() -> {
                try {
                    Thread.sleep(20000);
                    for (Runway runway : runways) {
                        if (affectedRunways.contains(runway.getRunwayId())) {
                            runway.setWeatherAffected(false);
                        }
                    }
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
