# Air Traffic Control System - Complete Specification

## Project Information
- **Name:** Air Traffic Control System (AirTrafficManager)
- **Type:** Multi-threaded simulation with real-time database
- **Language:** Java 25
- **Database:** MongoDB (localhost:27017)
- **Architecture:** Event-driven, database-centric, multi-threaded

---

## System Overview

### Purpose
Simulate an air traffic control system managing multiple aircraft approaching an airport, handling emergencies with priority-based runway assignment, and tracking all operations in real-time.

### Key Features
- Real-time aircraft position tracking
- Physics-based movement simulation
- Priority-based emergency handling (6 types)
- Multi-threaded concurrent processing (8 threads)
- MongoDB persistence (no in-memory state)
- Swing GUI with live updates
- Manual emergency scenario triggers

---

## Technical Architecture

### Database Collections

#### active_aircraft
```json
{
  "callsign": "DAL123",           // Unique identifier
  "aircraftType": "B737",         // Aircraft model
  "fuel": 4500.0,                 // Fuel in kg
  "speed": 150.0,                 // Speed in m/s
  "distance": 18000.0,            // Distance to runway threshold in meters
  "origin": "JFK",                // Departure airport
  "destination": "LAX",           // Arrival airport
  "status": "APPROACHING",        // APPROACHING | READY_TO_LAND | LANDING | LANDED | CRASHED
  "emergency": "NONE",            // NONE | FIRE | MEDICAL | SECURITY | FUEL_CRITICAL | FUEL_LOW | WEATHER_STORM
  "priority": 100,                // 1-100 (lower = higher priority)
  "emergencyTimestamp": 1234567,  // When emergency was declared (milliseconds)
  "assignedRunway": "RWY-09L",    // Assigned runway ID (nullable)
  "timestamp": "2024-01-16T10:00:00Z"
}
```

#### runways
```json
{
  "runwayId": "RWY-09L",          // Unique runway identifier
  "status": "AVAILABLE",          // AVAILABLE | OCCUPIED
  "currentAircraft": "DAL123",    // Callsign of aircraft using runway (nullable)
  "thresholdPosition": 500.0      // Runway position in meters (for display)
}
```

#### emergency_events (Audit log)
```json
{
  "callsign": "DAL123",
  "details": "MAYDAY - DAL123 FIRE emergency, immediate landing required",
  "timestamp": "2024-01-16T10:00:00Z"
}
```

#### runway_events (Audit log)
```json
{
  "event": "Aircraft DAL123 [FIRE - PRIORITY] assigned to RWY-09L at distance 4500m",
  "timestamp": "2024-01-16T10:00:00Z"
}
```

#### aircraft_types (Reference data)
```json
{
  "type": "B737",
  "cruiseSpeed": 150.0,
  "fuelCapacity": 5500.0,
  "fuelBurnRate": 0.8
}
```

#### airports (Reference data)
```json
{
  "code": "JFK",
  "name": "JFK International Airport",
  "active": true
}
```

#### weather_events (Audit log)
```json
{
  "description": "Thunderstorm affecting runways",
  "affectedRunways": ["RWY-09L", "RWY-09R"],
  "timestamp": "2024-01-16T10:00:00Z"
}
```

---

## Physics Model

### Aircraft Movement
```
Every 2 seconds:
  distance = distance - (speed * 2.0)
  fuel = fuel - (burnRate * 2.0)
```

### Status Transitions
```
distance > 5000m  → APPROACHING
distance ≤ 5000m  → READY_TO_LAND (enters landing queue)
Assigned runway   → LANDING (actively landing)
distance ≤ 50m    → LANDED (touchdown complete)
fuel = 0 + emergency → CRASHED
```

### Spawn Parameters
- Distance: 15,000m - 30,000m (15-30 km from airport)
- Fuel: 50%-90% of aircraft capacity
- Speed: Based on aircraft type (150-250 m/s)

---

## Emergency System

### Priority Levels
| Emergency | Priority | Action |
|-----------|----------|--------|
| FIRE | 1 | Immediate landing, highest priority |
| MEDICAL | 5 | Priority landing, ambulance ready |
| SECURITY | 8 | Priority landing, law enforcement alerted |
| FUEL_CRITICAL | 10 | Immediate landing, fuel < 800kg |
| WEATHER_STORM | 30 | Diversion or holding pattern |
| FUEL_LOW | 50 | Priority landing, fuel < 2000kg |
| NONE | 100 | Normal operations |

### Emergency Declaration
1. User clicks emergency button in GUI
2. System finds first aircraft without emergency
3. Updates aircraft: emergency type, priority, timestamp
4. For FUEL emergencies: reduces fuel to critical level, sets READY_TO_LAND
5. Logs emergency event to database

### Priority Queue Logic
```
Sort aircraft by:
  1. Priority (ascending: 1, 5, 8, 10, 30, 50, 100)
  2. Emergency timestamp (ascending: who declared first)

Example:
  FIRE (priority 1, time 10:00:01)
  MEDICAL (priority 5, time 10:00:05)
  FUEL_CRITICAL (priority 10, time 10:00:02)
  FUEL_CRITICAL (priority 10, time 10:00:08) ← declared later, lands later
```

---

## Runway Assignment

### Algorithm
```
For each aircraft in priority order:
  if aircraft.status == READY_TO_LAND:
    runway = first AVAILABLE runway
    if runway found:
      runway.status = OCCUPIED
      runway.currentAircraft = aircraft.callsign
      aircraft.status = LANDING
      aircraft.assignedRunway = runway.runwayId
      Log assignment event
```

### Runway Release
```
When aircraft.status == LANDED:
  runway = get runway by aircraft.assignedRunway
  runway.status = AVAILABLE
  runway.currentAircraft = null
  aircraft.assignedRunway = null
  aircraft.emergency = NONE
  aircraft.priority = 100
  Log landing event
```

---

## Thread Architecture

### Thread Pool
- **Type:** ThreadPoolExecutor
- **Size:** CPU cores to 2× CPU cores
- **Purpose:** Dynamic task execution

### Worker Threads (8 total)

1. **AircraftUpdateWorker** (1 thread)
   - Updates all aircraft positions and fuel
   - Runs every 2 seconds
   - Transitions status based on distance

2. **FuelMonitoringWorker** (1 thread)
   - Monitors fuel levels
   - Crashes aircraft if fuel = 0 with fuel emergency
   - Runs every 2 seconds

3. **EmergencyHandlerWorker** (1 thread)
   - Processes emergency events
   - Logs emergency actions
   - Runs every 2 seconds

4. **RunwayManagerWorker** (4 threads)
   - Assigns runways to aircraft
   - Releases runways after landing
   - Runs every 2 seconds
   - Thread-safe concurrent processing

5. **WeatherWorker** (1 thread)
   - Weather simulation (currently disabled)
   - Runs every 30 seconds

### Thread Safety
- All threads read/write through DatabaseManager
- MongoDB handles concurrent access
- No shared in-memory state
- No race conditions

---

## GUI Components

### Top Panel
- Title: "AIR TRAFFIC CONTROL SYSTEM"
- Stats: Aircraft count, emergencies, landed, occupied runways
- Buttons: Generate Aircraft, Add Runway, Clear Log

### Center Panel (3 columns)

**Aircraft Table:**
| Callsign | Type | Fuel | Distance | Status | Emergency |
|----------|------|------|----------|--------|-----------|
| DAL123 | B737 | 4500 | 18000 | APPROACHING | NONE |

**Runway Table:**
| Runway ID | Position (m) | Status | Aircraft |
|-----------|--------------|--------|----------|
| RWY-09L | 500 | AVAILABLE | None |

**System Log:**
```
[10:00:01] Generated aircraft: DAL123
[10:00:15] MAYDAY - DAL123 FIRE emergency (Priority: 1)
[10:00:20] Aircraft DAL123 [FIRE - PRIORITY] assigned to RWY-09L
[10:00:45] Aircraft DAL123 landed safely on RWY-09L
```

### Bottom Panel
**Emergency Scenario Buttons (6):**
- FIRE Emergency (red)
- MEDICAL Emergency (blue)
- SECURITY Threat (pink)
- FUEL CRITICAL (dark red)
- WEATHER STORM (gray-blue)
- FUEL LOW (orange)

---

## Aircraft Types

| Type | Speed (m/s) | Fuel (kg) | Burn Rate (kg/s) |
|------|-------------|-----------|------------------|
| B737 | 150 | 5500 | 0.8 |
| A320 | 160 | 6400 | 0.82 |
| B777 | 250 | 11000 | 0.84 |
| A330 | 230 | 11750 | 0.82 |
| B787 | 210 | 15200 | 0.85 |

---

## Simulation Parameters

```java
SimulationConfig(
  metersPerTimeUnit: 1000.0,
  timeStepSeconds: 2.0,
  fuelBurnRatePerHour: 1000.0,
  fuelReserveMinutes: 30.0,
  landingDurationSeconds: 120.0
)
```

---

## Error Handling

### Database Connection Failure
- System continues with `database = null`
- All operations check `if (database == null) return`
- Errors logged to `System.err`

### MongoDB Unavailable
- Operations fail gracefully
- Error messages logged
- System remains operational (degraded mode)

### Thread Interruption
- All threads check `InterruptedException`
- Graceful shutdown on interrupt
- Resources cleaned up properly

---

## Startup Sequence

1. Initialize database connection
2. Clear old runtime data
3. Initialize reference data (airports, aircraft types)
4. Create 4 runways (RWY-09L, 09R, 27L, 27R)
5. Start simulation manager
6. Generate 3 initial aircraft
7. Start 8 worker threads
8. Launch GUI
9. Register shutdown hook

---

## Shutdown Sequence

1. Stop all worker threads
2. Stop simulation manager
3. Close MongoDB connection
4. Exit

---

## Performance Characteristics

- **Update Frequency:** 2 seconds
- **GUI Refresh:** 2 seconds
- **Concurrent Runways:** 4 (can handle 4 simultaneous landings)
- **Thread Count:** 8 worker threads + GUI thread + thread pool
- **Database Queries:** ~10-20 per second (read-heavy)
- **Memory:** Minimal (all state in database)

---

## Known Limitations

1. **1D Movement Model:** All aircraft move toward single point (runway threshold at 0m)
2. **Simplified Physics:** No wind, weather effects on speed, or 3D positioning
3. **No Collision Detection:** Aircraft don't interact with each other
4. **Fixed Runway Count:** 4 initial runways (can add more via GUI)
5. **No Takeoffs:** System only handles landings

---

## Future Enhancements (Not Implemented)

- 2D/3D positioning with actual runway coordinates
- Holding patterns for delayed aircraft
- Takeoff scheduling
- Weather impact on aircraft speed
- Collision avoidance system
- Multiple airports
- Air traffic controller commands
- Replay system from event logs

---

## Dependencies

```xml
<dependencies>
  <dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.11.1</version>
  </dependency>
  
  <dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
  </dependency>
  
  <dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.1</version>
  </dependency>
  
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
  </dependency>
</dependencies>
```

---

**Document Version:** 1.0  
**Last Updated:** 2024-01-16  
**Authors:** Development Team
