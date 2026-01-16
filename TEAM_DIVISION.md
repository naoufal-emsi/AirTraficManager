# Air Traffic Control System - Team Division

## Project Overview
Multi-threaded Air Traffic Control simulation with MongoDB database, priority-based emergency handling, and physics-based aircraft movement.

---

## Partner 1: Aircraft & Emergency System

### 📁 Your Files (Full Ownership)

#### Models
- `src/main/java/com/atc/core/models/Aircraft.java`
  - Aircraft state, fuel, position, emergency status
  - Physics calculations (fuel burn, distance updates)
  
- `src/main/java/com/atc/core/models/AircraftType.java`
  - Enum: B737, A320, B777, A330, B787
  - Specs: speed, fuel capacity, burn rate

- `src/main/java/com/atc/core/models/FlightPlan.java`
  - Origin, destination, waypoints
  - Fuel calculations

#### Worker Threads (3 threads)
- `src/main/java/com/atc/workers/AircraftUpdateWorker.java`
  - **Thread 1**: Updates aircraft position every 2 seconds
  - Physics: `distance -= speed * time`
  - Status transitions: APPROACHING → READY_TO_LAND → LANDING → LANDED

- `src/main/java/com/atc/workers/FuelMonitoringWorker.java`
  - **Thread 2**: Monitors fuel levels
  - Crashes aircraft if fuel = 0 with FUEL emergency

- `src/main/java/com/atc/workers/EmergencyHandlerWorker.java`
  - **Thread 3**: Processes emergency events
  - Logs emergency actions to database

#### Controllers
- `src/main/java/com/atc/controllers/EmergencyController.java`
  - Declares emergencies with priority assignment
  - Priority mapping: FIRE(1), MEDICAL(5), SECURITY(8), FUEL_CRITICAL(10), WEATHER(30), FUEL_LOW(50)

#### GUI Components (in AirTrafficControlGUI.java)
- Emergency buttons panel (6 buttons)
- Aircraft table display
- `generateAircraft()` button handler
- `triggerEmergency()` method

### 📊 Database Methods You Use
**Write:**
- `generateAndInsertRealisticFlight()` - Create new aircraft
- `insertActiveAircraft()` - Insert aircraft record
- `updateActiveAircraft()` - Update aircraft state
- `saveEmergencyEvent()` - Log emergency

**Read:**
- `getAllActiveAircraft()` - Get all aircraft for processing

### 🔗 Integration Points
**You WRITE to:**
- `active_aircraft` collection (aircraft state, fuel, distance, emergency)
- `emergency_events` collection (emergency logs)

**You READ from:**
- `active_aircraft` collection (to update aircraft)

---

## Partner 2: Runway & Simulation System

### 📁 Partner's Files (Full Ownership)

#### Models
- `src/main/java/com/atc/core/models/Runway.java`
  - Runway state, availability, assigned aircraft
  - Emergency services ready flag

#### Core Simulation
- `src/main/java/com/atc/core/SimulationManager.java`
  - Manages thread pool, simulation lifecycle
  - Start/stop simulation

- `src/main/java/com/atc/core/SimulationConfig.java`
  - Physics parameters (time step, fuel burn, etc.)
  - Calculation methods

- `src/main/java/com/atc/core/SimulationClock.java`
  - Simulation time tracking
  - Tick counter

#### Worker Threads (5 threads)
- `src/main/java/com/atc/workers/RunwayManagerWorker.java`
  - **Threads 4-7**: 4 instances manage runway assignments
  - Priority queue sorting (priority → timestamp)
  - Assigns first available runway to READY_TO_LAND aircraft
  - Releases runway when aircraft LANDED

- `src/main/java/com/atc/workers/WeatherWorker.java`
  - **Thread 8**: Weather simulation (currently disabled)
  - Can generate weather events

#### GUI Components (in AirTrafficControlGUI.java)
- Runway table display
- `addRunway()` button handler
- Stats display (top panel)

### 📊 Database Methods Partner Uses
**Write:**
- `insertRunway()` - Create new runway
- `updateRunway()` - Update runway status
- `saveRunwayEvent()` - Log runway operations
- `saveWeatherEvent()` - Log weather

**Read:**
- `getAllRunways()` - Get all runways for assignment
- `getAllActiveAircraft()` - Get aircraft for runway assignment

### 🔗 Integration Points
**Partner WRITES to:**
- `runways` collection (runway status, assigned aircraft)
- `runway_events` collection (runway operation logs)
- `weather_events` collection (weather logs)

**Partner READS from:**
- `active_aircraft` collection (to assign runways)
- `runways` collection (to find available runways)

---

## Shared Components

### Database
- `src/main/java/com/atc/database/DatabaseManager.java`
  - **Both read and write**
  - Singleton pattern
  - MongoDB connection: `localhost:27017`
  - Database: `airTrafficControl`

### Main Entry Point
- `src/main/java/com/atc/AirTrafficSystem.java`
  - **Both may modify**
  - Initializes database, runways, simulation
  - Starts all worker threads
  - Launches GUI

### GUI
- `src/main/java/com/atc/gui/AirTrafficControlGUI.java`
  - **Both add features**
  - Partner 1: Emergency buttons, aircraft table
  - Partner 2: Runway table, stats, add runway button

---

## Data Flow

### Aircraft Lifecycle (Partner 1 → Partner 2)
```
1. Partner 1: Generate aircraft → DB (active_aircraft)
2. Partner 1: AircraftUpdateWorker updates position → DB
3. Partner 1: When distance ≤ 5000m → status = READY_TO_LAND → DB
4. Partner 2: RunwayManagerWorker reads READY_TO_LAND aircraft → DB
5. Partner 2: Assigns first available runway → DB (updates both aircraft & runway)
6. Partner 1: AircraftUpdateWorker continues moving aircraft
7. Partner 1: When distance ≤ 50m → status = LANDED → DB
8. Partner 2: RunwayManagerWorker releases runway → DB
```

### Emergency Flow (Partner 1 only)
```
1. User clicks emergency button (GUI)
2. Partner 1: triggerEmergency() → updates aircraft priority & emergency type → DB
3. Partner 1: EmergencyHandlerWorker logs emergency → DB
4. Partner 2: RunwayManagerWorker reads priority → sorts queue → assigns runway first
```

---

## MongoDB Collections

### active_aircraft (Both read/write)
```json
{
  "callsign": "DAL123",
  "aircraftType": "B737",
  "fuel": 4500.0,
  "speed": 150.0,
  "distance": 18000.0,
  "origin": "JFK",
  "destination": "LAX",
  "status": "APPROACHING",
  "emergency": "NONE",
  "priority": 100,
  "emergencyTimestamp": 1234567890,
  "assignedRunway": "RWY-09L",
  "timestamp": "2024-01-16T10:00:00Z"
}
```

### runways (Partner 2 writes, Partner 1 reads)
```json
{
  "runwayId": "RWY-09L",
  "status": "AVAILABLE",
  "currentAircraft": null,
  "thresholdPosition": 500.0
}
```

### emergency_events (Partner 1 writes)
```json
{
  "callsign": "DAL123",
  "details": "MAYDAY - DAL123 FIRE emergency",
  "timestamp": "2024-01-16T10:00:00Z"
}
```

### runway_events (Partner 2 writes)
```json
{
  "event": "Aircraft DAL123 [FIRE - PRIORITY] assigned to RWY-09L",
  "timestamp": "2024-01-16T10:00:00Z"
}
```

---

## Thread Summary

| Thread | Owner | Purpose | Frequency |
|--------|-------|---------|-----------|
| AircraftUpdateWorker | Partner 1 | Update aircraft position/fuel | Every 2s |
| FuelMonitoringWorker | Partner 1 | Check fuel levels | Every 2s |
| EmergencyHandlerWorker | Partner 1 | Process emergencies | Every 2s |
| RunwayManagerWorker (×4) | Partner 2 | Assign runways | Every 2s |
| WeatherWorker | Partner 2 | Weather simulation | Every 30s |

**Total: 8 active threads**

---

## Communication Protocol

### No Direct Communication Needed!
- All communication happens through **MongoDB**
- Partner 1 writes aircraft state → Partner 2 reads it
- Partner 2 writes runway state → Partner 1 reads it
- **Database is the single source of truth**

### Conflict Resolution
- MongoDB handles concurrent writes automatically
- Each partner owns different collections (mostly)
- `active_aircraft` is shared but updates different fields:
  - Partner 1: fuel, distance, status, emergency, priority
  - Partner 2: assignedRunway

---

## Testing Strategy

### Partner 1 Tests
1. Generate aircraft → verify in DB
2. Trigger emergency → verify priority updated
3. Watch fuel decrease → verify crash at 0
4. Check aircraft moves toward runway (distance decreases)

### Partner 2 Tests
1. Add runway → verify in DB
2. Aircraft reaches READY_TO_LAND → verify runway assigned
3. Multiple emergencies → verify priority order
4. Aircraft lands → verify runway released

### Integration Test (Both)
1. Partner 1 generates aircraft with FIRE emergency
2. Partner 2 verifies it gets runway first (priority 1)
3. Partner 1 verifies aircraft lands
4. Partner 2 verifies runway freed

---

## Build & Run

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.atc.AirTrafficSystem"

# Or run main class directly
java -cp target/classes com.atc.AirTrafficSystem
```

**Prerequisites:**
- MongoDB running on `localhost:27017`
- Java 25
- Maven

---

## Emergency Priority Reference

| Emergency | Priority | Description |
|-----------|----------|-------------|
| FIRE | 1 | Highest - immediate landing |
| MEDICAL | 5 | Critical medical |
| SECURITY | 8 | Security threat |
| FUEL_CRITICAL | 10 | Fuel exhaustion imminent |
| WEATHER_STORM | 30 | Weather diversion |
| FUEL_LOW | 50 | Low fuel warning |
| NONE | 100 | Normal operations |

**Lower number = Higher priority = Lands first**

---

## Git Workflow

```bash
# Partner 1 commits
git add src/main/java/com/atc/core/models/Aircraft*.java
git add src/main/java/com/atc/workers/Aircraft*.java
git add src/main/java/com/atc/workers/Fuel*.java
git add src/main/java/com/atc/workers/Emergency*.java
git add src/main/java/com/atc/controllers/
git commit -m "Partner 1: Aircraft system implementation"

# Partner 2 commits
git add src/main/java/com/atc/core/models/Runway.java
git add src/main/java/com/atc/core/Simulation*.java
git add src/main/java/com/atc/workers/Runway*.java
git add src/main/java/com/atc/workers/Weather*.java
git commit -m "Partner 2: Runway system implementation"

# Merge
git pull --rebase
git push
```

---

**Last Updated:** 2024-01-16
**Project:** Air Traffic Control System
**Architecture:** Multi-threaded, Database-driven, Event-based
