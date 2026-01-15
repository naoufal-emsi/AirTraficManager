package com.atc.database;

import com.mongodb.client.*;
import org.bson.Document;
import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import java.util.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private DatabaseManager() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("airTrafficControl");
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void saveAircraft(Aircraft aircraft) {
        MongoCollection<Document> collection = database.getCollection("aircraft");
        Document doc = new Document("callsign", aircraft.getCallsign())
            .append("fuel", aircraft.getFuelLevel())
            .append("speed", aircraft.getSpeed())
            .append("distance", aircraft.getDistanceToAirport())
            .append("status", aircraft.getStatus().toString())
            .append("emergency", aircraft.getEmergencyType().toString())
            .append("priority", aircraft.getPriority())
            .append("runway", aircraft.getAssignedRunway())
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public void saveRunwayEvent(Runway runway, String event) {
        MongoCollection<Document> collection = database.getCollection("runway_events");
        Document doc = new Document("runwayId", runway.getRunwayId())
            .append("status", runway.getStatus())
            .append("event", event)
            .append("aircraft", runway.getCurrentAircraft() != null ? runway.getCurrentAircraft().getCallsign() : null)
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public void saveEmergencyEvent(Aircraft aircraft, String details) {
        MongoCollection<Document> collection = database.getCollection("emergency_events");
        Document doc = new Document("callsign", aircraft.getCallsign())
            .append("emergencyType", aircraft.getEmergencyType().toString())
            .append("priority", aircraft.getPriority())
            .append("fuel", aircraft.getFuelLevel())
            .append("details", details)
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public void saveWeatherEvent(String description, List<String> affectedRunways) {
        MongoCollection<Document> collection = database.getCollection("weather_events");
        Document doc = new Document("description", description)
            .append("affectedRunways", affectedRunways)
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public void close() {
        if (mongoClient != null) mongoClient.close();
    }
}
