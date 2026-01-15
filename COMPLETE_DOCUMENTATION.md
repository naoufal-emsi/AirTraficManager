# AIR TRAFFIC CONTROL SIMULATION - COMPLETE SYSTEM

## 🚀 FULL IMPLEMENTATION

Complete Air Traffic Control simulation with:
- **Swing GUI** with real-time updates (4 panels)
- **MongoDB** database integration (4 collections)
- **Multi-threading** (10+ worker threads)
- **Realistic aircraft dynamics** (fuel, position, speed, ETA)
- **5 Emergency scenarios** with automatic escalation
- **Dynamic runway management** with priority queuing
- **Weather system** affecting runway availability

---

## 📋 PROJECT STRUCTURE

```
AirTraficManager/
├── src/main/java/com/atc/
│   ├── AirTrafficSystem.java          # Main entry point
│   ├── core/models/
│   │   ├── Aircraft.java              # Aircraft model with emergency logic
│   │   └── Runway.java                # Runway model with weather tracking
│   ├── controllers/
│   │   └── EmergencyController.java   # Emergency management
│   ├── database/
│   │   └── DatabaseManager.java       # MongoDB integration
│   ├── gui/
│   │   └── AirTrafficControlGUI.java  # Swing GUI
│   ├── utils/
│   │   └── AircraftGenerator.java     # Realistic aircraft generation
│   └── workers/
│       ├── AircraftUpdateWorker.java  # Position/fuel updates
│       ├── FuelMonitoringWorker.java  # Fuel monitoring & escalation
│       ├── RunwayManagerWorker.java   # Runway assignments
│       ├── EmergencyHandlerWorker.java # Emergency handling
│       └── WeatherWorker.java         # Weather events
├── pom.xml
├── run.sh                              # Run script
├── setup_mongodb.js                    # MongoDB setup
└── SYSTEM_README.md                    # This file
```

---

## 🎯 EMERGENCY SCENARIOS

### 1. FUEL_LOW (Priority: 50)
- **Trigger**: Fuel < 120% of required
- **Actions**: 
  - Request priority vectors
  - Direct routing
  - Speed reduced by 10%
  - Avoid holding patterns
- **Auto-escalation**: Escalates to FUEL_CRITICAL if fuel < 105% required

### 2. FUEL_CRITICAL (Priority: 10)
- **Trigger**: Fuel < 105% of required OR manual escalation
- **Actions**:
  - MAYDAY declared
  - Immediate landing clearance
  - No holding
  - Direct approach

### 3. EMERGENCY_FIRE (Priority: 1 - HIGHEST)
- **Actions**:
  - MAYDAY MAYDAY declared
  - Engine shutdown procedures
  - Fire suppression activated
  - Emergency services positioned on runway
  - Immediate landing at nearest available runway

### 4. EMERGENCY_MEDICAL (Priority: 5)
- **Actions**:
  - PAN-PAN or MAYDAY based on severity
  - Ambulance dispatched before landing
  - Divert to nearest suitable airport
  - ATC clears path

### 5. SECURITY_ONBOARD (Priority: 8)
- **Actions**:
  - Discrete emergency transponder code set
  - Law enforcement alerted
  - Controlled diversion
  - Secure landing area prepared

### 6. WEATHER_STORM (Priority: 30)
- **Actions**:
  - Reroute or holding pattern
  - Runway closures if severe
  - Diversion if storm blocks approach beyond fuel limits
  - Dynamic runway availability changes

---

## 🧵 THREADING ARCHITECTURE

### Worker Threads (8 total):

1. **AircraftUpdateWorker** (1 thread)
   - Updates aircraft position every 5 seconds
   - Calculates fuel consumption
   - Updates ETA
   - Saves to database

2. **FuelMonitoringWorker** (1 thread)
   - Checks fuel status every 3 seconds
   - Automatic escalation logic
   - FUEL_LOW → FUEL_CRITICAL escalation

3. **RunwayManagerWorker** (3 threads)
   - Priority-based landing queue (PriorityBlockingQueue)
   - Assigns runways based on emergency priority
   - 10-second landing duration simulation
   - Releases runways after landing

4. **EmergencyHandlerWorker** (1 thread)
   - Monitors all emergency scenarios
   - Logs emergency actions to database
   - Coordinates emergency responses

5. **WeatherWorker** (1 thread)
   - Generates weather events every 30 seconds
   - 30% chance of weather event
   - Affects 40% of runways randomly
   - 20-second storm duration

### Thread Safety:
- **CopyOnWriteArrayList** for aircraft and runways
- **PriorityBlockingQueue** for landing queue
- **ExecutorService** for thread pool management
- **No synchronized blocks** - using concurrent collections

---

## 🖥️ GUI FEATURES

### 4 Main Panels:

#### 1. Active Aircraft Table
- Callsign
- Fuel level (real-time)
- Speed (knots)
- Distance to airport (nautical miles)
- Status (APPROACHING, HOLDING, LANDING, LANDED, DIVERTED)
- Emergency type
- Assigned runway

#### 2. Runway Status Table
- Runway ID (RWY-09L, RWY-09R, RWY-27L, RWY-27R)
- Status (FREE, OCCUPIED)
- Assigned aircraft
- Weather status (Clear, AFFECTED)

#### 3. Emergency Alerts Table
- Callsign
- Emergency type
- Priority level
- Action taken

#### 4. System Log
- Timestamped events
- Auto-scroll
- All system activities

### Control Buttons:
- **Add Aircraft**: Generate random aircraft
- **Fuel Low Emergency**: Create fuel low scenario
- **Fire Emergency**: Create fire emergency
- **Medical Emergency**: Create medical emergency
- **Security Threat**: Create security threat
- **Weather Storm**: Generate weather event

### Statistics Bar:
- Total aircraft count
- Active emergencies
- Landed aircraft
- Active runways / Total runways

---

## 🗄️ MONGODB INTEGRATION

### Database: `airTrafficControl`

### Collections:

#### 1. aircraft
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

#### 2. runway_events
```json
{
  "runwayId": "RWY-09L",
  "status": "OCCUPIED",
  "event": "Aircraft UAL123 assigned for landing",
  "aircraft": "UAL123",
  "timestamp": ISODate()
}
```

#### 3. emergency_events
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

#### 4. weather_events
```json
{
  "description": "Thunderstorm affecting runways",
  "affectedRunways": ["RWY-09L", "RWY-27R"],
  "timestamp": ISODate()
}
```

---

## 🚀 RUNNING THE SYSTEM

### Prerequisites:

1. **Java 25** installed
```bash
java -version
```

2. **MongoDB** running on localhost:27017
```bash
mongod --dbpath /path/to/data
```

3. **Maven** installed
```bash
mvn -version
```

### Setup Database:
```bash
mongosh < setup_mongodb.js
```

### Compile:
```bash
mvn clean compile
```

### Run:
```bash
# Option 1: Using run script
./run.sh

# Option 2: Using Maven
mvn exec:java -Dexec.mainClass="com.atc.AirTrafficSystem"

# Option 3: Direct Java
cd target/classes
java com.atc.AirTrafficSystem
```

---

## 🧪 TESTING SCENARIOS

### Scenario 1: Normal Operations
1. Click "Add Aircraft" 5 times
2. Watch aircraft automatically land
3. Observe runway assignments in real-time
4. Check system log for events

### Scenario 2: Fuel Emergency Escalation
1. Click "Fuel Low Emergency"
2. Watch fuel level decrease in real-time
3. Observe automatic escalation to FUEL_CRITICAL
4. See priority change from 50 → 10
5. Watch immediate landing

### Scenario 3: Fire Emergency (Highest Priority)
1. Click "Fire Emergency"
2. Observe Priority 1 assignment
3. Watch immediate runway assignment
4. Check emergency services ready
5. See MAYDAY in system log

### Scenario 4: Weather Impact
1. Click "Weather Storm"
2. See runways marked as "AFFECTED"
3. Watch aircraft rerouting
4. Observe runway availability changes
5. Wait 20 seconds for weather to clear

### Scenario 5: Multiple Emergencies
1. Create multiple emergency types simultaneously
2. Observe priority-based landing order:
   - FIRE (Priority 1) lands first
   - MEDICAL (Priority 5) lands second
   - FUEL_CRITICAL (Priority 10) lands third
   - FUEL_LOW (Priority 50) lands last
3. Check emergency table updates

### Scenario 6: Runway Saturation
1. Add 10+ aircraft rapidly
2. Watch priority queue management
3. Observe 3 runway manager threads working
4. See aircraft in HOLDING status
5. Monitor queue processing

---

## 📊 REALISTIC FEATURES

### Aircraft Dynamics:
- **Fuel consumption**: Based on burn rate (0.5-1.0 units/min)
- **Position updates**: Based on speed (400-600 knots)
- **ETA calculation**: Real-time based on distance and speed
- **Automatic fuel escalation**: Monitors fuel vs. required

### Runway Management:
- **4 runways**: RWY-09L, RWY-09R, RWY-27L, RWY-27R
- **Priority queue**: Emergency aircraft get immediate access
- **10-second landing**: Realistic landing duration
- **Weather closures**: Runways can be temporarily unavailable

### Weather System:
- **Random events**: 30% chance every 30 seconds
- **Multiple runways**: Affects 40% of runways
- **Temporary**: 20-second duration
- **Dynamic**: Clears automatically

### Emergency Escalation:
- **Automatic**: FUEL_LOW → FUEL_CRITICAL
- **Condition-based**: Fuel vs. required calculation
- **Priority adjustment**: Priority increases with severity
- **Database logging**: All escalations logged

---

## 🎯 KEY FEATURES SUMMARY

✅ **Swing GUI** with 4 interactive panels
✅ **MongoDB** with 4 collections and indexes
✅ **8 worker threads** with proper thread safety
✅ **5 emergency scenarios** with realistic handling
✅ **Automatic fuel escalation** logic
✅ **Priority-based landing queue** (PriorityBlockingQueue)
✅ **Weather impact system** with dynamic runway closures
✅ **Real-time updates** (position, fuel, ETA)
✅ **Realistic aircraft generation** (callsigns, origins, destinations)
✅ **Complete event logging** to database
✅ **Thread-safe operations** (no synchronized blocks)
✅ **Graceful shutdown** with cleanup

---

## 📝 SYSTEM OUTPUT

```
=== AIR TRAFFIC CONTROL SYSTEM STARTING ===
✓ Database connected
✓ Runways initialized: 4
✓ Initial aircraft created: 5
✓ Worker threads started: 8
✓ GUI launched
```

---

## 🔧 TROUBLESHOOTING

### MongoDB Connection Failed:
```bash
# Start MongoDB
mongod --dbpath /path/to/data

# Or use default path
mongod
```

### Compilation Errors:
```bash
# Clean and recompile
mvn clean compile

# Check Java version
java -version  # Should be 25
```

### GUI Not Showing:
```bash
# Check DISPLAY variable (Linux)
echo $DISPLAY

# Run with X11 forwarding if remote
ssh -X user@host
```

---

## 📚 DOCUMENTATION

- **SYSTEM_README.md**: This file (complete documentation)
- **setup_mongodb.js**: MongoDB setup script
- **run.sh**: Quick run script
- **pom.xml**: Maven configuration

---

## 🎉 COMPLETE SYSTEM READY

This is a fully functional Air Traffic Control simulation with:
- Professional Swing GUI
- Real MongoDB integration
- Realistic threading architecture
- Complete emergency handling
- Automatic escalation logic
- Weather impact system
- Priority-based operations

**Ready for demonstration and testing!**
