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

    public void createAircraft(String callsign, String aircraftType, double fuelLevel, double speed, double distance, String origin, String destination) {
        MongoCollection<Document> collection = database.getCollection("aircraft_templates");
        Document doc = new Document("callsign", callsign)
            .append("aircraftType", aircraftType)
            .append("fuel", fuelLevel)
            .append("speed", speed)
            .append("distance", distance)
            .append("origin", origin)
            .append("destination", destination)
            .append("status", "APPROACHING")
            .append("emergency", "NONE")
            .append("priority", 100)
            .append("created", new Date());
        collection.insertOne(doc);
    }

    public List<Document> getAllAircraftTemplates() {
        MongoCollection<Document> collection = database.getCollection("aircraft_templates");
        return collection.find().into(new ArrayList<>());
    }

    public Document getAircraftTemplate(String callsign) {
        MongoCollection<Document> collection = database.getCollection("aircraft_templates");
        return collection.find(new Document("callsign", callsign)).first();
    }

    public void deleteAircraftTemplate(String callsign) {
        MongoCollection<Document> collection = database.getCollection("aircraft_templates");
        collection.deleteOne(new Document("callsign", callsign));
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

    public List<Document> getAircraftHistory(String callsign) {
        MongoCollection<Document> collection = database.getCollection("aircraft");
        return collection.find(new Document("callsign", callsign))
            .sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public List<Document> getRunwayEvents(String runwayId) {
        MongoCollection<Document> collection = database.getCollection("runway_events");
        return collection.find(new Document("runwayId", runwayId))
            .sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public List<Document> getEmergencyEvents() {
        MongoCollection<Document> collection = database.getCollection("emergency_events");
        return collection.find().sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public List<Document> getWeatherEvents() {
        MongoCollection<Document> collection = database.getCollection("weather_events");
        return collection.find().sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public void clearActiveAircraft() {
        MongoCollection<Document> collection = database.getCollection("aircraft");
        collection.deleteMany(new Document());
    }

    public void clearAllRuntimeData() {
        clearActiveAircraft();
        database.getCollection("runway_events").deleteMany(new Document());
        database.getCollection("emergency_events").deleteMany(new Document());
    }

    public List<Document> getActiveAircraft() {
        MongoCollection<Document> collection = database.getCollection("aircraft");
        return collection.find().into(new ArrayList<>());
    }

    public void close() {
        if (mongoClient != null) mongoClient.close();
    }
}
