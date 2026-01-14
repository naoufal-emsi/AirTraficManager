package com.atc.shared.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DatabaseManager {
    private static DatabaseManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private boolean isConnected;
    
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "airTrafficManager";

    private DatabaseManager() {
        this.isConnected = false;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static void connect() {
        DatabaseManager manager = getInstance();
        try {
            manager.mongoClient = MongoClients.create(CONNECTION_STRING);
            manager.database = manager.mongoClient.getDatabase(DATABASE_NAME);
            manager.isConnected = true;
            System.out.println("✅ Connected to MongoDB: " + DATABASE_NAME);
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to MongoDB: " + e.getMessage());
            manager.isConnected = false;
        }
    }

    public static void disconnect() {
        DatabaseManager manager = getInstance();
        if (manager.mongoClient != null) {
            manager.mongoClient.close();
            manager.isConnected = false;
            System.out.println("✅ Disconnected from MongoDB");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        if (!isConnected || database == null) {
            throw new IllegalStateException("Database not connected");
        }
        return database.getCollection(collectionName);
    }

    public boolean testConnection() {
        try {
            if (database != null) {
                database.runCommand(new Document("ping", 1));
                return true;
            }
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
        }
        return false;
    }
}