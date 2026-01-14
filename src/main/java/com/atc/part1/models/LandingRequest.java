package com.atc.part1.models;

import java.time.LocalDateTime;

public class LandingRequest implements Comparable<LandingRequest> {
    private Aircraft aircraft;
    private LocalDateTime requestTime;
    private int priority;

    public LandingRequest(Aircraft aircraft) {
        this.aircraft = aircraft;
        this.requestTime = LocalDateTime.now();
        this.priority = aircraft.getPriority();
    }

    @Override
    public int compareTo(LandingRequest other) {
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) return priorityCompare;
        return this.requestTime.compareTo(other.requestTime);
    }

    public Aircraft getAircraft() { return aircraft; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
