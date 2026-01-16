package com.atc.workers;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.List;

public class RunwayManagerWorker implements Runnable {
    private static final Object RUNWAY_LOCK = new Object();
    private volatile boolean running = true;

    public RunwayManagerWorker() {
    }

    @Override
    public void run() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        while (running) {
            try {
                synchronized (RUNWAY_LOCK) {
                    List<Document> readyAircraft = dbManager.getAllActiveAircraft();
                    List<Document> availableRunways = dbManager.getAllRunways();
                
                // Sort by threat time - who will die first gets priority
                readyAircraft.sort((a1, a2) -> {
                    double threat1 = calculateThreatTime(a1);
                    double threat2 = calculateThreatTime(a2);
                    return Double.compare(threat1, threat2);
                });
                
                // Handle preemption: Emergency can take runway from HOLDING aircraft
                for (Document aircraft : readyAircraft) {
                    String status = aircraft.getString("status");
                    int priority = aircraft.getInteger("priority", 100);
                    
                    if ("HOLDING".equals(status)) {
                        Document runway = findFirstAvailableRunway(availableRunways);
                        
                        // If no runway available, check if we can preempt a HOLDING aircraft
                        if (runway == null && !"NONE".equals(aircraft.getString("emergency"))) {
                            runway = findPreemptableRunway(readyAircraft, availableRunways, dbManager, aircraft);
                        }
                        
                        if (runway != null) {
                            String callsign = aircraft.getString("callsign");
                            String runwayId = runway.getString("runwayId");
                            
                            dbManager.updateRunway(runwayId, 
                                new Document("status", "OCCUPIED")
                                    .append("currentAircraft", callsign));
                            
                            dbManager.updateActiveAircraft(callsign, 
                                new Document("status", "LANDING")
                                    .append("assignedRunway", runwayId));
                            
                            String emergency = aircraft.getString("emergency");
                            double threatTime = calculateThreatTime(aircraft);
                            String logMsg = "Aircraft " + callsign + 
                                          ("NONE".equals(emergency) ? "" : 
                                              " [" + emergency + " - THREAT: " + String.format("%.0f", threatTime) + "s]" ) +
                                          " assigned to " + runwayId +
                                          " at distance " + String.format("%.0f", aircraft.getDouble("distance")) + "m";
                            dbManager.saveRunwayEvent(logMsg);
                            System.out.println(logMsg);
                        }
                    } else if ("LANDED".equals(status)) {
                        String assignedRunway = aircraft.getString("assignedRunway");
                        if (assignedRunway != null) {
                            dbManager.updateRunway(assignedRunway, 
                                new Document("status", "AVAILABLE")
                                    .append("currentAircraft", null));
                            
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"),
                                new Document("assignedRunway", null));
                            
                            dbManager.saveRunwayEvent("Aircraft " + aircraft.getString("callsign") + " landed safely on " + assignedRunway);
                        }
                    }
                }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private Document findFirstAvailableRunway(List<Document> runways) {
        return runways.stream()
            .filter(r -> "AVAILABLE".equals(r.getString("status")))
            .findFirst()
            .orElse(null);
    }
    
    private Document findPreemptableRunway(List<Document> allAircraft, List<Document> runways, DatabaseManager dbManager, Document emergencyAircraft) {
        double emergencyThreat = calculateThreatTime(emergencyAircraft);
        
        for (Document runway : runways) {
            if ("OCCUPIED".equals(runway.getString("status"))) {
                String currentCallsign = runway.getString("currentAircraft");
                if (currentCallsign != null) {
                    Document currentAircraft = allAircraft.stream()
                        .filter(a -> currentCallsign.equals(a.getString("callsign")))
                        .findFirst()
                        .orElse(null);
                    
                    if (currentAircraft != null) {
                        String currentStatus = currentAircraft.getString("status");
                        double currentDistance = currentAircraft.getDouble("distance");
                        double currentThreat = calculateThreatTime(currentAircraft);
                        
                        // Can preempt if emergency has lower threat time and current aircraft is still far
                        if ("LANDING".equals(currentStatus) && currentDistance > 1000 && emergencyThreat < currentThreat) {
                            dbManager.updateActiveAircraft(currentCallsign, 
                                new Document("status", "HOLDING")
                                    .append("assignedRunway", null));
                            
                            String logMsg = "Aircraft " + currentCallsign + " (threat: " + String.format("%.0f", currentThreat) + "s) preempted by emergency (threat: " + String.format("%.0f", emergencyThreat) + "s)";
                            dbManager.saveRunwayEvent(logMsg);
                            System.out.println(logMsg);
                            
                            return runway;
                        }
                    }
                }
            }
        }
        return null;
    }

    private double calculateThreatTime(Document aircraft) {
        String emergency = aircraft.getString("emergency");
        double distance = aircraft.getDouble("distance");
        double speed = aircraft.getDouble("speed");
        double fuel = aircraft.getDouble("fuel");
        double fuelBurnRate = aircraft.getDouble("fuelBurnRate");
        
        if ("FIRE".equals(emergency)) {
            return aircraft.getDouble("fireTimeRemaining");
        } else if ("FUEL_CRITICAL".equals(emergency)) {
            // FUEL_CRITICAL: only care about fuel time, not distance
            if (fuelBurnRate <= 0) return Double.MAX_VALUE;
            return (fuel / fuelBurnRate) * 3600;
        } else if ("FUEL_LOW".equals(emergency)) {
            // FUEL_LOW: consider both fuel and distance
            if (speed <= 0 || fuelBurnRate <= 0) return Double.MAX_VALUE;
            double fuelTime = (fuel / fuelBurnRate) * 3600;
            double runwayTime = distance / speed;
            return Math.min(fuelTime, runwayTime);
        } else {
            // Normal aircraft: just time to runway
            if (speed <= 0) return Double.MAX_VALUE;
            return distance / speed;
        }
    }

    public void stop() {
        running = false;
    }
}
