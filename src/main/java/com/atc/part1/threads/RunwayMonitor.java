package com.atc.part1.threads;

import com.atc.part1.managers.RunwayManager;
import com.atc.part1.models.Runway;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RunwayMonitor implements Runnable {
    private final RunwayManager runwayManager;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;

    public RunwayMonitor(RunwayManager runwayManager) {
        this.runwayManager = runwayManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = true;
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(() -> {
            if (running) {
                monitorRunways();
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    private void monitorRunways() {
        System.out.println("[RUNWAY MONITOR] Checking runway status...");
        for (Runway runway : runwayManager.getRunways().values()) {
            System.out.println("  " + runway.getRunwayId() + ": " + runway.getStatus() + 
                             " | Aircraft: " + (runway.getCurrentAircraft() != null ? runway.getCurrentAircraft() : "None"));
        }
        System.out.println("  Available permits: " + runwayManager.getAvailableRunways());
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }
}
