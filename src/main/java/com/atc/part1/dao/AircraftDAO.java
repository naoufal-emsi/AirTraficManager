package com.atc.part1.dao;

import com.atc.part1.models.Aircraft;
import com.atc.shared.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class AircraftDAO {
    private final MongoCollection<Document> aircraftCollection;

    public AircraftDAO() {
        this.aircraftCollection = DatabaseManager.getInstance().getCollection("aircraft");
    }

    public void saveAircraft(Aircraft aircraft) {
        Document doc = aircraftToDocument(aircraft);
        aircraftCollection.insertOne(doc);
    }

    public Aircraft findAircraftById(String aircraftId) {
        Document doc = aircraftCollection.find(eq("aircraftId", aircraftId)).first();
        return doc != null ? documentToAircraft(doc) : null;
    }

    public List<Aircraft> findAllAircraft() {
        List<Aircraft> aircraft = new ArrayList<>();
        aircraftCollection.find().forEach(doc -> aircraft.add(documentToAircraft(doc)));
        return aircraft;
    }

    public void updateAircraftStatus(String aircraftId, String status) {
        aircraftCollection.updateOne(eq("aircraftId", aircraftId), set("status", status));
    }

    private Aircraft documentToAircraft(Document doc) {
        Aircraft aircraft = new Aircraft(
            doc.getString("aircraftId"),
            doc.getString("callsign"),
            doc.getInteger("fuelLevel", 100)
        );
        aircraft.setStatus(doc.getString("status"));
        aircraft.setEmergency(doc.getBoolean("isEmergency", false));
        aircraft.setPriority(doc.getInteger("priority", 3));
        aircraft.setAssignedRunway(doc.getString("assignedRunway"));
        return aircraft;
    }

    private Document aircraftToDocument(Aircraft aircraft) {
        return new Document("aircraftId", aircraft.getAircraftId())
            .append("callsign", aircraft.getCallsign())
            .append("fuelLevel", aircraft.getFuelLevel())
            .append("status", aircraft.getStatus())
            .append("isEmergency", aircraft.isEmergency())
            .append("priority", aircraft.getPriority())
            .append("assignedRunway", aircraft.getAssignedRunway());
    }
}
