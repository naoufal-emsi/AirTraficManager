package com.atc.workers;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.List;

public class AircraftUpdateWorker implements Runnable {
    private final double timeStepSeconds;
    private volatile boolean running = true;

    public AircraftUpdateWorker(double timeStepSeconds) {
        this.timeStepSeconds = timeStepSeconds;
    }

    @Override
    public void run() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        while (running) {
            try {
                List<Document> activeAircraft = dbManager.getAllActiveAircraft();
                for (Document aircraft : activeAircraft) {
                    String status = aircraft.getString("status");
                    if ("APPROACHING".equals(status) || "LANDING".equals(status)) {
                        double distance = aircraft.getDouble("distance");
                        double speed = aircraft.getDouble("speed");
                        
                        distance -= speed * timeStepSeconds;
                        distance = Math.max(0, distance);
                        
                        Document updates = new Document("distance", distance);
                        
                        if (distance <= 5000 && "APPROACHING".equals(status)) {
                            updates.append("status", "READY_TO_LAND");
                        }
                        
                        if (distance <= 50 && "LANDING".equals(status)) {
                            updates.append("distance", 0.0)
                                   .append("status", "LANDED")
                                   .append("emergency", "NONE")
                                   .append("priority", 100);
                        }
                        
                        dbManager.updateActiveAircraft(aircraft.getString("callsign"), updates);
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
