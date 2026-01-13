package com.atc.shared.models;

import java.time.LocalDateTime;
import java.util.Map;

public class Event {
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String component;
    private String message;
    private Map<String, Object> details;
    private String severity;

    public Event(String eventType, String source, String component, String message, String severity) {
        this.eventType = eventType;
        this.source = source;
        this.component = component;
        this.message = message;
        this.severity = severity;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}