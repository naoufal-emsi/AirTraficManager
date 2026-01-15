# AIR TRAFFIC CONTROL SIMULATION SYSTEM

## Complete Java Implementation with Swing GUI & MongoDB

---

## SYSTEM OVERVIEW

Full Air Traffic Control simulation with:
- **Swing GUI** with real-time updates
- **MongoDB** database for all events
- **Multi-threaded** architecture with 10+ worker threads
- **Realistic aircraft** dynamics (fuel, position, speed)
- **5 Emergency scenarios** with automatic escalation
- **Dynamic runway** assignment with weather impact

---

## EMERGENCY SCENARIOS

### 1. FUEL_LOW
- Triggered when fuel < 120% of required
- Priority: 50
- Actions: Priority vectors, direct routing, speed reduction
- **Auto-escalates to FUEL_CRITICAL** if fuel drops below 105% required

### 2. FUEL_CRITICAL
- Triggered when fuel < 105% of required
- Priority: 10
- Actions: MAYDAY declared, immediate landing, no holding

### 3. EMERGENCY_FIRE
- Priority: 1 (HIGHEST)
- Actions: Engine shutdown, fire suppression, emergency services positioned
- Immediate landing at nearest runway

### 4. EMERGENCY_MEDICAL
- Priority: 5
- Actions: PAN-PAN/MAYDAY declared, ambulance dispatched
- Divert to nearest suitable airport

### 5. SECURITY_ONBOARD
- Priority: 8
- Actions: Discrete emergency code, law enforcement alerted
- Controlled diversion, secure landing

### 6. WEATHER_STORM
- Priority: 30
- Actions: Reroute or holding, runway closures
- Diversion if storm blocks approach beyond fuel limits

---

## ARCHITECTURE

### Models
- **Aircraft**: Callsign, fuel, speed, position, ETA, emergency type, priority
- **Runway**: ID, status, assigned aircraft, weather impact, emergency services

### Workers (Threads)
1. **AircraftUpdateWorker**: Updates position & fuel every 5s
2. **FuelMonitoringWorker**: Checks fuel status every 3s, auto-escalates
3. **RunwayManagerWorker** (3 threads): Assigns runways based on priority
4. **EmergencyHandlerWorker**: Handles all emergency scenarios
5. **WeatherWorker**: Generates weather events affecting runways

### Controllers
- **EmergencyController**: Declares and manages all emergencies

### Database (MongoDB)
Collections:
- `aircraft` - All aircraft data snapshots
- `runway_events` - Runway assignments and releases
- `emergency_events` - All emergency declarations
- `weather_events` - Weather impacts on runways

---

## GUI FEATURES

### Display Panels
1. **Active Aircraft Table**: Callsign, fuel, speed, distance, status, emergency, runway
2. **Runway Status Table**: Runway ID, status, assigned aircraft, weather
3. **Emergency Alerts Table**: Active emergencies with priority and actions
4. **System Log**: Real-time event logging with timestamps

### Control Buttons
- **Add Aircraft**: Generate random aircraft
- **Fuel Low Emergency**: Create fuel low scenario
- **Fire Emergency**: Create fire emergency
- **Medical Emergency**: Create medical emergency
- **Security Threat**: Create security threat
- **Weather Storm**: Generate weather event

### Statistics Bar
- Total aircraft count
- Active emergencies
- Landed aircraft
- Active runways

---

## REALISTIC FEATURES

### Aircraft Dynamics
- Fuel consumption based on burn rate
- Position updates based on speed
- ETA calculation
- Automatic fuel escalation

### Runway Management
- Priority-based queue (PriorityBlockingQueue)
- Emergency aircraft get immediate access
- Weather can close runways
- 10-second landing duration

### Weather System
- Random weather events every 30s
- Affects 40% of runways
- 20-second storm duration
- Blocks non-emergency landings

---

## RUNNING THE SYSTEM

### Prerequisites
```bash
# Start MongoDB
mongod --dbpath /path/to/data

# Ensure Java 11+ installed
java -version
```

### Compile & Run
```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.atc.AirTrafficSystem"
```

### Alternative
```bash
# Direct Java execution
cd target/classes
java com.atc.AirTrafficSystem
```

---

## TESTING SCENARIOS

### Test 1: Normal Operations
1. Click "Add Aircraft" 5 times
2. Watch aircraft land automatically
3. Observe runway assignments

### Test 2: Fuel Emergency Escalation
1. Click "Fuel Low Emergency"
2. Watch fuel decrease
3. Observe auto-escalation to FUEL_CRITICAL
4. See priority increase

### Test 3: Fire Emergency
1. Click "Fire Emergency"
2. Observe Priority 1 assignment
3. Watch immediate runway assignment
4. Check emergency services ready

### Test 4: Weather Impact
1. Click "Weather Storm"
2. See runways marked as weather-affected
3. Watch aircraft rerouting
4. Observe runway availability changes

### Test 5: Multiple Emergencies
1. Create multiple emergency types
2. Observe priority-based landing order
3. Check emergency table updates

---

## THREAD SAFETY

- **CopyOnWriteArrayList** for aircraft and runways
- **PriorityBlockingQueue** for landing queue
- **ExecutorService** for thread pool management
- **No synchronized blocks** - using concurrent collections

---

## DATABASE SCHEMA

### aircraft
```json
{
  "callsign": "UAL123",
  "fuel": 85.5,
  "speed": 450,
  "distance": 120.5,
  "status": "APPROACHING",
  "emergency": "FUEL_LOW",
  "priority": 50,
  "runway": "RWY-09L",
  "timestamp": ISODate()
}
```

### emergency_events
```json
{
  "callsign": "DAL456",
  "emergencyType": "FIRE",
  "priority": 1,
  "fuel": 95.0,
  "details": "MAYDAY - Fire indication, requesting immediate landing",
  "timestamp": ISODate()
}
```

---

## KEY CLASSES

- `AirTrafficSystem.java` - Main entry point
- `Aircraft.java` - Aircraft model with emergency logic
- `Runway.java` - Runway model with weather tracking
- `AirTrafficControlGUI.java` - Swing GUI
- `DatabaseManager.java` - MongoDB integration
- `AircraftGenerator.java` - Realistic aircraft generation
- `EmergencyController.java` - Emergency management
- All Worker classes - Threading implementation

---

## SYSTEM OUTPUT

```
=== AIR TRAFFIC CONTROL SYSTEM STARTING ===
✓ Database connected
✓ Runways initialized: 4
✓ Initial aircraft created: 5
✓ Worker threads started: 8
✓ GUI launched
```

---

## FEATURES SUMMARY

✅ Swing GUI with 4 panels
✅ MongoDB with 4 collections
✅ 8+ worker threads
✅ 5 emergency scenarios
✅ Automatic fuel escalation
✅ Priority-based landing queue
✅ Weather impact system
✅ Real-time position/fuel updates
✅ Realistic aircraft generation
✅ Complete event logging
✅ Thread-safe operations
✅ Graceful shutdown

**COMPLETE SYSTEM READY FOR DEMONSTRATION**
