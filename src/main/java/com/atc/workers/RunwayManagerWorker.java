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
                
                for (Document aircraft : readyAircraft) {
                    if ("READY_TO_LAND".equals(aircraft.getString("status"))) {
                        Document runway = findAvailableRunway(availableRunways);
                        if (runway != null) {
                            // Assign aircraft to runway
                            dbManager.updateRunway(runway.getString("runwayId"), 
                                new Document("status", "OCCUPIED")
                                    .append("currentAircraft", aircraft.getString("callsign")));
                            
                            // Update aircraft status
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), 
                                new Document("status", "LANDING")
                                    .append("runway", runway.getString("runwayId")));
                            
                            // Log event
                            dbManager.saveRunwayEvent("Aircraft " + aircraft.getString("callsign") + " assigned to " + runway.getString("runwayId"));
                            
                            // Simulate landing duration
                            Thread.sleep(5000);
                            
                            // Complete landing
                            dbManager.updateActiveAircraft(aircraft.getString("callsign"), 
                                new Document("status", "LANDED"));
                            
                            dbManager.updateRunway(runway.getString("runwayId"), 
                                new Document("status", "AVAILABLE")
                                    .append("currentAircraft", null));
                            
                            dbManager.saveRunwayEvent("Aircraft " + aircraft.getString("callsign") + " landed on " + runway.getString("runwayId"));
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
    
    private Document findAvailableRunway(List<Document> runways) {
        for (Document runway : runways) {
            if ("AVAILABLE".equals(runway.getString("status"))) {
                return runway;
            }
        }
        return null;
    }

    public void stop() {
        running = false;
    }
}
