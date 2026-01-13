package com.atc.part2.dao;

import com.atc.part2.models.WeatherAlert;
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

public class WeatherAlertDAO {
    private final MongoCollection<Document> alertCollection;

    public WeatherAlertDAO() {
        this.alertCollection = DatabaseManager.getInstance().getCollection("weather_alerts");
    }

    public void saveWeatherAlert(WeatherAlert alert) {
        Document doc = alertToDocument(alert);
        alertCollection.insertOne(doc);
    }

    public WeatherAlert findAlertById(String alertId) {
        Document doc = alertCollection.find(eq("alertId", alertId)).first();
        return doc != null ? documentToAlert(doc) : null;
    }

    public List<WeatherAlert> findActiveAlerts() {
        List<WeatherAlert> alerts = new ArrayList<>();
        alertCollection.find(eq("isActive", true)).forEach(doc -> alerts.add(documentToAlert(doc)));
        return alerts;
    }

    public void updateAlertStatus(String alertId, boolean isActive) {
        alertCollection.updateOne(eq("alertId", alertId), set("isActive", isActive));
    }

    private WeatherAlert documentToAlert(Document doc) {
        WeatherAlert alert = new WeatherAlert(
            doc.getString("alertId"),
            doc.getString("alertType"),
            doc.getString("severity"),
            doc.getString("affectedAirport")
        );
        alert.setDescription(doc.getString("description"));
        alert.setActive(doc.getBoolean("isActive", true));
        alert.setStartTime(dateToLocalDateTime(doc.getDate("startTime")));
        alert.setEndTime(dateToLocalDateTime(doc.getDate("endTime")));
        return alert;
    }

    private Document alertToDocument(WeatherAlert alert) {
        return new Document("alertId", alert.getAlertId())
            .append("alertType", alert.getAlertType())
            .append("severity", alert.getSeverity())
            .append("affectedAirport", alert.getAffectedAirport())
            .append("description", alert.getDescription())
            .append("isActive", alert.isActive())
            .append("startTime", localDateTimeToDate(alert.getStartTime()))
            .append("endTime", localDateTimeToDate(alert.getEndTime()));
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    private Date localDateTimeToDate(LocalDateTime dateTime) {
        return dateTime != null ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}