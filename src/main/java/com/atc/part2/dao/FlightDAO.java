package com.atc.part2.dao;

import com.atc.part2.models.Flight;
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

public class FlightDAO {
    private final MongoCollection<Document> flightCollection;

    public FlightDAO() {
        this.flightCollection = DatabaseManager.getInstance().getCollection("flights");
    }

    public void saveFlight(Flight flight) {
        Document doc = flightToDocument(flight);
        flightCollection.insertOne(doc);
    }

    public Flight findFlightById(String flightId) {
        Document doc = flightCollection.find(eq("flightId", flightId)).first();
        return doc != null ? documentToFlight(doc) : null;
    }

    public List<Flight> findAllFlights() {
        List<Flight> flights = new ArrayList<>();
        flightCollection.find().forEach(doc -> flights.add(documentToFlight(doc)));
        return flights;
    }

    public void updateFlightStatus(String flightId, String status) {
        flightCollection.updateOne(eq("flightId", flightId), set("status", status));
    }

    public void updateFlightDelay(String flightId, int delayMinutes, String reason) {
        flightCollection.updateOne(eq("flightId", flightId), 
            combine(set("delayMinutes", delayMinutes), set("delayReason", reason)));
    }

    public List<Flight> findFlightsByStatus(String status) {
        List<Flight> flights = new ArrayList<>();
        flightCollection.find(eq("status", status)).forEach(doc -> flights.add(documentToFlight(doc)));
        return flights;
    }

    public List<Flight> findDelayedFlights() {
        List<Flight> flights = new ArrayList<>();
        flightCollection.find(gt("delayMinutes", 0)).forEach(doc -> flights.add(documentToFlight(doc)));
        return flights;
    }

    public List<Flight> findFlightsAffectedByWeather() {
        List<Flight> flights = new ArrayList<>();
        flightCollection.find(eq("isAffectedByWeather", true)).forEach(doc -> flights.add(documentToFlight(doc)));
        return flights;
    }

    private Flight documentToFlight(Document doc) {
        Flight flight = new Flight(
            doc.getString("flightId"),
            doc.getString("flightNumber"),
            doc.getString("aircraftId"),
            doc.getString("origin"),
            doc.getString("destination"),
            dateToLocalDateTime(doc.getDate("scheduledDeparture"))
        );
        flight.setStatus(doc.getString("status"));
        flight.setDelayMinutes(doc.getInteger("delayMinutes", 0));
        flight.setDelayReason(doc.getString("delayReason"));
        flight.setAffectedByWeather(doc.getBoolean("isAffectedByWeather", false));
        return flight;
    }

    private Document flightToDocument(Flight flight) {
        return new Document("flightId", flight.getFlightId())
            .append("flightNumber", flight.getFlightNumber())
            .append("aircraftId", flight.getAircraftId())
            .append("origin", flight.getOrigin())
            .append("destination", flight.getDestination())
            .append("scheduledDeparture", localDateTimeToDate(flight.getScheduledDeparture()))
            .append("status", flight.getStatus())
            .append("delayMinutes", flight.getDelayMinutes())
            .append("delayReason", flight.getDelayReason())
            .append("isAffectedByWeather", flight.isAffectedByWeather());
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    private Date localDateTimeToDate(LocalDateTime dateTime) {
        return dateTime != null ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}