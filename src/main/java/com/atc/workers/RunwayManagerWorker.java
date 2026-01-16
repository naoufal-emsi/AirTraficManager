package com.atc.workers;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.List;

public class RunwayManagerWorker implements Runnable {
    private volatile boolean running = true;

    public RunwayManagerWorker() {
    }

    @Override
    public void run() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        while (running) {
            try {
                List<Document> readyAircraft = dbManager.getAllActiveAircraft();
                List<Document> availableRunways = dbManager.getAllRunways();
                
                readyAircraft.sort((a1, a2) -> {
                    int p1 = a1.getInteger("priority", 100);
                    int p2 = a2.getInteger("priority", 100);
                    if (p1 != p2) {
                        return Integer.compare(p1, p2);
                    }
                    Long t1 = a1.getLong("emergencyTimestamp");
                    Long t2 = a2.getLong("emergencyTimestamp");
                    if (t1 == null) t1 = 0L;
                    if (t2 == null) t2 = 0L;
                    return Long.compare(t1, t2);
                });
                
                for (Document aircraft : readyAircraft) {
                    if ("READY_TO_LAND".equals(aircraft.getString("status"))) {
                        Document runway = findFirstAvailableRunway(availableRunways);
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
                                          ("NONE".equals(emergency) ? "" : " [" + emergency + " - PRIORITY]" ) +
                                          " assigned to " + runwayId +
                                          " at distance " + String.format("%.0f", aircraft.getDouble("distance")) + "m";
                            dbManager.saveRunwayEvent(logMsg);
                            System.out.println(logMsg);
                        }
                    } else if ("LANDED".equals(aircraft.getString("status"))) {
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

    public void stop() {
        running = false;
    }
}
