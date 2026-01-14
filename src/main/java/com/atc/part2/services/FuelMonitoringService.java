package com.atc.part2.services;

import com.atc.part2.models.FuelAlert;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.UUID;
import java.util.ArrayList;

public class FuelMonitoringService {
    private final ConcurrentHashMap<String, FuelAlert> activeFuelAlerts;
    private final ConcurrentHashMap<String, Integer> aircraftFuelLevels;
    private final NotificationService notificationService;
    private final int LOW_FUEL_THRESHOLD = 20;
    private final int CRITICAL_FUEL_THRESHOLD = 10;

    public FuelMonitoringService(NotificationService notificationService) {
        this.activeFuelAlerts = new ConcurrentHashMap<>();
        this.aircraftFuelLevels = new ConcurrentHashMap<>();
        this.notificationService = notificationService;
    }

    public List<String> getLowFuelAircraft() {
        return aircraftFuelLevels.entrySet().stream()
            .filter(entry -> entry.getValue() <= LOW_FUEL_THRESHOLD && entry.getValue() > CRITICAL_FUEL_THRESHOLD)
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparing(aircraftFuelLevels::get))
            .collect(Collectors.toList());
    }

    public List<String> getCriticalFuelAircraft() {
        return aircraftFuelLevels.entrySet().stream()
            .filter(entry -> entry.getValue() <= CRITICAL_FUEL_THRESHOLD)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public Map<String, Long> getFuelStatisticsByStatus() {
        return aircraftFuelLevels.entrySet().stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getValue() <= CRITICAL_FUEL_THRESHOLD ? "CRITICAL" :
                        entry.getValue() <= LOW_FUEL_THRESHOLD ? "LOW" : "NORMAL",
                Collectors.counting()));
    }

    public OptionalDouble getAverageFuelLevel() {
        return aircraftFuelLevels.values().stream()
            .mapToInt(Integer::intValue)
            .average();
    }

    public List<String> getAircraftNeedingPriority() {
        return aircraftFuelLevels.entrySet().stream()
            .filter(entry -> entry.getValue() <= 15)
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparing(aircraftFuelLevels::get))
            .collect(Collectors.toList());
    }

    public int getFuelLevel(String aircraftId) {
        return aircraftFuelLevels.getOrDefault(aircraftId, 0);
    }

    public void updateFuelLevel(String aircraftId, int fuelLevel) {
        int oldLevel = aircraftFuelLevels.getOrDefault(aircraftId, 100);
        aircraftFuelLevels.put(aircraftId, fuelLevel);
        checkFuelThresholds(aircraftId, oldLevel, fuelLevel);
    }

    public FuelAlert createFuelAlert(String aircraftId, int fuelLevel, String alertLevel) {
        String alertId = "FA" + UUID.randomUUID().toString().substring(0, 8);
        FuelAlert alert = new FuelAlert(alertId, aircraftId, fuelLevel, alertLevel);
        activeFuelAlerts.put(alertId, alert);
        return alert;
    }

    public void escalateToEmergency(String aircraftId) {
        activeFuelAlerts.values().stream()
            .filter(alert -> aircraftId.equals(alert.getAircraftId()))
            .forEach(alert -> {
                alert.escalateToEmergency();
                notificationService.sendEmergencyFuelAlert(aircraftId, alert.getCurrentFuelLevel());
            });
    }

    public void resolveFuelAlert(String alertId, String resolution) {
        FuelAlert alert = activeFuelAlerts.get(alertId);
        if (alert != null) {
            alert.resolve(resolution);
        }
    }

    public List<FuelAlert> getActiveFuelAlerts() {
        return activeFuelAlerts.values().stream()
            .filter(alert -> !alert.isResolved())
            .collect(Collectors.toList());
    }

    public void processLowFuelAircraft() {
        getLowFuelAircraft().forEach(aircraftId -> 
            notificationService.sendFuelAlert(aircraftId, aircraftFuelLevels.get(aircraftId)));
    }

    public void checkAllFuelThresholds() {
        aircraftFuelLevels.entrySet().parallelStream()
            .forEach(entry -> checkFuelThresholds(entry.getKey(), 100, entry.getValue()));
    }

    private void checkFuelThresholds(String aircraftId, int oldLevel, int newLevel) {
        if (newLevel <= CRITICAL_FUEL_THRESHOLD && oldLevel > CRITICAL_FUEL_THRESHOLD) {
            createFuelAlert(aircraftId, newLevel, "CRITICAL");
            escalateToEmergency(aircraftId);
        } else if (newLevel <= LOW_FUEL_THRESHOLD && oldLevel > LOW_FUEL_THRESHOLD) {
            createFuelAlert(aircraftId, newLevel, "LOW");
            notificationService.sendFuelAlert(aircraftId, newLevel);
        }
    }
}