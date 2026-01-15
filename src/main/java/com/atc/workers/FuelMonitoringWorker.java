package com.atc.workers;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.List;

public class FuelMonitoringWorker implements Runnable {
    private final double timeStepSeconds;
    private volatile boolean running = true;

    public FuelMonitoringWorker(double timeStepSeconds) {
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
                        double fuel = aircraft.getDouble("fuel");
                        String emergency = "NONE";
                        int priority = 100;
                        
                        if (fuel < 1000) {
                            emergency = "FUEL_CRITICAL";
                            priority = 1;
                        } else if (fuel < 3000) {
                            emergency = "FUEL_LOW";
                            priority = 25;
                        }
                        
                        if (!"NONE".equals(emergency)) {
                            Document updates = new Document("emergency", emergency)
                                .append("priority", priority);
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), updates);
                            
                            dbManager.saveEmergencyEvent(aircraft.getString("callsign"), "Fuel emergency: " + aircraft.getString("callsign") + " - " + emergency);
                        }
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
