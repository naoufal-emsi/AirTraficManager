# Part 2: Flight Operations & Scheduling System
## Technical Specifications Document

**Assigned to:** You  
**Focus:** Flight scheduling, weather management, fuel monitoring, operations coordination  
**Java Concepts:** Streams, Lambdas, Thread Pools, Concurrent Collections, CompletableFuture
 
---

## 1. FOLDER STRUCTURE

```
src/main/java/com/atc/
├── part2/
│   ├── controllers/
│   │   ├── FlightScheduler.java
│   │   └── WeatherController.java
│   ├── services/
│   │   ├── WeatherService.java
│   │   ├── FuelMonitoringService.java
│   │   └── NotificationService.java
│   ├── models/
│   │   ├── Flight.java
│   │   ├── WeatherAlert.java
│   │   └── FuelAlert.java
│   ├── threads/
│   │   ├── FlightWorker.java
│   │   ├── WeatherMonitor.java
│   │   └── FuelMonitor.java
│   └── dao/
│       ├── FlightDAO.java
│       └── WeatherAlertDAO.java
└── gui/
    ├── AirTrafficGUI.java
    └── controllers/
        └── MainController.java
```

---

## 2. DETAILED CLASS SPECIFICATIONS

### 2.1 Flight.java (Model)
```java
package com.atc.part2.models;

public class Flight {
    // Fields
    private String flightId;             // "FL001", "FL002"
    private String flightNumber;         // "BA101", "UA202"
    private Aircraft aircraft;           // Associated aircraft
    private String origin;               // "JFK", "LAX"
    private String destination;          // "LHR", "CDG"
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private String status;               // "SCHEDULED", "DELAYED", "CANCELLED", "IN_FLIGHT", "LANDED"
    private int delayMinutes;            // Delay in minutes
    private String delayReason;          // "WEATHER", "FUEL", "TECHNICAL"
    private List<String> weatherAlerts;  // Associated weather alerts
    private boolean isAffectedByWeather; // Weather impact flag
    
    // Constructors
    public Flight(String flightId, String flightNumber, Aircraft aircraft, 
                  String origin, String destination, LocalDateTime scheduledDeparture)
    public Flight(String flightId, String flightNumber, String origin, String destination)
    
    // Methods (ALL getters/setters required)
    public String getFlightId()
    public void setFlightId(String flightId)
    public String getFlightNumber()
    public void setFlightNumber(String flightNumber)
    public Aircraft getAircraft()
    public void setAircraft(Aircraft aircraft)
    public String getOrigin()
    public void setOrigin(String origin)
    public String getDestination()
    public void setDestination(String destination)
    public LocalDateTime getScheduledDeparture()
    public void setScheduledDeparture(LocalDateTime scheduledDeparture)
    public LocalDateTime getScheduledArrival()
    public void setScheduledArrival(LocalDateTime scheduledArrival)
    public LocalDateTime getActualDeparture()
    public void setActualDeparture(LocalDateTime actualDeparture)
    public LocalDateTime getActualArrival()
    public void setActualArrival(LocalDateTime actualArrival)
    public String getStatus()
    public void setStatus(String status)
    public int getDelayMinutes()
    public void setDelayMinutes(int delayMinutes)
    public String getDelayReason()
    public void setDelayReason(String delayReason)
    public List<String> getWeatherAlerts()
    public void setWeatherAlerts(List<String> weatherAlerts)
    public boolean isAffectedByWeather()
    public void setAffectedByWeather(boolean affectedByWeather)
    
    // Business Methods
    public boolean isDelayed()           // Returns delayMinutes > 0
    public boolean isScheduledToday()    // Check if scheduled for today
    public void addDelay(int minutes, String reason) // Add delay and reason
    public void cancelFlight(String reason) // Set status to CANCELLED
    public Duration getFlightDuration()  // Calculate flight duration
    public boolean isDeparted()          // Check if actualDeparture is set
    public boolean hasLanded()           // Check if actualArrival is set
    public void addWeatherAlert(String alertId) // Add weather alert to list
    public String toString()             // Format: "Flight[FL001: BA101 JFK->LHR, Status: SCHEDULED]"
}
```

### 2.2 WeatherAlert.java (Model)
```java
package com.atc.part2.models;

public class WeatherAlert {
    // Fields
    private String alertId;              // UUID
    private String alertType;            // "STORM", "FOG", "WIND", "SNOW"
    private String severity;             // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private String affectedAirport;      // Airport code
    private List<String> affectedRunways; // Runway IDs
    private LocalDateTime startTime;     // Alert start time
    private LocalDateTime endTime;       // Expected end time
    private String description;          // Human readable description
    private boolean isActive;            // Alert status
    private List<String> affectedFlights; // Flight IDs affected
    
    // Constructors
    public WeatherAlert(String alertType, String severity, String affectedAirport)
    public WeatherAlert(String alertType, String severity, String affectedAirport, 
                       LocalDateTime startTime, LocalDateTime endTime)
    
    // Methods (ALL getters/setters)
    public String getAlertId()
    public void setAlertId(String alertId)
    public String getAlertType()
    public void setAlertType(String alertType)
    public String getSeverity()
    public void setSeverity(String severity)
    public String getAffectedAirport()
    public void setAffectedAirport(String affectedAirport)
    public List<String> getAffectedRunways()
    public void setAffectedRunways(List<String> affectedRunways)
    public LocalDateTime getStartTime()
    public void setStartTime(LocalDateTime startTime)
    public LocalDateTime getEndTime()
    public void setEndTime(LocalDateTime endTime)
    public String getDescription()
    public void setDescription(String description)
    public boolean isActive()
    public void setActive(boolean active)
    public List<String> getAffectedFlights()
    public void setAffectedFlights(List<String> affectedFlights)
    
    // Business Methods
    public boolean isCritical()          // Returns severity.equals("CRITICAL")
    public boolean isCurrentlyActive()   // Check if current time is within start/end
    public void activate()               // Set isActive=true, startTime=now
    public void deactivate()             // Set isActive=false, endTime=now
    public void addAffectedFlight(String flightId) // Add flight to affected list
    public Duration getDuration()        // Calculate alert duration
    public boolean affectsRunway(String runwayId) // Check if runway is affected
}
```

### 2.3 FuelAlert.java (Model)
```java
package com.atc.part2.models;

public class FuelAlert {
    // Fields
    private String alertId;              // UUID
    private String aircraftId;           // Aircraft ID
    private String flightId;             // Flight ID
    private int currentFuelLevel;        // Current fuel percentage
    private int criticalThreshold;       // Critical fuel threshold (default 10%)
    private int lowThreshold;            // Low fuel threshold (default 20%)
    private LocalDateTime alertTime;     // When alert was generated
    private String alertLevel;           // "LOW", "CRITICAL", "EMERGENCY"
    private boolean isResolved;          // Alert resolution status
    private String resolution;           // How alert was resolved
    
    // Constructors
    public FuelAlert(String aircraftId, String flightId, int currentFuelLevel)
    public FuelAlert(String aircraftId, int currentFuelLevel, int criticalThreshold, int lowThreshold)
    
    // Methods (ALL getters/setters)
    public String getAlertId()
    public String getAircraftId()
    public String getFlightId()
    public int getCurrentFuelLevel()
    public int getCriticalThreshold()
    public int getLowThreshold()
    public LocalDateTime getAlertTime()
    public String getAlertLevel()
    public boolean isResolved()
    public String getResolution()
    // ... setters
    
    // Business Methods
    public boolean isCritical()          // Returns currentFuelLevel <= criticalThreshold
    public boolean isLow()               // Returns currentFuelLevel <= lowThreshold
    public boolean isEmergency()         // Returns currentFuelLevel <= 5%
    public void resolve(String resolution) // Mark alert as resolved
    public void escalate()               // Increase alert level
    public String getRecommendedAction() // Return recommended action based on fuel level
}
```

---

## 3. THREAD SPECIFICATIONS

### 3.1 FlightWorker.java (Thread Class)
```java
package com.atc.part2.threads;

public class FlightWorker implements Runnable {
    // Fields
    private final BlockingQueue<Flight> flightQueue;
    private final FlightScheduler scheduler;
    private final String workerId;
    private volatile boolean running;
    private final CompletableFuture<Void> completionFuture;
    
    // Constructor
    public FlightWorker(BlockingQueue<Flight> flightQueue, FlightScheduler scheduler, String workerId)
    
    // Thread Methods
    @Override
    public void run()                    // Main thread execution
    public void stop()                   // Graceful shutdown
    public CompletableFuture<Void> getCompletionFuture() // For async coordination
    
    // Business Methods
    private void processFlight(Flight flight)
    private void checkFlightStatus(Flight flight)
    private void updateFlightInDatabase(Flight flight)
    private void handleDelayedFlight(Flight flight)
    private void notifyPassengers(Flight flight) // Simulate passenger notification
}

// THREAD USAGE:
// - 5 FlightWorker threads running continuously
// - Each thread processes flights from flightQueue
// - Updates flight status and schedules
// - Handles flight delays and cancellations
// - Uses CompletableFuture for async operations
```

### 3.2 WeatherMonitor.java (Thread Class)
```java
package com.atc.part2.threads;

public class WeatherMonitor implements Runnable {
    // Fields
    private final WeatherService weatherService;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, WeatherAlert> activeAlerts;
    private volatile boolean running;
    
    // Constructor
    public WeatherMonitor(WeatherService weatherService)
    
    // Thread Methods
    @Override
    public void run()                    // Monitors weather every 30 seconds
    public void stop()
    
    // Business Methods
    private void checkWeatherConditions()
    private void generateWeatherAlert(String airport, String condition)
    private void updateExistingAlerts()
    private void notifyAffectedFlights(WeatherAlert alert)
    private void simulateWeatherChange() // Random weather generation for testing
}

// THREAD USAGE:
// - 1 WeatherMonitor thread using ScheduledExecutorService
// - Runs every 30 seconds
// - Generates random weather alerts for simulation
// - Updates existing alerts
// - Notifies affected flights using streams/lambdas
```

### 3.3 FuelMonitor.java (Thread Class)
```java
package com.atc.part2.threads;

public class FuelMonitor implements Runnable {
    // Fields
    private final FuelMonitoringService fuelService;
    private final List<Aircraft> monitoredAircraft;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;
    
    // Constructor
    public FuelMonitor(FuelMonitoringService fuelService, List<Aircraft> aircraft)
    
    // Thread Methods
    @Override
    public void run()                    // Monitors fuel every 10 seconds
    public void stop()
    
    // Business Methods
    private void checkFuelLevels()       // Check all aircraft fuel levels
    private void generateFuelAlert(Aircraft aircraft) // Create fuel alert
    private void simulateFuelConsumption() // Decrease fuel levels over time
    private void escalateEmergencies()   // Handle critical fuel situations
}

// THREAD USAGE:
// - 1 FuelMonitor thread using ScheduledExecutorService
// - Runs every 10 seconds
// - Monitors all aircraft fuel levels
// - Generates fuel alerts when thresholds exceeded
// - Simulates fuel consumption during flight
```

---

## 4. SERVICE CLASSES WITH STREAMS & LAMBDAS

### 4.1 WeatherService.java (Core Service)
```java
package com.atc.part2.services;

public class WeatherService {
    // Fields
    private final ConcurrentHashMap<String, WeatherAlert> activeAlerts;
    private final List<String> airports;
    private final WeatherAlertDAO weatherDAO;
    private final NotificationService notificationService;
    
    // Constructor
    public WeatherService(List<String> airports, WeatherAlertDAO weatherDAO)
    
    // STREAMS & LAMBDAS METHODS
    public List<Flight> getAffectedFlights(WeatherAlert alert)
    // STREAM USAGE: Filter flights by origin/destination matching alert airport
    // flights.stream()
    //     .filter(flight -> flight.getOrigin().equals(alert.getAffectedAirport()) ||
    //                      flight.getDestination().equals(alert.getAffectedAirport()))
    //     .filter(flight -> !flight.getStatus().equals("LANDED"))
    //     .collect(Collectors.toList());
    
    public List<Flight> getFlightsByWeatherSeverity(String severity)
    // STREAM USAGE: Filter flights affected by specific weather severity
    // flights.stream()
    //     .filter(flight -> flight.getWeatherAlerts().stream()
    //         .anyMatch(alertId -> getAlertById(alertId).getSeverity().equals(severity)))
    //     .collect(Collectors.toList());
    
    public Map<String, List<Flight>> groupFlightsByAirport(List<Flight> flights)
    // STREAM USAGE: Group flights by origin airport
    // flights.stream()
    //     .collect(Collectors.groupingBy(Flight::getOrigin));
    
    public List<Flight> getDelayedFlightsByWeather()
    // STREAM USAGE: Find all flights delayed due to weather
    // flights.stream()
    //     .filter(Flight::isDelayed)
    //     .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
    //     .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
    //     .collect(Collectors.toList());
    
    public OptionalDouble getAverageDelayByWeather()
    // STREAM USAGE: Calculate average weather delay
    // flights.stream()
    //     .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
    //     .mapToInt(Flight::getDelayMinutes)
    //     .average();
    
    public void applyWeatherDelays(WeatherAlert alert)
    // STREAM USAGE: Apply delays to affected flights
    // getAffectedFlights(alert).stream()
    //     .forEach(flight -> {
    //         int delay = calculateDelayBySeverity(alert.getSeverity());
    //         flight.addDelay(delay, "WEATHER");
    //         updateFlightStatus(flight);
    //     });
    
    // WEATHER OPERATIONS
    public WeatherAlert createWeatherAlert(String type, String severity, String airport)
    public void activateAlert(String alertId)
    public void deactivateAlert(String alertId)
    public List<WeatherAlert> getActiveAlerts()
    public void processWeatherUpdate(String airport, String condition)
    
    // LAMBDA USAGE EXAMPLES
    public void notifyAffectedFlights(WeatherAlert alert)
    // getAffectedFlights(alert).forEach(flight -> 
    //     notificationService.sendWeatherNotification(flight, alert));
    
    public List<String> getAirportsWithActiveAlerts()
    // activeAlerts.values().stream()
    //     .filter(WeatherAlert::isActive)
    //     .map(WeatherAlert::getAffectedAirport)
    //     .distinct()
    //     .collect(Collectors.toList());
}
```

### 4.2 FuelMonitoringService.java (Core Service)
```java
package com.atc.part2.services;

public class FuelMonitoringService {
    // Fields
    private final ConcurrentHashMap<String, FuelAlert> activeFuelAlerts;
    private final List<Aircraft> monitoredAircraft;
    private final NotificationService notificationService;
    
    // Constructor
    public FuelMonitoringService(List<Aircraft> aircraft)
    
    // STREAMS & LAMBDAS METHODS
    public List<Aircraft> getLowFuelAircraft()
    // STREAM USAGE: Filter aircraft with low fuel
    // monitoredAircraft.stream()
    //     .filter(aircraft -> aircraft.getFuelLevel() <= 20)
    //     .filter(aircraft -> !aircraft.getStatus().equals("LANDED"))
    //     .sorted(Comparator.comparing(Aircraft::getFuelLevel))
    //     .collect(Collectors.toList());
    
    public List<Aircraft> getCriticalFuelAircraft()
    // STREAM USAGE: Filter aircraft with critical fuel
    // monitoredAircraft.stream()
    //     .filter(aircraft -> aircraft.getFuelLevel() <= 10)
    //     .collect(Collectors.toList());
    
    public Map<String, Long> getFuelStatisticsByStatus()
    // STREAM USAGE: Group aircraft by fuel status
    // monitoredAircraft.stream()
    //     .collect(Collectors.groupingBy(
    //         aircraft -> aircraft.getFuelLevel() <= 10 ? "CRITICAL" :
    //                    aircraft.getFuelLevel() <= 20 ? "LOW" : "NORMAL",
    //         Collectors.counting()));
    
    public OptionalDouble getAverageFuelLevel()
    // STREAM USAGE: Calculate average fuel level
    // monitoredAircraft.stream()
    //     .mapToInt(Aircraft::getFuelLevel)
    //     .average();
    
    public List<Aircraft> getAircraftNeedingPriority()
    // STREAM USAGE: Find aircraft needing landing priority due to fuel
    // monitoredAircraft.stream()
    //     .filter(aircraft -> aircraft.getFuelLevel() <= 15)
    //     .filter(aircraft -> aircraft.getStatus().equals("APPROACHING"))
    //     .sorted(Comparator.comparing(Aircraft::getFuelLevel))
    //     .collect(Collectors.toList());
    
    public void updateFuelLevels()
    // STREAM USAGE: Update fuel for all in-flight aircraft
    // monitoredAircraft.stream()
    //     .filter(aircraft -> aircraft.getStatus().equals("AIRBORNE"))
    //     .forEach(aircraft -> {
    //         int newLevel = aircraft.getFuelLevel() - 2; // Consume 2% per update
    //         aircraft.setFuelLevel(Math.max(0, newLevel));
    //         checkFuelThresholds(aircraft);
    //     });
    
    // FUEL MONITORING OPERATIONS
    public FuelAlert createFuelAlert(Aircraft aircraft)
    public void escalateToEmergency(Aircraft aircraft)
    public void resolveFuelAlert(String alertId, String resolution)
    public List<FuelAlert> getActiveFuelAlerts()
    
    // LAMBDA USAGE EXAMPLES
    public void processLowFuelAircraft()
    // getLowFuelAircraft().forEach(aircraft -> 
    //     notificationService.sendFuelAlert(aircraft));
    
    public void checkAllFuelThresholds()
    // monitoredAircraft.parallelStream()
    //     .forEach(this::checkFuelThresholds);
}
```

### 4.3 NotificationService.java (Communication Service)
```java
package com.atc.part2.services;

public class NotificationService {
    // Fields
    private final ExecutorService notificationPool;
    private final BlockingQueue<String> notificationQueue;
    private final ConcurrentHashMap<String, List<String>> subscribedFlights;
    
    // Constructor
    public NotificationService()
    
    // NOTIFICATION METHODS WITH LAMBDAS
    public CompletableFuture<Void> sendWeatherNotification(Flight flight, WeatherAlert alert)
    // LAMBDA USAGE: Async notification sending
    // return CompletableFuture.runAsync(() -> {
    //     String message = formatWeatherMessage(flight, alert);
    //     notificationQueue.offer(message);
    //     logNotification(flight.getFlightId(), "WEATHER", message);
    // }, notificationPool);
    
    public CompletableFuture<Void> sendFuelAlert(Aircraft aircraft)
    // LAMBDA USAGE: Async fuel alert
    // return CompletableFuture.runAsync(() -> {
    //     String message = formatFuelMessage(aircraft);
    //     notificationQueue.offer(message);
    //     logNotification(aircraft.getAircraftId(), "FUEL", message);
    // }, notificationPool);
    
    public void broadcastToMultipleFlights(List<Flight> flights, String message)
    // LAMBDA USAGE: Broadcast to multiple flights
    // flights.parallelStream()
    //     .forEach(flight -> sendNotification(flight.getFlightId(), message));
    
    public void notifyByCondition(Predicate<Flight> condition, String message)
    // LAMBDA USAGE: Conditional notifications
    // getAllFlights().stream()
    //     .filter(condition)
    //     .forEach(flight -> sendNotification(flight.getFlightId(), message));
    
    // NOTIFICATION OPERATIONS
    public void sendNotification(String flightId, String message)
    public void subscribeToNotifications(String flightId, String notificationType)
    public void unsubscribeFromNotifications(String flightId, String notificationType)
    public List<String> getNotificationHistory(String flightId)
    public void processNotificationQueue()
}
```

---

## 5. CONTROLLER CLASSES

### 5.1 FlightScheduler.java (Main Controller)
```java
package com.atc.part2.controllers;

public class FlightScheduler {
    // THREAD POOLS
    private final ExecutorService flightThreadPool;     // 5 threads for flight operations
    private final ExecutorService weatherThreadPool;    // 2 threads for weather processing
    private final ScheduledExecutorService monitorPool; // 2 threads for monitoring
    
    // CONCURRENT COLLECTIONS
    private final ConcurrentHashMap<String, Flight> activeFlights;
    private final BlockingQueue<Flight> flightQueue;
    private final ConcurrentLinkedQueue<String> delayedFlights;
    
    // SERVICES
    private final WeatherService weatherService;
    private final FuelMonitoringService fuelService;
    private final NotificationService notificationService;
    
    // ATOMIC COUNTERS
    private final AtomicInteger totalFlights;
    private final AtomicInteger delayedFlights;
    private final AtomicInteger cancelledFlights;
    
    // Constructor
    public FlightScheduler(WeatherService weatherService, FuelMonitoringService fuelService)
    
    // MAIN OPERATIONS WITH STREAMS
    public void scheduleFlight(Flight flight)
    // - Add flight to activeFlights map
    // - Add to flightQueue for processing
    // - Log scheduling event
    
    public void processScheduledFlights()
    // STREAM USAGE: Process flights scheduled for today
    // activeFlights.values().stream()
    //     .filter(Flight::isScheduledToday)
    //     .filter(flight -> flight.getStatus().equals("SCHEDULED"))
    //     .forEach(this::processFlight);
    
    public List<Flight> getDelayedFlights()
    // STREAM USAGE: Get all delayed flights
    // activeFlights.values().stream()
    //     .filter(Flight::isDelayed)
    //     .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
    //     .collect(Collectors.toList());
    
    public void handleWeatherImpact(WeatherAlert alert)
    // STREAM USAGE: Handle weather impact on flights
    // weatherService.getAffectedFlights(alert).stream()
    //     .forEach(flight -> applyWeatherDelay(flight, alert));
    
    public Map<String, List<Flight>> getFlightsByStatus()
    // STREAM USAGE: Group flights by status
    // activeFlights.values().stream()
    //     .collect(Collectors.groupingBy(Flight::getStatus));
    
    public void rescheduleDelayedFlights()
    // STREAM USAGE: Reschedule flights with delays
    // getDelayedFlights().stream()
    //     .filter(flight -> flight.getDelayMinutes() > 60)
    //     .forEach(this::rescheduleToNextSlot);
    
    // THREAD MANAGEMENT
    public void startFlightWorkers()                    // Start 5 FlightWorker threads
    public void startWeatherMonitoring()               // Start WeatherMonitor thread
    public void startFuelMonitoring()                  // Start FuelMonitor thread
    public void shutdown()                              // Graceful shutdown
    
    // STATISTICS WITH STREAMS
    public FlightStatistics getFlightStatistics()
    // Calculate statistics using streams:
    // - Total flights: activeFlights.size()
    // - On-time percentage: flights.stream().filter(not delayed).count() / total
    // - Average delay: flights.stream().mapToInt(delay).average()
    // - Flights by status: groupingBy(status)
    
    // LAMBDA USAGE EXAMPLES
    public void notifyDelayedPassengers()
    // getDelayedFlights().forEach(flight -> 
    //     notificationService.sendDelayNotification(flight));
    
    public void updateFlightStatuses()
    // activeFlights.values().parallelStream()
    //     .forEach(this::updateFlightStatus);
}

// THREAD POOL CONFIGURATION:
// flightThreadPool = Executors.newFixedThreadPool(5)
// weatherThreadPool = Executors.newFixedThreadPool(2)  
// monitorPool = Executors.newScheduledThreadPool(2)
```

### 5.2 WeatherController.java (Weather Management)
```java
package com.atc.part2.controllers;

public class WeatherController {
    // Fields
    private final WeatherService weatherService;
    private final FlightScheduler flightScheduler;
    private final ExecutorService weatherPool;           // 2 threads for weather processing
    
    // Constructor
    public WeatherController(WeatherService weatherService, FlightScheduler flightScheduler)
    
    // WEATHER OPERATIONS WITH STREAMS
    public void processWeatherAlert(WeatherAlert alert)
    // - Activate alert in system
    // - Find affected flights using streams
    // - Apply delays/cancellations
    // - Notify passengers
    
    public List<Flight> findAffectedFlights(WeatherAlert alert)
    // STREAM USAGE: Find flights affected by weather
    // flightScheduler.getActiveFlights().stream()
    //     .filter(flight -> isFlightAffected(flight, alert))
    //     .collect(Collectors.toList());
    
    public void applyWeatherDelays(WeatherAlert alert)
    // STREAM USAGE: Apply delays based on weather severity
    // findAffectedFlights(alert).stream()
    //     .forEach(flight -> {
    //         int delay = calculateWeatherDelay(alert.getSeverity());
    //         flight.addDelay(delay, "WEATHER");
    //     });
    
    public void closeRunwaysForWeather(WeatherAlert alert)
    // Integration with Part 1: Close runways due to weather
    // Call runwayManager.closeRunway() for affected runways
    
    public Map<String, Integer> getWeatherImpactStatistics()
    // STREAM USAGE: Calculate weather impact statistics
    // weatherService.getActiveAlerts().stream()
    //     .collect(Collectors.toMap(
    //         WeatherAlert::getAffectedAirport,
    //         alert -> findAffectedFlights(alert).size()));
    
    // THREAD MANAGEMENT
    public void startWeatherProcessing()                // Start weather processing threads
    public void processWeatherUpdates()                 // Continuous weather monitoring
}
```

---

## 6. DATABASE ACCESS OBJECTS (DAO)

### 6.1 FlightDAO.java
```java
package com.atc.part2.dao;

public class FlightDAO {
    private final MongoCollection<Document> flightCollection;
    
    // Constructor
    public FlightDAO(DatabaseManager dbManager)
    
    // CRUD OPERATIONS
    public void saveFlight(Flight flight)               // Insert flight document
    public Flight findFlightById(String flightId)      // Find by flightId
    public List<Flight> findAllFlights()               // Get all flights
    public void updateFlightStatus(String flightId, String status) // Update status
    public void updateFlightDelay(String flightId, int delayMinutes, String reason)
    public void deleteFlight(String flightId)          // Remove flight
    
    // QUERIES WITH STREAMS INTEGRATION
    public List<Flight> findFlightsByStatus(String status) // Filter by status
    public List<Flight> findDelayedFlights()            // Find flights with delays > 0
    public List<Flight> findFlightsByOrigin(String origin) // Filter by origin airport
    public List<Flight> findFlightsByDestination(String destination) // Filter by destination
    public List<Flight> findFlightsScheduledToday()    // Today's flights
    public List<Flight> findFlightsByDateRange(LocalDateTime start, LocalDateTime end)
    
    // STATISTICS QUERIES
    public long countFlightsByStatus(String status)    // Count flights in status
    public double getAverageDelayMinutes()              // Average delay across all flights
    public Map<String, Long> getFlightCountByAirport() // Flight counts per airport
    public List<Flight> getTopDelayedFlights(int limit) // Most delayed flights
    
    // WEATHER-RELATED QUERIES
    public List<Flight> findFlightsAffectedByWeather() // Flights with weather delays
    public void updateFlightWeatherStatus(String flightId, boolean affected)
    public List<Flight> findFlightsByWeatherAlert(String alertId) // Flights affected by specific alert
}
```

### 6.2 WeatherAlertDAO.java
```java
package com.atc.part2.dao;

public class WeatherAlertDAO {
    private final MongoCollection<Document> alertCollection;
    
    // Constructor
    public WeatherAlertDAO(DatabaseManager dbManager)
    
    // CRUD OPERATIONS
    public void saveWeatherAlert(WeatherAlert alert)    // Insert alert document
    public WeatherAlert findAlertById(String alertId)   // Find by alertId
    public List<WeatherAlert> findAllAlerts()           // Get all alerts
    public void updateAlertStatus(String alertId, boolean isActive) // Update active status
    public void deleteAlert(String alertId)             // Remove alert
    
    // QUERIES
    public List<WeatherAlert> findActiveAlerts()        // Find active alerts
    public List<WeatherAlert> findAlertsByAirport(String airport) // Filter by airport
    public List<WeatherAlert> findAlertsBySeverity(String severity) // Filter by severity
    public List<WeatherAlert> findAlertsByType(String type) // Filter by alert type
    public List<WeatherAlert> findAlertsInTimeRange(LocalDateTime start, LocalDateTime end)
    
    // STATISTICS
    public long countActiveAlerts()                     // Count active alerts
    public Map<String, Long> getAlertCountBySeverity()  // Alert counts by severity
    public Map<String, Long> getAlertCountByType()      // Alert counts by type
    public List<WeatherAlert> getMostRecentAlerts(int limit) // Recent alerts
}
```

---

## 7. GUI IMPLEMENTATION

### 7.1 AirTrafficGUI.java (JavaFX Application)
```java
package com.atc.gui;

public class AirTrafficGUI extends Application {
    // Fields
    private FlightScheduler flightScheduler;
    private WeatherController weatherController;
    private LandingController landingController;  // From Part 1
    
    // GUI Components
    private TableView<Flight> flightTable;
    private TableView<Aircraft> aircraftTable;
    private ListView<WeatherAlert> weatherAlertList;
    private ListView<String> logList;
    
    // Buttons for scenarios
    private Button scheduleFlight;
    private Button createWeatherAlert;
    private Button simulateFuelLow;
    private Button requestLanding;
    private Button declareEmergency;
    
    @Override
    public void start(Stage primaryStage)
    // - Initialize all controllers
    // - Create GUI layout
    // - Set up event handlers
    // - Start background threads
    
    // EVENT HANDLERS
    private void handleScheduleFlight()
    // - Create new flight with random data
    // - Call flightScheduler.scheduleFlight()
    // - Update flight table
    
    private void handleWeatherAlert()
    // - Create weather alert dialog
    // - Generate weather alert
    // - Process affected flights
    // - Update weather alert list
    
    private void handleFuelAlert()
    // - Select random aircraft
    // - Reduce fuel level to critical
    // - Generate fuel alert
    // - Update aircraft table
    
    private void handleLandingRequest()
    // - Create new aircraft
    // - Call landingController.requestLanding() (Part 1)
    // - Update aircraft table
    
    private void handleEmergency()
    // - Select aircraft
    // - Call landingController.declareEmergency() (Part 1)
    // - Update emergency status
    
    // TABLE UPDATE METHODS
    private void updateFlightTable()                    // Refresh flight data using streams
    private void updateAircraftTable()                  // Refresh aircraft data
    private void updateWeatherAlerts()                  // Refresh weather alerts
    private void updateLogMessages()                    // Refresh log messages
    
    // BACKGROUND TASKS
    private void startPeriodicUpdates()
    // - Schedule GUI updates every 2 seconds
    // - Update tables with latest data
    // - Refresh statistics
}
```

### 7.2 MainController.java (GUI Controller)
```java
package com.atc.gui.controllers;

public class MainController {
    // FXML Injected Components
    @FXML private TableView<Flight> flightTable;
    @FXML private TableView<Aircraft> aircraftTable;
    @FXML private ListView<WeatherAlert> alertList;
    @FXML private TextArea logArea;
    @FXML private Label statisticsLabel;
    
    // Controllers
    private FlightScheduler flightScheduler;
    private WeatherController weatherController;
    
    // FXML EVENT HANDLERS
    @FXML
    private void onScheduleFlightClick()
    // - Open flight scheduling dialog
    // - Create new flight
    // - Add to scheduler
    
    @FXML
    private void onCreateWeatherAlertClick()
    // - Open weather alert dialog
    // - Create weather alert
    // - Process affected flights
    
    @FXML
    private void onSimulateFuelLowClick()
    // - Select random in-flight aircraft
    // - Set fuel to low level
    // - Generate fuel alert
    
    // TABLE INITIALIZATION WITH STREAMS
    private void initializeFlightTable()
    // Set up flight table columns and cell factories
    // Use lambdas for cell value factories:
    // flightIdColumn.setCellValueFactory(cellData -> 
    //     new SimpleStringProperty(cellData.getValue().getFlightId()));
    
    private void initializeAircraftTable()
    // Set up aircraft table with fuel level progress bars
    
    // DATA BINDING WITH STREAMS
    private void bindTableData()
    // Bind table data to observable lists updated by streams:
    // ObservableList<Flight> flightData = FXCollections.observableArrayList();
    // flightScheduler.getActiveFlights().values().stream()
    //     .forEach(flightData::add);
    
    // PERIODIC UPDATES
    private void startPeriodicUpdates()
    // Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> updateTables()));
    // timeline.setCycleCount(Timeline.INDEFINITE);
    // timeline.play();
}
```

---

## 8. MONGODB COLLECTIONS STRUCTURE

### 8.1 flights Collection
```json
{
  "_id": ObjectId("..."),
  "flightId": "FL001",
  "flightNumber": "BA101",
  "aircraftId": "AC001",
  "origin": "JFK",
  "destination": "LHR",
  "scheduledDeparture": ISODate("2024-01-15T14:30:00Z"),
  "scheduledArrival": ISODate("2024-01-15T22:45:00Z"),
  "actualDeparture": null,
  "actualArrival": null,
  "status": "SCHEDULED",
  "delayMinutes": 0,
  "delayReason": null,
  "weatherAlerts": [],
  "isAffectedByWeather": false,
  "createdAt": ISODate("2024-01-15T10:00:00Z"),
  "updatedAt": ISODate("2024-01-15T10:00:00Z")
}
```

### 8.2 weather_alerts Collection
```json
{
  "_id": ObjectId("..."),
  "alertId": "WA001",
  "alertType": "STORM",
  "severity": "HIGH",
  "affectedAirport": "JFK",
  "affectedRunways": ["RW01", "RW02"],
  "startTime": ISODate("2024-01-15T12:00:00Z"),
  "endTime": ISODate("2024-01-15T16:00:00Z"),
  "description": "Severe thunderstorm affecting all runways",
  "isActive": true,
  "affectedFlights": ["FL001", "FL002", "FL003"],
  "createdAt": ISODate("2024-01-15T11:55:00Z")
}
```

### 8.3 fuel_alerts Collection
```json
{
  "_id": ObjectId("..."),
  "alertId": "FA001",
  "aircraftId": "AC001",
  "flightId": "FL001",
  "currentFuelLevel": 15,
  "criticalThreshold": 10,
  "lowThreshold": 20,
  "alertTime": ISODate("2024-01-15T13:30:00Z"),
  "alertLevel": "LOW",
  "isResolved": false,
  "resolution": null
}
```

### 8.4 flight_events Collection (Shared with Part 1)
```json
{
  "_id": ObjectId("..."),
  "eventType": "FLIGHT_SCHEDULED",
  "flightId": "FL001",
  "aircraftId": "AC001",
  "timestamp": ISODate("2024-01-15T10:00:00Z"),
  "details": {
    "origin": "JFK",
    "destination": "LHR",
    "scheduledDeparture": "2024-01-15T14:30:00Z"
  },
  "workerId": "FlightWorker-2"
}
```

---

## 9. STREAMS & LAMBDAS USAGE EXAMPLES

### 9.1 Flight Filtering and Processing
```java
// Find all delayed flights sorted by delay time
List<Flight> delayedFlights = flights.stream()
    .filter(Flight::isDelayed)
    .sorted(Comparator.comparing(Flight::getDelayMinutes).reversed())
    .collect(Collectors.toList());

// Group flights by status
Map<String, List<Flight>> flightsByStatus = flights.stream()
    .collect(Collectors.groupingBy(Flight::getStatus));

// Calculate average delay for weather-related delays
OptionalDouble avgWeatherDelay = flights.stream()
    .filter(flight -> "WEATHER".equals(flight.getDelayReason()))
    .mapToInt(Flight::getDelayMinutes)
    .average();

// Find flights departing today
List<Flight> todaysFlights = flights.stream()
    .filter(flight -> flight.getScheduledDeparture().toLocalDate().equals(LocalDate.now()))
    .collect(Collectors.toList());

// Count flights by origin airport
Map<String, Long> flightsByOrigin = flights.stream()
    .collect(Collectors.groupingBy(Flight::getOrigin, Collectors.counting()));
```

### 9.2 Weather Alert Processing
```java
// Find active critical weather alerts
List<WeatherAlert> criticalAlerts = weatherAlerts.stream()
    .filter(WeatherAlert::isActive)
    .filter(WeatherAlert::isCritical)
    .collect(Collectors.toList());

// Get all airports with active weather alerts
Set<String> affectedAirports = weatherAlerts.stream()
    .filter(WeatherAlert::isActive)
    .map(WeatherAlert::getAffectedAirport)
    .collect(Collectors.toSet());

// Apply weather delays to affected flights
weatherAlerts.stream()
    .filter(WeatherAlert::isActive)
    .forEach(alert -> {
        getAffectedFlights(alert).forEach(flight -> {
            int delay = calculateDelayBySeverity(alert.getSeverity());
            flight.addDelay(delay, "WEATHER");
        });
    });
```

### 9.3 Fuel Monitoring with Streams
```java
// Find aircraft with critical fuel levels
List<Aircraft> criticalFuelAircraft = aircraft.stream()
    .filter(a -> a.getFuelLevel() <= 10)
    .filter(a -> !"LANDED".equals(a.getStatus()))
    .sorted(Comparator.comparing(Aircraft::getFuelLevel))
    .collect(Collectors.toList());

// Calculate fuel statistics by status
Map<String, DoubleSummaryStatistics> fuelStatsByStatus = aircraft.stream()
    .collect(Collectors.groupingBy(Aircraft::getStatus,
        Collectors.summarizingDouble(Aircraft::getFuelLevel)));

// Update fuel levels for in-flight aircraft
aircraft.stream()
    .filter(a -> "AIRBORNE".equals(a.getStatus()))
    .forEach(a -> {
        int newLevel = Math.max(0, a.getFuelLevel() - 2);
        a.setFuelLevel(newLevel);
        if (newLevel <= 20) {
            generateFuelAlert(a);
        }
    });
```

---

## 10. THREAD EXECUTION FLOW

### 10.1 Flight Scheduling Process
```
1. User schedules flight → FlightScheduler.scheduleFlight()
2. Flight added to flightQueue → BlockingQueue.offer()
3. FlightWorker thread takes flight → flightQueue.take()
4. Worker processes flight → Updates status, checks weather
5. Worker updates database → FlightDAO.updateFlight()
6. GUI updated → Streams filter and display updated data
```

### 10.2 Weather Alert Process
```
1. WeatherMonitor generates alert → WeatherService.createWeatherAlert()
2. Find affected flights → Streams filter flights by airport
3. Apply delays to flights → Streams forEach with lambda
4. Notify passengers → NotificationService async with CompletableFuture
5. Update GUI → Streams refresh weather alert list
```

### 10.3 Fuel Monitoring Process
```
1. FuelMonitor checks levels → Every 10 seconds
2. Find low fuel aircraft → Streams filter by fuel level
3. Generate fuel alerts → FuelMonitoringService.createFuelAlert()
4. Escalate to emergency → Call Part 1 EmergencyController
5. Update GUI → Streams refresh aircraft table with fuel bars
```

---

## 11. INTEGRATION POINTS WITH PART 1

### 11.1 Methods You'll Call from Part 1
```java
// From LandingController
landingController.requestLanding(aircraft);          // When flight ready to land
landingController.declareEmergency(aircraft);        // When fuel critical
landingController.getWaitingAircraft();              // For GUI display

// From RunwayManager
runwayManager.closeRunway(runwayId, "WEATHER");      // Weather-related closure
runwayManager.getAvailableRunwayCount();             // For scheduling decisions
```

### 11.2 Events You'll Listen For from Part 1
- `LANDING_COMPLETED` - Update flight status to "LANDED"
- `EMERGENCY_DECLARED` - Update aircraft emergency status
- `RUNWAY_CLOSED` - Adjust flight schedules
- `FUEL_CRITICAL` - Escalate fuel alert to emergency

### 11.3 Shared Data Structures
```java
// Shared between both parts
ConcurrentHashMap<String, Aircraft> allAircraft;     // All aircraft in system
BlockingQueue<Event> systemEvents;                   // System-wide events
AtomicInteger systemLoad;                             // Overall system load
```

---

## 12. TESTING SCENARIOS FOR PART 2

### 12.1 Scenario 4: Weather Delays
```java
// Test Method: testWeatherDelayHandling()
// Create weather alert for airport
// Verify affected flights identified using streams
// Verify delays applied correctly
// Verify passenger notifications sent
```

### 12.2 Scenario 5: Fuel Management
```java
// Test Method: testFuelMonitoringAndAlerts()
// Create aircraft with varying fuel levels
// Verify low fuel alerts generated
// Verify critical fuel escalates to emergency
// Verify fuel consumption simulation
```

### 12.3 Scenario 6: Flight Scheduling
```java
// Test Method: testFlightSchedulingAndRescheduling()
// Schedule multiple flights
// Apply various delays
// Verify rescheduling logic
// Verify statistics calculations using streams
```

---

## 13. PERFORMANCE REQUIREMENTS

### 13.1 Thread Performance
- **Flight Workers**: Process 1 flight every 2-3 seconds
- **Weather Monitor**: Check weather every 30 seconds
- **Fuel Monitor**: Check fuel every 10 seconds
- **GUI Updates**: Refresh every 2 seconds using streams

### 13.2 Stream Performance
- **Flight Filtering**: Handle 1000+ flights efficiently
- **Weather Processing**: Process 50+ alerts simultaneously
- **Statistics Calculation**: Real-time calculations using parallel streams
- **GUI Data Binding**: Smooth updates without blocking UI

### 13.3 Database Performance
- **Flight Queries**: < 100ms for complex stream operations
- **Weather Alert Queries**: < 50ms per query
- **Bulk Updates**: Handle 100+ flight updates per minute
- **Statistics Queries**: < 200ms for aggregation operations

---

## 14. DELIVERABLES CHECKLIST

### 14.1 Code Files (12 files)
- [ ] `Flight.java` - Complete with all methods
- [ ] `WeatherAlert.java` - Complete with business logic
- [ ] `FuelAlert.java` - Complete with alert levels
- [ ] `FlightWorker.java` - Runnable with CompletableFuture
- [ ] `WeatherMonitor.java` - Scheduled monitoring thread
- [ ] `FuelMonitor.java` - Fuel monitoring thread
- [ ] `WeatherService.java` - Streams/lambdas implementation
- [ ] `FuelMonitoringService.java` - Fuel processing with streams
- [ ] `NotificationService.java` - Async notifications
- [ ] `FlightScheduler.java` - Main controller with thread pools
- [ ] `WeatherController.java` - Weather management
- [ ] `FlightDAO.java` - MongoDB operations
- [ ] `WeatherAlertDAO.java` - Alert persistence
- [ ] `AirTrafficGUI.java` - JavaFX application
- [ ] `MainController.java` - FXML controller

### 14.2 Streams & Lambdas Implementation
- [ ] Flight filtering and grouping operations
- [ ] Weather alert processing with streams
- [ ] Fuel monitoring with parallel streams
- [ ] Statistics calculations using collectors
- [ ] Async operations with CompletableFuture
- [ ] GUI data binding with observable streams

### 14.3 Thread Implementation
- [ ] 5 FlightWorker threads in ExecutorService
- [ ] 1 WeatherMonitor thread with ScheduledExecutorService
- [ ] 1 FuelMonitor thread with periodic execution
- [ ] 2 Weather processing threads
- [ ] Proper thread coordination and shutdown

### 14.4 Database Integration
- [ ] MongoDB collections for flights and alerts
- [ ] All CRUD operations implemented
- [ ] Query methods with stream integration
- [ ] Statistics and reporting queries

### 14.5 GUI Implementation
- [ ] JavaFX application with all scenarios
- [ ] Real-time data updates using streams
- [ ] Event handlers for all user actions
- [ ] Integration with both Part 1 and Part 2 controllers

### 14.6 Testing
- [ ] Unit tests for all service classes
- [ ] Stream operation tests
- [ ] Thread safety verification
- [ ] Integration tests for 3 scenarios
- [ ] GUI functionality tests

---

**CRITICAL SUCCESS FACTORS:**
1. **Streams & Lambdas**: Extensive use for data processing and filtering
2. **Thread Coordination**: Proper async operations with CompletableFuture
3. **Database Integration**: Efficient queries supporting stream operations
4. **GUI Responsiveness**: Non-blocking updates using background threads
5. **Weather Processing**: Real-time alert generation and flight impact
6. **Fuel Monitoring**: Continuous monitoring with emergency escalation

**ESTIMATED EFFORT:** 45-55 hours of development time