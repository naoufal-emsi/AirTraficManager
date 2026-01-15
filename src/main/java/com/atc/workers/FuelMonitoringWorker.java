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
                        String currentEmergency = aircraft.getString("emergency");
                        
                        if (fuel <= 0 && ("FUEL_LOW".equals(currentEmergency) || "FUEL_CRITICAL".equals(currentEmergency))) {
                            Document updates = new Document("status", "CRASHED")
                                .append("emergency", "FUEL_EXHAUSTED");
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), updates);
                            dbManager.saveEmergencyEvent(aircraft.getString("callsign"), "CRITICAL: " + aircraft.getString("callsign") + " ran out of fuel and crashed");
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
