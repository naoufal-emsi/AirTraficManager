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
                    if (!"LANDED".equals(aircraft.getString("status"))) {
                        double currentFuel = aircraft.getDouble("fuel");
                        double currentDistance = aircraft.getDouble("distance");
                        
                        // Update fuel and distance
                        double newFuel = Math.max(0, currentFuel - 50);
                        double newDistance = Math.max(0, currentDistance - 2);
                        
                        Document updates = new Document("fuel", newFuel)
                            .append("distance", newDistance);
                            
                        if (newDistance <= 0) {
                            updates.append("status", "READY_TO_LAND");
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
