package com.atc.core;

import com.atc.core.models.Aircraft;
import com.atc.core.models.Runway;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationManager {
    private static SimulationManager instance;
    private SimulationConfig config;
    private AtomicBoolean running = new AtomicBoolean(false);
    private ThreadPoolExecutor aircraftThreadPool;
    private List<Thread> workerThreads = new CopyOnWriteArrayList<>();
    
    private SimulationManager() {}
    
    public static synchronized SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }
    
    public void startSimulation(SimulationConfig newConfig) {
        if (running.get()) {
            stopSimulation();
        }
        
        this.config = newConfig;
        running.set(true);
        
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        aircraftThreadPool = new ThreadPoolExecutor(
            corePoolSize, corePoolSize * 2, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            r -> new Thread(r, "Aircraft-Worker")
        );
        
        System.out.println("✓ Simulation started: " + config.getMetersPerTimeUnit() + "m/unit, " + 
            config.getTimeStepSeconds() + "s/step");
    }
    
    public void stopSimulation() {
        if (!running.get()) return;
        
        running.set(false);
        
        for (Thread thread : workerThreads) {
            thread.interrupt();
        }
        workerThreads.clear();
        
        if (aircraftThreadPool != null) {
            aircraftThreadPool.shutdownNow();
            try {
                aircraftThreadPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("✓ Simulation stopped");
    }
    
    public void restartSimulation(SimulationConfig newConfig, 
                                 List<Aircraft> activeAircraft, 
                                 List<Runway> runways,
                                 PriorityBlockingQueue<Aircraft> landingQueue) {
        stopSimulation();
        
        activeAircraft.clear();
        landingQueue.clear();
        
        for (Runway runway : runways) {
            runway.releaseRunway();
            runway.setWeatherAffected(false);
        }
        
        startSimulation(newConfig);
        
        System.out.println("✓ Simulation restarted");
    }
    
    public void submitAircraftTask(Runnable task) {
        if (aircraftThreadPool != null && !aircraftThreadPool.isShutdown()) {
            aircraftThreadPool.submit(task);
        }
    }
    
    public void addWorkerThread(Thread thread) {
        workerThreads.add(thread);
    }
    
    public boolean isRunning() { return running.get(); }
    public SimulationConfig getConfig() { return config; }
}