package com.atc.workers;

import com.atc.database.DatabaseManager;
import com.atc.AirTrafficSystem;
import org.bson.Document;
import java.util.*;

public class WeatherWorker implements Runnable {
    private volatile boolean running = true;
    private Random random = new Random();

    public WeatherWorker() {
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void generateWeatherEvent(DatabaseManager dbManager) {
        List<Document> runways = dbManager.getAllRunways();
        List<String> affectedRunways = new ArrayList<>();
        
        for (Document runway : runways) {
            if (random.nextDouble() < 0.4) {
                affectedRunways.add(runway.getString("runwayId"));
            }
        }
        
        if (!affectedRunways.isEmpty()) {
            dbManager.saveWeatherEvent("Thunderstorm affecting runways", affectedRunways);
            
            List<Document> activeAircraft = dbManager.getAllActiveAircraft();
            for (Document aircraft : activeAircraft) {
                if ("APPROACHING".equals(aircraft.getString("status")) && random.nextDouble() < 0.3) {
                    Document updates = new Document("emergency", "WEATHER_STORM")
                        .append("status", "HOLDING")
                        .append("priority", 30);
                    dbManager.updateActiveAircraft(aircraft.getString("callsign"), updates);
                }
            }
            AirTrafficSystem.updateGUI();
            
            new Thread(() -> {
                try {
                    Thread.sleep(20000);
                    for (String runwayId : affectedRunways) {
                        dbManager.saveRunwayEvent("Weather cleared for " + runwayId);
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
