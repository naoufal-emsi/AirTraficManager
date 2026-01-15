package com.atc.database;

import com.mongodb.client.*;
import org.bson.Document;
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

    public void clearAllRuntimeData() {
        database.getCollection("active_aircraft").deleteMany(new Document());
        database.getCollection("runways").deleteMany(new Document());
        database.getCollection("runway_events").deleteMany(new Document());
        database.getCollection("emergency_events").deleteMany(new Document());
    }

    // CRUD operations for active aircraft - ALL FROM DATABASE
    public void insertActiveAircraft(String callsign, String aircraftType, double fuel, double speed, double distance, String origin, String destination, String status, String emergency, int priority) {
        MongoCollection<Document> collection = database.getCollection("active_aircraft");
        Document doc = new Document("callsign", callsign)
            .append("aircraftType", aircraftType)
            .append("fuel", fuel)
            .append("speed", speed)
            .append("distance", distance)
            .append("origin", origin)
            .append("destination", destination)
            .append("status", status)
            .append("emergency", emergency)
            .append("priority", priority)
            .append("runway", null)
            .append("lastUpdate", new Date());
        collection.insertOne(doc);
    }

    public List<Document> getAllActiveAircraft() {
        return database.getCollection("active_aircraft").find().into(new ArrayList<>());
    }

    public Document getActiveAircraft(String callsign) {
        return database.getCollection("active_aircraft").find(new Document("callsign", callsign)).first();
    }

    public void updateActiveAircraft(String callsign, Document updates) {
        updates.append("lastUpdate", new Date());
        database.getCollection("active_aircraft").updateOne(
            new Document("callsign", callsign), 
            new Document("$set", updates)
        );
    }

    public void deleteActiveAircraft(String callsign) {
        database.getCollection("active_aircraft").deleteOne(new Document("callsign", callsign));
    }

    // CRUD operations for runways - ALL FROM DATABASE
    public void insertRunway(String runwayId, String status, String currentAircraft) {
        MongoCollection<Document> collection = database.getCollection("runways");
        Document doc = new Document("runwayId", runwayId)
            .append("status", status)
            .append("currentAircraft", currentAircraft)
            .append("lastUpdate", new Date());
        collection.replaceOne(new Document("runwayId", runwayId), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }

    public List<Document> getAllRunways() {
        return database.getCollection("runways").find().into(new ArrayList<>());
    }

    public Document getRunway(String runwayId) {
        return database.getCollection("runways").find(new Document("runwayId", runwayId)).first();
    }

    public void updateRunway(String runwayId, Document updates) {
        updates.append("lastUpdate", new Date());
        database.getCollection("runways").updateOne(
            new Document("runwayId", runwayId),
            new Document("$set", updates)
        );
    }

    // Initialize real data
    public void initializeAirportData() {
        MongoCollection<Document> airports = database.getCollection("airports");
        if (airports.countDocuments() == 0) {
            airports.insertMany(Arrays.asList(
                new Document("code", "LAX").append("name", "Los Angeles International")
                    .append("lat", 33.9425).append("lon", -118.4081)
                    .append("runways", Arrays.asList("06L/24R", "06R/24L", "07L/25R", "07R/25L"))
                    .append("elevation", 125).append("timezone", "PST"),
                new Document("code", "JFK").append("name", "John F Kennedy International")
                    .append("lat", 40.6413).append("lon", -73.7781)
                    .append("runways", Arrays.asList("04L/22R", "04R/22L", "08L/26R", "08R/26L"))
                    .append("elevation", 13).append("timezone", "EST"),
                new Document("code", "ORD").append("name", "Chicago O'Hare International")
                    .append("lat", 41.9742).append("lon", -87.9073)
                    .append("runways", Arrays.asList("04L/22R", "04R/22L", "09L/27R", "09R/27L"))
                    .append("elevation", 672).append("timezone", "CST")
            ));
        }
    }

    public void initializeAircraftTypes() {
        MongoCollection<Document> types = database.getCollection("aircraft_types");
        if (types.countDocuments() == 0) {
            types.insertMany(Arrays.asList(
                new Document("type", "B737-800").append("manufacturer", "Boeing")
                    .append("cruiseSpeed", 453).append("approachSpeed", 140)
                    .append("fuelCapacity", 26020).append("fuelBurnRate", 850)
                    .append("maxRange", 3383).append("passengers", 162),
                new Document("type", "A320").append("manufacturer", "Airbus")
                    .append("cruiseSpeed", 447).append("approachSpeed", 138)
                    .append("fuelCapacity", 24210).append("fuelBurnRate", 780)
                    .append("maxRange", 3300).append("passengers", 150),
                new Document("type", "B777-300ER").append("manufacturer", "Boeing")
                    .append("cruiseSpeed", 490).append("approachSpeed", 155)
                    .append("fuelCapacity", 181280).append("fuelBurnRate", 2500)
                    .append("maxRange", 7370).append("passengers", 396)
            ));
        }
    }

    // Generate and insert realistic flight directly to database
    public String generateAndInsertRealisticFlight() {
        Random rand = new Random();
        List<Document> airports = database.getCollection("airports").find().into(new ArrayList<>());
        List<Document> aircraftTypes = database.getCollection("aircraft_types").find().into(new ArrayList<>());
        
        if (airports.isEmpty() || aircraftTypes.isEmpty()) return null;
        
        Document origin = airports.get(rand.nextInt(airports.size()));
        Document destination = airports.get(rand.nextInt(airports.size()));
        Document aircraftType = aircraftTypes.get(rand.nextInt(aircraftTypes.size()));
        
        double distance = calculateDistance(
            origin.getDouble("lat"), origin.getDouble("lon"),
            destination.getDouble("lat"), destination.getDouble("lon")
        );
        
        double flightTime = distance / aircraftType.getInteger("cruiseSpeed");
        double fuelNeeded = flightTime * aircraftType.getInteger("fuelBurnRate");
        double fuelLevel = fuelNeeded + (rand.nextDouble() * 5000) + 2000;
        
        String callsign = generateRealisticCallsign();
        
        insertActiveAircraft(
            callsign,
            aircraftType.getString("type"),
            fuelLevel,
            aircraftType.getInteger("approachSpeed"),
            distance * 0.1,
            origin.getString("code"),
            destination.getString("code"),
            "APPROACHING",
            "NONE",
            100
        );
        
        return callsign;
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 3440.065;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
    
    private String generateRealisticCallsign() {
        String[] airlines = {"AAL", "UAL", "DAL", "SWA", "JBU", "ASA", "FFT", "SKW"};
        Random rand = new Random();
        return airlines[rand.nextInt(airlines.length)] + (rand.nextInt(9000) + 1000);
    }

    public void saveEmergencyEvent(String callsign, String details) {
        MongoCollection<Document> collection = database.getCollection("emergency_events");
        Document doc = new Document("callsign", callsign)
            .append("emergencyType", "FUEL_LOW")
            .append("priority", 25)
            .append("fuel", 0)
            .append("details", details)
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public void saveRunwayEvent(String event) {
        MongoCollection<Document> collection = database.getCollection("runway_events");
        Document doc = new Document("runwayId", "UNKNOWN")
            .append("status", "OCCUPIED")
            .append("event", event)
            .append("aircraft", null)
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }

    public List<Document> getEmergencyEvents() {
        return database.getCollection("emergency_events").find().sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public List<Document> getRunwayEvents(String runwayId) {
        return database.getCollection("runway_events").find(new Document("runwayId", runwayId))
            .sort(new Document("timestamp", -1)).into(new ArrayList<>());
    }

    public void close() {
        if (mongoClient != null) mongoClient.close();
    }
}ports.get(rand.nextInt(airports.size()));
        Document destination = airports.get(rand.nextInt(airports.size()));
        Document aircraftType = aircraftTypes.get(rand.nextInt(aircraftTypes.size()));
        
        double distance = calculateDistance(
            origin.getDouble("lat"), origin.getDouble("lon"),
            destination.getDouble("lat"), destination.getDouble("lon")
        );
        
        double flightTime = distance / aircraftType.getInteger("cruiseSpeed");
        double fuelNeeded = flightTime * aircraftType.getInteger("fuelBurnRate");
        double fuelLevel = fuelNeeded + (rand.nextDouble() * 5000) + 2000;
        
        String callsign = generateRealisticCallsign();
        
        insertActiveAircraft(
            callsign,
            aircraftType.getString("type"),
            fuelLevel,
            aircraftType.getInteger("approachSpeed"),
            distance * 0.1,
            origin.getString("code"),
            destination.getString("code"),
            "APPROACHING",
            "NONE",
            100
        );
        
        return callsign;
    }

    // Real airport data management
    public void initializeAirportData() {
        MongoCollection<Document> airports = database.getCollection("airports");
        if (airports.countDocuments() == 0) {
            // Add real airport data
            airports.insertMany(Arrays.asList(
                new Document("code", "LAX").append("name", "Los Angeles International")
                    .append("lat", 33.9425).append("lon", -118.4081)
                    .append("runways", Arrays.asList("06L/24R", "06R/24L", "07L/25R", "07R/25L"))
                    .append("elevation", 125).append("timezone", "PST"),
                new Document("code", "JFK").append("name", "John F Kennedy International")
                    .append("lat", 40.6413).append("lon", -73.7781)
                    .append("runways", Arrays.asList("04L/22R", "04R/22L", "08L/26R", "08R/26L"))
                    .append("elevation", 13).append("timezone", "EST"),
                new Document("code", "ORD").append("name", "Chicago O'Hare International")
                    .append("lat", 41.9742).append("lon", -87.9073)
                    .append("runways", Arrays.asList("04L/22R", "04R/22L", "09L/27R", "09R/27L"))
                    .append("elevation", 672).append("timezone", "CST")
            ));
        }
    }

    // Real aircraft type specifications
    public void initializeAircraftTypes() {
        MongoCollection<Document> types = database.getCollection("aircraft_types");
        if (types.countDocuments() == 0) {
            types.insertMany(Arrays.asList(
                new Document("type", "B737-800").append("manufacturer", "Boeing")
                    .append("cruiseSpeed", 453).append("approachSpeed", 140)
                    .append("fuelCapacity", 26020).append("fuelBurnRate", 850)
                    .append("maxRange", 3383).append("passengers", 162),
                new Document("type", "A320").append("manufacturer", "Airbus")
                    .append("cruiseSpeed", 447).append("approachSpeed", 138)
                    .append("fuelCapacity", 24210).append("fuelBurnRate", 780)
                    .append("maxRange", 3300).append("passengers", 150),
                new Document("type", "B777-300ER").append("manufacturer", "Boeing")
                    .append("cruiseSpeed", 490).append("approachSpeed", 155)
                    .append("fuelCapacity", 181280).append("fuelBurnRate", 2500)
                    .append("maxRange", 7370).append("passengers", 396)
            ));
        }
    }

    // Generate realistic flight based on current time and traffic patterns
    public Document generateRealisticFlight() {
        Random rand = new Random();
        List<Document> airports = database.getCollection("airports").find().into(new ArrayList<>());
        List<Document> aircraftTypes = database.getCollection("aircraft_types").find().into(new ArrayList<>());
        
        if (airports.isEmpty() || aircraftTypes.isEmpty()) return null;
        
        Document origin = airports.get(rand.nextInt(airports.size()));
        Document destination = airports.get(rand.nextInt(airports.size()));
        Document aircraftType = aircraftTypes.get(rand.nextInt(aircraftTypes.size()));
        
        // Calculate realistic distance and fuel requirements
        double distance = calculateDistance(
            origin.getDouble("lat"), origin.getDouble("lon"),
            destination.getDouble("lat"), destination.getDouble("lon")
        );
        
        double flightTime = distance / aircraftType.getInteger("cruiseSpeed");
        double fuelNeeded = flightTime * aircraftType.getInteger("fuelBurnRate");
        double fuelLevel = fuelNeeded + (rand.nextDouble() * 5000) + 2000; // Reserve fuel
        
        String callsign = generateRealisticCallsign();
        
        return new Document("callsign", callsign)
            .append("aircraftType", aircraftType.getString("type"))
            .append("fuel", fuelLevel)
            .append("speed", aircraftType.getInteger("approachSpeed"))
            .append("distance", distance * 0.1) // 10% of total distance remaining
            .append("origin", origin.getString("code"))
            .append("destination", destination.getString("code"))
            .append("status", "APPROACHING")
            .append("emergency", "NONE")
            .append("priority", 100)
            .append("created", new Date());
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 3440.065; // Earth radius in nautical miles
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
    
    private String generateRealisticCallsign() {
        String[] airlines = {"AAL", "UAL", "DAL", "SWA", "JBU", "ASA", "FFT", "SKW"};
        Random rand = new Random();
        return airlines[rand.nextInt(airlines.length)] + (rand.nextInt(9000) + 1000);
    }

    // Weather simulation
    public void updateWeatherConditions() {
        MongoCollection<Document> weather = database.getCollection("current_weather");
        weather.deleteMany(new Document());
        
        Random rand = new Random();
        List<String> conditions = Arrays.asList("CLEAR", "CLOUDY", "RAIN", "FOG", "STORM");
        String condition = conditions.get(rand.nextInt(conditions.size()));
        
        Document weatherDoc = new Document("condition", condition)
            .append("visibility", rand.nextInt(10) + 1)
            .append("windSpeed", rand.nextInt(30))
            .append("windDirection", rand.nextInt(360))
            .append("temperature", rand.nextInt(40) + 10)
            .append("timestamp", new Date());
            
        weather.insertOne(weatherDoc);
    }

    public Document getCurrentWeather() {
        return database.getCollection("current_weather").find().first();
    }

    public void close() {
        if (mongoClient != null) mongoClient.close();
    }
}
