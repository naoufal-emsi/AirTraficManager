# PARTNER PART - Runway & Simulation System

## Files Partner Owns

### Models
- Runway.java - Runway state, availability, assigned aircraft, emergency services flag

### Core Simulation
- SimulationManager.java - Thread pool management, simulation lifecycle, start/stop
- SimulationConfig.java - Physics parameters (time step, fuel burn), calculation methods
- SimulationClock.java - Simulation time tracking, tick counter

### Workers (5 Threads)
- RunwayManagerWorker.java - Threads 4-7: 4 instances manage runway assignments every 2s
- WeatherWorker.java - Thread 8: Weather simulation every 30s

### GUI Components
- Runway table display
- addRunway() button handler
- Stats display (top panel)

## What Partner Builds

### Runway.java
Fields: runwayId, status, currentAircraft, thresholdPosition, emergencyServicesReady

Methods:
- assignAircraft(callsign) - Set runway OCCUPIED
- releaseRunway() - Set runway AVAILABLE
- isAvailable() - Check if runway free

Status: AVAILABLE, OCCUPIED

### SimulationManager.java
- Manages ExecutorService thread pool
- Starts all 8 worker threads
- Stops simulation gracefully
- Handles thread lifecycle

Methods:
- startSimulation()
- stopSimulation()
- submitWorker(Runnable worker)

### SimulationConfig.java
Constants:
- TIME_STEP = 2.0 seconds
- FUEL_BURN_MULTIPLIER
- DISTANCE_THRESHOLD_READY = 5000m
- DISTANCE_THRESHOLD_LANDED = 50m

### SimulationClock.java
- Tracks simulation time
- Increments tick counter
- Provides current simulation timestamp

### RunwayManagerWorker.java (4 instances)
Every 2 seconds:
- Get all READY_TO_LAND aircraft from DB
- Sort by priority (lower first), then timestamp
- Get all available runways from DB
- Assign first available runway to highest priority aircraft
- Update aircraft.assignedRunway in DB
- Update runway.status = OCCUPIED in DB
- Log to runway_events

When aircraft LANDED:
- Get runway from DB
- Set runway.status = AVAILABLE
- Set runway.currentAircraft = null
- Update runway in DB
- Log to runway_events

### WeatherWorker.java
Every 30 seconds:
- Generate weather events (currently disabled)
- Can trigger WEATHER_STORM emergencies
- Log to weather_events

## Database Methods Partner Uses

Write:
- insertRunway(runway)
- updateRunway(runway)
- saveRunwayEvent(event)
- saveWeatherEvent(event)

Read:
- getAllRunways()
- getAllActiveAircraft()
- getRunwayById(runwayId)

## Partner Data Flow

1. User clicks "Add Runway"
2. insertRunway() → DB (runways)
3. RunwayManagerWorker reads READY_TO_LAND aircraft → DB
4. Sort by priority (emergency first)
5. Find first available runway → DB
6. Assign runway to aircraft → DB (updates both aircraft & runway)
7. Wait for aircraft to reach LANDED status
8. Release runway → DB (runway.status = AVAILABLE)
9. Log to runway_events → DB

## Testing Checklist

- Add runway → verify in DB
- Aircraft READY_TO_LAND → runway assigned
- Multiple aircraft → priority order respected
- Emergency aircraft → gets runway first
- Aircraft LANDED → runway released
- All runways occupied → aircraft waits
- 4 RunwayManagerWorker threads running concurrently

## Critical Rules

- NEVER modify aircraft position/fuel
- NEVER generate aircraft
- ONLY write to: runways, runway_events, weather_events
- ONLY update aircraft.assignedRunway field
- Check every 2 seconds for assignments
- Priority sorting: Lower number first, then timestamp
- Release runway only when status = LANDED

## Integration with My Part

Partner receives:
- Aircraft with status READY_TO_LAND
- Emergency priority for sorting

Partner provides:
- Runway assignment (updates assignedRunway)
- Runway release when LANDED

Communication: 100% through MongoDB

## Priority Queue Logic

Sort aircraft by:
1. Priority (ascending) - Lower = Higher priority
2. Timestamp (ascending) - Earlier = First

Example:
- Aircraft A: FIRE (priority 1) at 10:00:00
- Aircraft B: MEDICAL (priority 5) at 09:59:00
- Aircraft C: NONE (priority 100) at 09:58:00

Order: A (priority 1), B (priority 5), C (priority 100)

## Runway Assignment Algorithm

```
1. Get all aircraft with status = READY_TO_LAND
2. Sort by priority, then timestamp
3. Get all runways with status = AVAILABLE
4. For each aircraft in sorted order:
   - If runway available:
     - Assign runway to aircraft
     - Update aircraft.assignedRunway
     - Update runway.status = OCCUPIED
     - Update runway.currentAircraft = callsign
     - Log event
5. Check all aircraft with status = LANDED
6. For each landed aircraft:
   - Get assigned runway
   - Release runway (status = AVAILABLE)
   - Clear runway.currentAircraft
   - Log event
```

## Success Criteria

- Runways created dynamically
- Aircraft assigned to runways by priority
- Emergency aircraft land first
- Runways released after landing
- No runway assigned to multiple aircraft
- All 4 RunwayManagerWorker threads active
- Runway events logged correctly
