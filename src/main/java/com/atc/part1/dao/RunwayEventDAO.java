package com.atc.part1.dao;

import com.atc.shared.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class RunwayEventDAO {
    private final MongoCollection<Document> eventCollection;

    public RunwayEventDAO() {
        this.eventCollection = DatabaseManager.getInstance().getCollection("landing_events");
    }

    public void logEvent(String eventType, String aircraftId, String runwayId, String workerId) {
        Document event = new Document("eventType", eventType)
            .append("aircraftId", aircraftId)
            .append("runwayId", runwayId)
            .append("timestamp", Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
            .append("workerId", workerId)
            .append("details", new Document());
        
        eventCollection.insertOne(event);
    }
}
