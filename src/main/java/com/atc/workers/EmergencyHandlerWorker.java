package com.atc.workers;

import com.atc.database.DatabaseManager;
import org.bson.Document;
import java.util.List;

public class EmergencyHandlerWorker implements Runnable {
    private volatile boolean running = true;

    public EmergencyHandlerWorker() {
    }

    @Override
    public void run() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        while (running) {
            try {
                List<Document> activeAircraft = dbManager.getAllActiveAircraft();
                for (Document aircraft : activeAircraft) {
                    String emergency = aircraft.getString("emergency");
                    if (emergency != null && !"NONE".equals(emergency)) {
                        handleEmergency(aircraft, emergency);
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleEmergency(Document aircraft, String emergencyType) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        String callsign = aircraft.getString("callsign");
        
        switch (emergencyType) {
            case "FIRE" -> dbManager.saveEmergencyEvent(callsign, 
                "FIRE EMERGENCY - Engine shutdown, fire suppression active, emergency services positioned");
            case "MEDICAL" -> dbManager.saveEmergencyEvent(callsign, 
                "MEDICAL EMERGENCY - PAN-PAN declared, ambulance dispatched");
            case "SECURITY" -> dbManager.saveEmergencyEvent(callsign, 
                "SECURITY THREAT - Discrete code set, law enforcement alerted");
            case "FUEL_CRITICAL" -> dbManager.saveEmergencyEvent(callsign, 
                "FUEL CRITICAL - Direct routing, no holding, immediate landing clearance");
            case "WEATHER_STORM" -> dbManager.saveEmergencyEvent(callsign, 
                "WEATHER DIVERSION - Storm blocking approach, rerouting to alternate");
        }
    }

    public void stop() {
        running = false;
    }
}
