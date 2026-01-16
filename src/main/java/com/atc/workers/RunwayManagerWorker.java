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
                        Document runway = findClosestAvailableRunway(availableRunways, aircraft);
                        if (runway != null) {
                            dbManager.updateRunway(runway.getString("runwayId"), 
                                new Document("status", "OCCUPIED")
                                    .append("currentAircraft", aircraft.getString("callsign")));
                            
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), 
                                new Document("status", "LANDING")
                                    .append("runway", runway.getString("runwayId")));
                            
                            String emergency = aircraft.getString("emergency");
                            String logMsg = "Aircraft " + aircraft.getString("callsign") + 
                                          ("NONE".equals(emergency) ? "" : " [" + emergency + " - PRIORITY]" ) +
                                          " assigned to " + runway.getString("runwayId");
                            dbManager.saveRunwayEvent(logMsg);
                            
                            Thread.sleep(5000);
                            
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), 
                                new Document("status", "LANDED")
                                    .append("emergency", "NONE")
                                    .append("priority", 100));
                            
                            dbManager.updateRunway(runway.getString("runwayId"), 
                                new Document("status", "AVAILABLE")
                                    .append("currentAircraft", null));
                            
                            dbManager.saveRunwayEvent("Aircraft " + aircraft.getString("callsign") + " landed safely on " + runway.getString("runwayId"));
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
    
    private Document findClosestAvailableRunway(List<Document> runways, Document aircraft) {
        double aircraftDistance = aircraft.getDouble("distance");
        return runways.stream()
            .filter(r -> "AVAILABLE".equals(r.getString("status")))
            .min((r1, r2) -> {
                double pos1 = r1.getDouble("position");
                double pos2 = r2.getDouble("position");
                double dist1 = Math.abs(aircraftDistance - pos1);
                double dist2 = Math.abs(aircraftDistance - pos2);
                return Double.compare(dist1, dist2);
            })
            .orElse(null);
    }

    public void stop() {
        running = false;
    }
}
