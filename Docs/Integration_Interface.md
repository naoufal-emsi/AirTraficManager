# PART 1 ↔ PART 2 INTEGRATION INTERFACE
## Functions Each Part Provides to the Other

---

## PART 1 → PART 2 

### LandingController
```java
void requestLanding(Aircraft aircraft)                    // → void
void declareEmergency(Aircraft aircraft)                  // → void  
List<Aircraft> getWaitingAircraft()                      // → List<Aircraft>
int getQueueSize()                                       // → int
```

### RunwayManager  
```java
int getAvailableRunwayCount()                            // → int
void closeRunway(String runwayId, String reason)         // → void
void openRunway(String runwayId)                         // → void
List<Runway> getAvailableRunways()                       // → List<Runway>
```

### Aircraft (Model)
```java
String getAircraftId()                                   // → String
String getCallsign()                                     // → String
int getFuelLevel()                                       // → int (0-100)
String getStatus()                                       // → String
boolean isEmergency()                                    // → boolean
void setFuelLevel(int level)                            // → void
void declareEmergency()                                 // → void
```

---

## PART 2 → PART 1 

### FlightScheduler
```java
ConcurrentHashMap<String, Flight> getActiveFlights()    // → Map<String, Flight>
List<Flight> getDelayedFlights()                        // → List<Flight>
void scheduleFlight(Flight flight)                      // → void
```

### WeatherController
```java
void closeRunwaysForWeather(WeatherAlert alert)         // → void
List<Flight> findAffectedFlights(WeatherAlert alert)   // → List<Flight>
```

### FuelMonitoringService
```java
List<Aircraft> getLowFuelAircraft()                     // → List<Aircraft>
List<Aircraft> getCriticalFuelAircraft()               // → List<Aircraft>
void escalateToEmergency(Aircraft aircraft)            // → void
```

### Flight (Model)
```java
String getFlightId()                                    // → String
String getAircraftId()                                  // → String
String getStatus()                                      // → String
boolean isDelayed()                                     // → boolean
int getDelayMinutes()                                   // → int
```

---

## SHARED MODELS (Both Use)

### DatabaseManager
```java
static void connect()                                   // → void
static void disconnect()                                // → void
Object getCollection(String name)                       // → MongoCollection
```

### Event
```java
static Event createPart1Event(String type, String msg) // → Event
static Event createPart2Event(String type, String msg) // → Event
```

---

## KEY INTEGRATION POINTS

### 1. Emergency Escalation (Part 2 → Part 1)
```java
// When fuel critical, Part 2 calls Part 1
Aircraft aircraft = getCriticalFuelAircraft().get(0);
landingController.declareEmergency(aircraft);
```

### 2. Weather Runway Closure (Part 2 → Part 1)  
```java
// When bad weather, Part 2 closes runways
runwayManager.closeRunway("RW01", "WEATHER");
```

### 3. Landing Status Updates (Part 1 → Part 2)
```java
// When aircraft lands, Part 1 updates Part 2
Flight flight = flightScheduler.getActiveFlights().get(flightId);
flight.setStatus("LANDED");
```

### 4. Fuel Monitoring (Part 2 → Part 1)
```java
// Part 2 monitors, Part 1 gets aircraft
List<Aircraft> waitingAircraft = landingController.getWaitingAircraft();
fuelMonitoringService.checkFuelLevels(waitingAircraft);
```

---

## CRITICAL SHARED DATA

### Aircraft Object (Both Modify)
- **Part 1**: Updates status, runway assignment, emergency flag
- **Part 2**: Updates fuel level, monitors for alerts

### Flight Object (Part 2 Owns, Part 1 Reads)
- **Part 2**: Creates, schedules, delays flights  
- **Part 1**: Reads aircraft ID to link with landings

### Events (Both Create)
- **Part 1**: Landing events, runway events, emergency events
- **Part 2**: Flight events, weather events, fuel events

---

## THREAD COMMUNICATION

### Part 1 Threads → Part 2
```java
// Emergency thread notifies fuel service
fuelMonitoringService.resolveFuelAlert(alertId, "EMERGENCY_LANDING");
```

### Part 2 Threads → Part 1  
```java
// Weather thread closes runways
runwayManager.closeRunway(runwayId, "STORM");
```

---

## GUI INTEGRATION (Part 2 Calls Part 1)

### Button Handlers
```java
// "Request Landing" button
landingController.requestLanding(selectedAircraft);

// "Declare Emergency" button  
landingController.declareEmergency(selectedAircraft);

// "Close Runway" button
runwayManager.closeRunway(selectedRunway, reason);
```

### Data Display
```java
// Show waiting aircraft
List<Aircraft> waiting = landingController.getWaitingAircraft();

// Show runway status
int available = runwayManager.getAvailableRunwayCount();
```