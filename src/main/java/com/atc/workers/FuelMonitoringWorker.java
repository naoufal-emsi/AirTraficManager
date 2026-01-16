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
                        double fuelBurnRate = aircraft.getDouble("fuelBurnRate");
                        String currentEmergency = aircraft.getString("emergency");
                        
                        // Burn fuel
                        double fuelBurned = (fuelBurnRate / 3600.0) * timeStepSeconds;
                        fuel = Math.max(0, fuel - fuelBurned);
                        
                        Document updates = new Document("fuel", fuel);
                        
                        // Decrease fire timer if FIRE emergency
                        if ("FIRE".equals(currentEmergency)) {
                            double fireTime = aircraft.getDouble("fireTimeRemaining");
                            fireTime = Math.max(0, fireTime - timeStepSeconds);
                            updates.append("fireTimeRemaining", fireTime);
                            
                            if (fireTime <= 0) {
                                updates.append("status", "CRASHED")
                                       .append("emergency", "FIRE_STRUCTURAL_FAILURE");
                                dbManager.saveEmergencyEvent(aircraft.getString("callsign"), 
                                    "CRITICAL: " + aircraft.getString("callsign") + " fire caused structural failure");
                            }
                        }
                        
                        if (fuel <= 0 && ("FUEL_LOW".equals(currentEmergency) || "FUEL_CRITICAL".equals(currentEmergency))) {
                            updates.append("status", "CRASHED")
                                   .append("emergency", "FUEL_EXHAUSTED");
                            dbManager.saveEmergencyEvent(aircraft.getString("callsign"), 
                                "CRITICAL: " + aircraft.getString("callsign") + " ran out of fuel and crashed");
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
