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
                
                // Sort by priority first, then by distance (closer = higher priority)
                readyAircraft.sort((a1, a2) -> {
                    int p1 = a1.getInteger("priority", 100);
                    int p2 = a2.getInteger("priority", 100);
                    if (p1 != p2) {
                        return Integer.compare(p1, p2); // Lower priority number = higher priority
                    }
                    // Same priority, sort by distance (closer first)
                    double d1 = a1.getDouble("distance");
                    double d2 = a2.getDouble("distance");
                    return Double.compare(d1, d2); // Closer distance = higher priority
                });
                
                // Handle preemption: Emergency can take runway from HOLDING aircraft
                for (Document aircraft : readyAircraft) {
                    String status = aircraft.getString("status");
                    int priority = aircraft.getInteger("priority", 100);
                    
                    if ("HOLDING".equals(status)) {
                        Document runway = findFirstAvailableRunway(availableRunways);
                        
                        // If no runway available, check if we can preempt a HOLDING aircraft
                        if (runway == null && priority < 100) { // Emergency aircraft
                            runway = findPreemptableRunway(readyAircraft, availableRunways, dbManager, priority);
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
                            String logMsg = "Aircraft " + callsign + 
                                          ("NONE".equals(emergency) ? "" : " [" + emergency + " - PRIORITY " + priority + "]" ) +
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
    
    private Document findPreemptableRunway(List<Document> allAircraft, List<Document> runways, DatabaseManager dbManager, int emergencyPriority) {
        // Find runway occupied by aircraft with lower priority that hasn't started landing yet
        for (Document runway : runways) {
            if ("OCCUPIED".equals(runway.getString("status"))) {
                String currentCallsign = runway.getString("currentAircraft");
                if (currentCallsign != null) {
                    // Find the aircraft using this runway
                    Document currentAircraft = allAircraft.stream()
                        .filter(a -> currentCallsign.equals(a.getString("callsign")))
                        .findFirst()
                        .orElse(null);
                    
                    if (currentAircraft != null) {
                        String currentStatus = currentAircraft.getString("status");
                        int currentPriority = currentAircraft.getInteger("priority", 100);
                        double currentDistance = currentAircraft.getDouble("distance");
                        
                        // Can preempt if:
                        // 1. Aircraft is LANDING but still far (distance > 1000m) - hasn't really started landing
                        // 2. Aircraft has lower priority than emergency
                        if ("LANDING".equals(currentStatus) && currentDistance > 1000 && currentPriority > emergencyPriority) {
                            // Preempt: send aircraft back to HOLDING
                            dbManager.updateActiveAircraft(currentCallsign, 
                                new Document("status", "HOLDING")
                                    .append("assignedRunway", null));
                            
                            String logMsg = "Aircraft " + currentCallsign + " (priority " + currentPriority + ") preempted by emergency (priority " + emergencyPriority + ")";
                            dbManager.saveRunwayEvent(logMsg);
                            System.out.println(logMsg);
                            
                            return runway; // Return this runway for the emergency aircraft
                        }
                    }
                }
            }
        }
        return null; // No preemptable runway found
    }

    public void stop() {
        running = false;
    }
}
