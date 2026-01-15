package com.atc.core;

import java.util.concurrent.atomic.AtomicLong;

public class SimulationClock {
    private static SimulationClock instance;
    private final AtomicLong currentTick = new AtomicLong(0);
    private double secondsPerTick;
    private double metersPerTick;
    
    private SimulationClock() {}
    
    public static synchronized SimulationClock getInstance() {
        if (instance == null) instance = new SimulationClock();
        return instance;
    }
    
    public void configure(double secondsPerTick, double metersPerTick) {
        this.secondsPerTick = secondsPerTick;
        this.metersPerTick = metersPerTick;
    }
    
    public void tick() {
        currentTick.incrementAndGet();
    }
    
    public void reset() {
        currentTick.set(0);
    }
    
    public long getCurrentTick() { return currentTick.get(); }
    public double getSecondsPerTick() { return secondsPerTick; }
    public double getMetersPerTick() { return metersPerTick; }
    public double getElapsedSeconds() { return currentTick.get() * secondsPerTick; }
}