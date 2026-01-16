package com.atc.database;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private Random random = new Random();
    private static final String[] AIRLINES = {"UAL", "DAL", "AAL", "SWA", "JBU", "ASA", "FFT", "SKW"};
    private static final String[] AIRPORTS = {"JFK", "LAX", "ORD", "DFW", "ATL", "DEN", "SFO", "SEA"};

    private DatabaseManager() {
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("airTrafficControl");
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void clearAllRuntimeData() {
        if (database == null) return;
        try {
            database.getCollection("active_aircraft").deleteMany(new Document());
            database.getCollection("runways").deleteMany(new Document());
            database.getCollection("runway_events").deleteMany(new Document());
            database.getCollection("emergency_events").deleteMany(new Document());
            database.getCollection("weather_events").deleteMany(new Document());
        } catch (Exception e) {
            System.err.println("Failed to clear runtime data: " + e.getMessage());
        }
    }

    public void initializeAirportData() {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("airports");
            if (collection.countDocuments() == 0) {
                for (String code : AIRPORTS) {
                    collection.insertOne(new Document("code", code)
                        .append("name", code + " International Airport")
                        .append("active", true));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize airport data: " + e.getMessage());
        }
    }

    public void initializeAircraftTypes() {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("aircraft_types");
            if (collection.countDocuments() == 0) {
                collection.insertOne(new Document("type", "B737").append("cruiseSpeed", 150.0).append("fuelCapacity", 5500.0).append("fuelBurnRate", 0.8));
                collection.insertOne(new Document("type", "A320").append("cruiseSpeed", 160.0).append("fuelCapacity", 6400.0).append("fuelBurnRate", 0.82));
                collection.insertOne(new Document("type", "B777").append("cruiseSpeed", 250.0).append("fuelCapacity", 11000.0).append("fuelBurnRate", 0.84));
                collection.insertOne(new Document("type", "A330").append("cruiseSpeed", 230.0).append("fuelCapacity", 11750.0).append("fuelBurnRate", 0.82));
                collection.insertOne(new Document("type", "B787").append("cruiseSpeed", 210.0).append("fuelCapacity", 15200.0).append("fuelBurnRate", 0.85));
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize aircraft types: " + e.getMessage());
        }
    }

    public void insertRunway(String runwayId, String status, String currentAircraft, double thresholdPosition) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("runways");
            Document doc = new Document("runwayId", runwayId)
                .append("status", status)
                .append("currentAircraft", currentAircraft)
                .append("thresholdPosition", thresholdPosition);
            collection.replaceOne(
                new Document("runwayId", runwayId),
                doc,
                new com.mongodb.client.model.ReplaceOptions().upsert(true)
            );
        } catch (Exception e) {
            System.err.println("Failed to insert runway: " + e.getMessage());
        }
    }

    public String generateAndInsertRealisticFlight() {
        if (database == null) return null;
        try {
            String airline = AIRLINES[random.nextInt(AIRLINES.length)];
            int flightNumber = 100 + random.nextInt(9900);
            String callsign = airline + flightNumber;
            
            String[] types = {"B737", "A320", "B777", "A330", "B787"};
            double[] speeds = {150.0, 160.0, 250.0, 230.0, 210.0};
            double[] capacities = {5500.0, 6400.0, 11000.0, 11750.0, 15200.0};
            
            int typeIndex = random.nextInt(types.length);
            String type = types[typeIndex];
            String origin = AIRPORTS[random.nextInt(AIRPORTS.length)];
            String destination = AIRPORTS[random.nextInt(AIRPORTS.length)];
            
            double fuel = capacities[typeIndex] * (0.5 + random.nextDouble() * 0.4);
            double speed = speeds[typeIndex];
            double distance = 15000 + random.nextDouble() * 15000;
            
            insertActiveAircraft(callsign, type, fuel, speed, distance, origin, destination, "APPROACHING", "NONE", 100);
            return callsign;
        } catch (Exception e) {
            System.err.println("Failed to generate flight: " + e.getMessage());
            return null;
        }
    }

    public void insertActiveAircraft(String callsign, String aircraftType, double fuel, double speed, 
                                    double distance, String origin, String destination, 
                                    String status, String emergency, int priority) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("active_aircraft");
            
            double[] speeds = {150.0, 160.0, 250.0, 230.0, 210.0};
            double[] burnRates = {800.0, 820.0, 840.0, 820.0, 850.0};
            int typeIndex = 0;
            for (int i = 0; i < speeds.length; i++) {
                if (Math.abs(speed - speeds[i]) < 10) {
                    typeIndex = i;
                    break;
                }
            }
            double fuelBurnRate = burnRates[typeIndex];
            
            Document doc = new Document("callsign", callsign)
                .append("aircraftType", aircraftType)
                .append("fuel", fuel)
                .append("fuelBurnRate", fuelBurnRate)
                .append("speed", speed)
                .append("distance", distance)
                .append("origin", origin)
                .append("destination", destination)
                .append("status", status)
                .append("emergency", emergency)
                .append("fireTimeRemaining", 180.0)
                .append("runway", null)
                .append("timestamp", new Date());
            collection.replaceOne(
                new Document("callsign", callsign),
                doc,
                new com.mongodb.client.model.ReplaceOptions().upsert(true)
            );
        } catch (Exception e) {
            System.err.println("Failed to insert aircraft: " + e.getMessage());
        }
    }

    public List<Document> getAllActiveAircraft() {
        if (database == null) return new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("active_aircraft");
            List<Document> result = new ArrayList<>();
            collection.find().into(result);
            return result;
        } catch (Exception e) {
            System.err.println("Failed to get active aircraft: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Document> getAllRunways() {
        if (database == null) return new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("runways");
            List<Document> result = new ArrayList<>();
            collection.find().into(result);
            return result;
        } catch (Exception e) {
            System.err.println("Failed to get runways: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void updateActiveAircraft(String callsign, Document updates) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("active_aircraft");
            collection.updateOne(
                new Document("callsign", callsign),
                new Document("$set", updates)
            );
        } catch (Exception e) {
            System.err.println("Failed to update aircraft: " + e.getMessage());
        }
    }

    public void updateRunway(String runwayId, Document updates) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("runways");
            collection.updateOne(
                new Document("runwayId", runwayId),
                new Document("$set", updates)
            );
        } catch (Exception e) {
            System.err.println("Failed to update runway: " + e.getMessage());
        }
    }

    public void saveRunwayEvent(String event) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("runway_events");
            Document doc = new Document("event", event)
                .append("timestamp", new Date());
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Failed to save runway event: " + e.getMessage());
        }
    }

    public void saveEmergencyEvent(String callsign, String details) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("emergency_events");
            Document doc = new Document("callsign", callsign)
                .append("details", details)
                .append("timestamp", new Date());
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Failed to save emergency event: " + e.getMessage());
        }
    }



    public void saveWeatherEvent(String description, List<String> affectedRunways) {
        if (database == null) return;
        try {
            MongoCollection<Document> collection = database.getCollection("weather_events");
            Document doc = new Document("description", description)
                .append("affectedRunways", affectedRunways)
                .append("timestamp", new Date());
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Failed to save weather event: " + e.getMessage());
        }
    }

    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}
