# Deterministic & Realistic Simulation - Implementation Summary

## Overview
The Air Traffic Control simulator has been modified to implement deterministic, physically consistent, and realistic behavior through formula-based calculations and proper concurrency management.

## Key Changes

### 1. Deterministic Simulation Core (SimulationConfig.java)
- **Removed singleton pattern** - Now instantiated with explicit parameters
- **Added `metersPerTimeUnit`** - Configurable distance scale parameter
- **Formula-based calculations**:
  - `calculateDistanceTraveled(speedMetersPerSecond, timeSeconds)` - Pure physics: distance = speed × time
  - `calculateFuelBurned(burnRatePerHour, timeSeconds)` - Deterministic fuel consumption
  - `calculateETA(distanceMeters, speedMetersPerSecond)` - Computed from distance/speed
  - `calculateFuelNeeded(distance, speed, burnRate)` - Includes flight fuel + reserve
  - `isFuelLow()` / `isFuelCritical()` - Threshold-based escalation (1.2x and 1.05x)

### 2. Time & Distance Scaling
- All aircraft now use **meters** and **meters/second** (not knots/nautical miles)
- All time calculations use **seconds** (not arbitrary units)
- `metersPerTimeUnit` allows user to scale simulation (e.g., 1000m = 1 time unit)
- `timeStepSeconds` controls update frequency
- **No magic numbers** - all relationships derived from scale parameters

### 3. Aircraft Model (Aircraft.java)
- Changed from `int speed` (knots) to `double speedMetersPerSecond`
- Changed from `double distanceToAirport` (NM) to `double distanceToAirportMeters`
- Added `SimulationConfig config` reference for deterministic calculations
- `updatePosition()` now uses config formulas instead of hardcoded math
- `checkAndEscalateFuelStatus()` uses config thresholds for automatic escalation
- Fuel burn rate realistic: 800-1200 kg/h (typical for commercial aircraft)

### 4. GUI Controls (AirTrafficControlGUI.java)
**Added Restart Dialog**:
- Configure meters per time unit
- Configure time step (seconds)
- Configure fuel burn rate (kg/h)
- Configure fuel reserve (minutes)
- Configure landing duration (seconds)

**Added Status Change Controls**:
- APPROACHING button
- HOLDING button
- Clear Emergency button

**Enhanced Emergency Controls**:
- All emergency types trigger proper priority reordering
- Queue automatically reorders via remove + offer pattern

**Display Updates**:
- Speed shown as "m/s" instead of "knots"
- Distance shown as "m" instead of "nm"

### 5. Simulation Restart (SimulationManager.java)
- `restartSimulation()` method:
  - Stops all worker threads cleanly
  - Shuts down thread pools with timeout
  - Clears all queues and shared state
  - Reinitializes with new user-selected parameters
- No application restart required

### 6. Threading Model (AirTrafficSystem.java)
**ThreadPoolExecutor for aircraft**:
- Core pool size = CPU cores
- Max pool size = 2 × CPU cores
- Workers process aircraft updates concurrently

**Independent system workers**:
- `AircraftUpdateWorker` - Updates all aircraft positions using timeStepSeconds
- `FuelMonitoringWorker` - Monitors fuel and escalates emergencies
- `RunwayManagerWorker` (3 threads) - Consume landing queue concurrently
- `EmergencyHandlerWorker` - Handles emergency procedures
- `WeatherWorker` - Generates weather events

**Synchronization**:
- Minimal locks: only on runway assignment and queue reordering
- Uses `CopyOnWriteArrayList` for aircraft/runway lists
- Uses `PriorityBlockingQueue` for landing queue (thread-safe)
- Atomic operations where possible

### 7. Landing Queue & Priority
**Priority-driven ordering**:
- FIRE: priority = 1
- MEDICAL: priority = 5
- SECURITY: priority = 8
- FUEL_CRITICAL: priority = 10
- FUEL_LOW: priority = 50
- NORMAL: priority = 100

**Queue reordering**:
- When priority changes: `queue.remove(aircraft)` + `queue.offer(aircraft)`
- Multiple RunwayManagerWorker threads consume concurrently
- Runway selection based on availability and emergency status
- Landing duration uses `config.getLandingDurationSeconds()`

### 8. Realistic Emergent Complexity
**Chaos emerges from**:
- Multiple aircraft updating simultaneously (ThreadPoolExecutor)
- Shared runway contention (3 concurrent runway managers)
- Overlapping emergencies (fuel monitoring + manual triggers)
- Weather reducing capacity (random onset, deterministic duration)

**No hard-coded outcomes**:
- Fuel escalation computed from formulas
- ETA computed from distance/speed
- Landing order emerges from priority + availability
- Weather affects runways probabilistically but duration is fixed

### 9. Aircraft Generator (AircraftGenerator.java)
- Now requires `SimulationConfig` parameter
- Generates realistic values:
  - Fuel: 5000-15000 kg (typical commercial aircraft)
  - Speed: 100-200 m/s (≈ 194-388 knots, typical approach speeds)
  - Distance: scaled by `metersPerTimeUnit` × (50-250 time units)

## Determinism Guarantee
Given the same:
1. Initial aircraft states (fuel, speed, distance)
2. Simulation parameters (metersPerTimeUnit, timeStepSeconds, etc.)
3. Random seed (for weather/initial generation)

The simulation will produce:
- Same fuel consumption at each time step
- Same distance traveled at each time step
- Same ETA calculations
- Same emergency escalations
- Same landing order (priority-based)

## Usage

### Starting Simulation
Default parameters:
- metersPerTimeUnit: 1000.0
- timeStepSeconds: 2.0
- fuelBurnRatePerHour: 1000.0
- fuelReserveMinutes: 30.0
- landingDurationSeconds: 120.0

### Restarting Simulation
1. Click "Restart Simulation" button
2. Enter new parameters
3. System cleanly stops all threads
4. Clears all state
5. Starts with new configuration

### Manual Interventions
1. Select aircraft from table
2. Change status (APPROACHING/HOLDING)
3. Trigger emergency scenarios
4. Clear emergencies
5. System reacts naturally to new state

## Files Modified
1. `SimulationConfig.java` - Deterministic formulas, no singleton
2. `SimulationManager.java` - Clean restart, thread management
3. `Aircraft.java` - Metric units, config-based calculations
4. `AirTrafficSystem.java` - Integrated restart, worker management
5. `AircraftGenerator.java` - Config-aware generation
6. `AirTrafficControlGUI.java` - Restart dialog, status controls
7. `AircraftUpdateWorker.java` - Uses timeStepSeconds
8. `RunwayManagerWorker.java` - Uses config landing duration
9. `FuelMonitoringWorker.java` - Uses config for monitoring interval

## Verification
- ✓ No hard-coded scenario outcomes
- ✓ All movement/fuel/ETA computed from formulas
- ✓ Configurable time/distance scale
- ✓ Clean simulation restart
- ✓ GUI controls for manual intervention
- ✓ Priority queue reordering on state change
- ✓ Multiple concurrent runway managers
- ✓ Minimal synchronization
- ✓ Deterministic given same inputs
- ✓ Realistic emergent complexity
