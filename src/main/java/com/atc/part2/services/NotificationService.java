package com.atc.part2.services;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.ArrayList;

public class NotificationService {
    private final ExecutorService notificationPool;
    private final BlockingQueue<String> notificationQueue;
    private final ConcurrentHashMap<String, List<String>> notificationHistory;

    public NotificationService() {
        this.notificationPool = Executors.newFixedThreadPool(3);
        this.notificationQueue = new LinkedBlockingQueue<>();
        this.notificationHistory = new ConcurrentHashMap<>();
    }

    public CompletableFuture<Void> sendWeatherNotification(Flight flight, WeatherAlert alert) {
        return CompletableFuture.runAsync(() -> {
            String message = formatWeatherMessage(flight, alert);
            notificationQueue.offer(message);
            logNotification(flight.getFlightId(), "WEATHER", message);
        }, notificationPool);
    }

    public CompletableFuture<Void> sendFuelAlert(String aircraftId, int fuelLevel) {
        return CompletableFuture.runAsync(() -> {
            String message = formatFuelMessage(aircraftId, fuelLevel);
            notificationQueue.offer(message);
            logNotification(aircraftId, "FUEL", message);
        }, notificationPool);
    }

    public CompletableFuture<Void> sendEmergencyFuelAlert(String aircraftId, int fuelLevel) {
        return CompletableFuture.runAsync(() -> {
            String message = "EMERGENCY: Aircraft " + aircraftId + " critical fuel: " + fuelLevel + "%";
            notificationQueue.offer(message);
            logNotification(aircraftId, "EMERGENCY", message);
        }, notificationPool);
    }

    public void broadcastToMultipleFlights(List<Flight> flights, String message) {
        flights.parallelStream()
            .forEach(flight -> sendNotification(flight.getFlightId(), message));
    }

    public void sendNotification(String flightId, String message) {
        CompletableFuture.runAsync(() -> {
            System.out.println("[NOTIFICATION] " + flightId + ": " + message);
            logNotification(flightId, "INFO", message);
        }, notificationPool);
    }

    public List<String> getNotificationHistory(String flightId) {
        return notificationHistory.getOrDefault(flightId, new ArrayList<>());
    }

    public void processNotificationQueue() {
        while (!notificationQueue.isEmpty()) {
            try {
                String notification = notificationQueue.take();
                System.out.println("[PROCESSING] " + notification);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        notificationPool.shutdown();
    }

    private String formatWeatherMessage(Flight flight, WeatherAlert alert) {
        return String.format("Weather Alert: Flight %s affected by %s (%s severity) at %s", 
            flight.getFlightNumber(), alert.getAlertType(), alert.getSeverity(), alert.getAffectedAirport());
    }

    private String formatFuelMessage(String aircraftId, int fuelLevel) {
        return String.format("Fuel Alert: Aircraft %s fuel level: %d%%", aircraftId, fuelLevel);
    }

    private void logNotification(String flightId, String type, String message) {
        notificationHistory.computeIfAbsent(flightId, k -> new ArrayList<>()).add(
            String.format("[%s] %s: %s", java.time.LocalDateTime.now(), type, message));
    }
}