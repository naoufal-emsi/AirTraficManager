package com.atc.part2.controllers;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.services.WeatherService;
import com.atc.part2.services.FuelMonitoringService;
import com.atc.part2.services.NotificationService;
import com.atc.part2.dao.FlightDAO;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Comparator;

public class FlightScheduler {
    private final ExecutorService flightThreadPool;
    private final ScheduledExecutorService monitorPool;
    private final ConcurrentHashMap<String, Flight> activeFlights;
    private final BlockingQueue<Flight> flightQueue;
    private final WeatherService weatherService;
    private final FuelMonitoringService fuelService;
    private final NotificationService notificationService;
    private final FlightDAO flightDAO;
    private final AtomicInteger totalFlights;
    private final AtomicInteger delayedFlightsCount;

    public FlightScheduler(WeatherService weatherService, FuelMonitoringService fuelService, 
                          NotificationService notificationService, FlightDAO flightDAO) {
        this.flightThreadPool = Executors.newFixedThreadPool(5);
        this.monitorPool = Executors.newScheduledThreadPool(2);
        this.activeFlights = new ConcurrentHashMap<>();
        this.flightQueue = new LinkedBlockingQueue<>();
        this.weatherService = weatherService;
        this.fuelService = fuelService;
        this.notificationService = notificationService;
        this.flightDAO = flightDAO;
        this.totalFlights = new AtomicInteger(0);
        this.delayedFlightsCount = new AtomicInteger(0);
    }

    public void scheduleFlight(Flight flight) {
        activeFlights.put(flight.getFlightId(), flight);
        flightQueue.offer(flight);
        flightDAO.saveFlight(flight);
        totalFlights.incrementAndGet();
    }

    public void processScheduledFlights() {
        activeFlights.values().stream()
            .filter(flight -> "SCHEDULED".equals(flight.getStatus()))
            .forEach(this::processFlight);
    }

    public List<Flight> getDelayedFlights() {
        return activeFlights.values().stream()
            .filter(flight -> flight.getDelayMinutes() > 0)
            .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
            .collect(Collectors.toList());
    }

    public void handleWeatherImpact(WeatherAlert alert) {
        weatherService.getAffectedFlights(alert).stream()
            .forEach(flight -> applyWeatherDelay(flight, alert));
    }

    public Map<String, List<Flight>> getFlightsByStatus() {
        return activeFlights.values().stream()
            .collect(Collectors.groupingBy(Flight::getStatus));
    }

    public void rescheduleDelayedFlights() {
        getDelayedFlights().stream()
            .filter(flight -> flight.getDelayMinutes() > 60)
            .forEach(this::rescheduleToNextSlot);
    }

    public void startFlightWorkers() {
        for (int i = 0; i < 5; i++) {
            flightThreadPool.submit(new FlightWorker(i));
        }
    }

    public void notifyDelayedPassengers() {
        getDelayedFlights().forEach(flight -> 
            notificationService.sendNotification(flight.getFlightId(), 
                "Flight delayed by " + flight.getDelayMinutes() + " minutes"));
    }

    public void updateFlightStatuses() {
        activeFlights.values().parallelStream()
            .forEach(this::updateFlightStatus);
    }

    public Map<String, Object> getFlightStatistics() {
        long totalCount = activeFlights.size();
        long onTimeCount = activeFlights.values().stream()
            .filter(flight -> flight.getDelayMinutes() == 0)
            .count();
        double averageDelay = activeFlights.values().stream()
            .mapToInt(Flight::getDelayMinutes)
            .average().orElse(0.0);
        
        return Map.of(
            "totalFlights", totalCount,
            "onTimePercentage", totalCount > 0 ? (double) onTimeCount / totalCount * 100 : 0,
            "averageDelay", averageDelay,
            "flightsByStatus", getFlightsByStatus()
        );
    }

    public void shutdown() {
        flightThreadPool.shutdown();
        monitorPool.shutdown();
        notificationService.shutdown();
    }

    private void processFlight(Flight flight) {
        flight.setStatus("IN_FLIGHT");
        flightDAO.updateFlightStatus(flight.getFlightId(), "IN_FLIGHT");
    }

    private void applyWeatherDelay(Flight flight, WeatherAlert alert) {
        int delay = calculateWeatherDelay(alert.getSeverity());
        flight.addDelay(delay, "WEATHER");
        flightDAO.updateFlightDelay(flight.getFlightId(), flight.getDelayMinutes(), "WEATHER");
        delayedFlightsCount.incrementAndGet();
    }

    private void rescheduleToNextSlot(Flight flight) {
        flight.setStatus("RESCHEDULED");
        flightDAO.updateFlightStatus(flight.getFlightId(), "RESCHEDULED");
    }

    private void updateFlightStatus(Flight flight) {
        flightDAO.updateFlightStatus(flight.getFlightId(), flight.getStatus());
    }

    private int calculateWeatherDelay(String severity) {
        return switch (severity) {
            case "LOW" -> 15;
            case "MEDIUM" -> 30;
            case "HIGH" -> 60;
            case "CRITICAL" -> 120;
            default -> 0;
        };
    }

    private class FlightWorker implements Runnable {
        private final int workerId;
        
        public FlightWorker(int workerId) {
            this.workerId = workerId;
        }
        
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Flight flight = flightQueue.take();
                    processFlight(flight);
                    Thread.sleep(1000); // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public ConcurrentHashMap<String, Flight> getActiveFlights() {
        return activeFlights;
    }

    public BlockingQueue<Flight> getFlightQueue() {
        return flightQueue;
    }
}