package com.atc.part2.services;

import com.atc.part2.models.Flight;
import com.atc.part2.models.WeatherAlert;
import com.atc.part2.dao.FlightDAO;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.UUID;

public class WeatherService {
    private final ConcurrentHashMap<String, WeatherAlert> activeAlerts;
    private final FlightDAO flightDAO;
    private final NotificationService notificationService;

    public WeatherService(FlightDAO flightDAO, NotificationService notificationService) {
        this.activeAlerts = new ConcurrentHashMap<>();
        this.flightDAO = flightDAO;
        this.notificationService = notificationService;
    }

    public List<Flight> getAffectedFlights(WeatherAlert alert) {
        return flightDAO.findAllFlights().stream()
            .filter(flight -> flight.getOrigin().equals(alert.getAffectedAirport()) ||
                             flight.getDestination().equals(alert.getAffectedAirport()))
            .filter(flight -> !"LANDED".equals(flight.getStatus()))
            .collect(Collectors.toList());
    }

    public Map<String, List<Flight>> groupFlightsByAirport(List<Flight> flights) {
        return flights.stream().collect(Collectors.groupingBy(Flight::getOrigin));
    }

    public List<Flight> getDelayedFlightsByWeather() {
        return flightDAO.findAllFlights().stream()
            .filter(flight -> flight.getDelayMinutes() > 0)
            .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
            .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
            .collect(Collectors.toList());
    }

    public OptionalDouble getAverageDelayByWeather() {
        return flightDAO.findAllFlights().stream()
            .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
            .mapToInt(Flight::getDelayMinutes)
            .average();
    }

    public void applyWeatherDelays(WeatherAlert alert) {
        getAffectedFlights(alert).stream().forEach(flight -> {
            int delay = calculateDelayBySeverity(alert.getSeverity());
            flight.addDelay(delay, "WEATHER");
            flight.addWeatherAlert(alert.getAlertId());
            flightDAO.updateFlightDelay(flight.getFlightId(), flight.getDelayMinutes(), "WEATHER");
        });
    }

    public WeatherAlert createWeatherAlert(String type, String severity, String airport) {
        String alertId = "WA" + UUID.randomUUID().toString().substring(0, 8);
        WeatherAlert alert = new WeatherAlert(alertId, type, severity, airport);
        activeAlerts.put(alertId, alert);
        return alert;
    }

    public void activateAlert(String alertId) {
        WeatherAlert alert = activeAlerts.get(alertId);
        if (alert != null) {
            alert.setActive(true);
            applyWeatherDelays(alert);
            notifyAffectedFlights(alert);
        }
    }

    public void deactivateAlert(String alertId) {
        WeatherAlert alert = activeAlerts.get(alertId);
        if (alert != null) {
            alert.resolve();
        }
    }

    public List<WeatherAlert> getActiveAlerts() {
        return activeAlerts.values().stream()
            .filter(WeatherAlert::isActive)
            .collect(Collectors.toList());
    }

    public void notifyAffectedFlights(WeatherAlert alert) {
        getAffectedFlights(alert).forEach(flight -> 
            notificationService.sendWeatherNotification(flight, alert));
    }

    public List<String> getAirportsWithActiveAlerts() {
        return activeAlerts.values().stream()
            .filter(WeatherAlert::isActive)
            .map(WeatherAlert::getAffectedAirport)
            .distinct()
            .collect(Collectors.toList());
    }

    private int calculateDelayBySeverity(String severity) {
        return switch (severity) {
            case "LOW" -> 30;      // 30 min - minor delays
            case "MEDIUM" -> 90;   // 1.5 hours - moderate impact
            case "HIGH" -> 180;    // 3 hours - severe weather
            case "CRITICAL" -> 360; // 6 hours - airport closure
            default -> 0;
        };
    }
}