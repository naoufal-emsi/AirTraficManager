# Part 1: Runway Management & Landing System
## Technical Specifications Document

**Assigned to:** Your Partner  
**Focus:** Runway control, landing operations, emergency handling  
**Java Concepts:** Semaphores, Thread Pools, BlockingQueues, Concurrent Collections
 
--- 

## 1. FOLDER STRUCTURE

```
src/main/java/com/atc/
├── core/
│   ├── controllers/
│   │   ├── LandingController.java
│   │   └── EmergencyController.java
│   ├── managers/
│   │   ├── RunwayManager.java
│   │   └── ResourceManager.java
│   ├── models/
│   │   ├── Aircraft.java
│   │   ├── Runway.java
│   │   └── LandingRequest.java
│   ├── threads/
│   │   ├── LandingWorker.java
│   │   ├── EmergencyWorker.java
│   │   └── RunwayMonitor.java
│   └── dao/
│       ├── AircraftDAO.java
│       └── RunwayEventDAO.java
└── shared/
    ├── database/
    │   └── DatabaseManager.java
    └── models/
        └── Event.java
```

---

## 2. DETAILED CLASS SPECIFICATIONS

### 2.1 Aircraft.java (Model)
```java
package com.atc.core.models;

public class Aircraft {
    // Fields
    private String aircraftId;           // "AC001", "AC002"
    private String callsign;             // "BA101", "UA202"
    private int fuelLevel;               // 0-100 percentage
    private String status;               // "APPROACHING", "LANDING", "LANDED"
    private boolean isEmergency;         // Emergency flag
    private LocalDateTime requestTime;   // When landing was requested
    private int priority;                // 1=Emergency, 2=Low fuel, 3=Normal
    
    // Constructors
    public Aircraft(String aircraftId, String callsign, int fuelLevel)
    public Aircraft(String aircraftId, String callsign, int fuelLevel, boolean isEmergency)
    
    // Methods (ALL getters/setters required)
    public String getAircraftId()
    public void setAircraftId(String aircraftId)
    public String getCallsign()
    public void setCallsign(String callsign)
    public int getFuelLevel()
    public void setFuelLevel(int fuelLevel)
    public String getStatus()
    public void setStatus(String status)
    public boolean isEmergency()
    public void setEmergency(boolean emergency)
    public LocalDateTime getRequestTime()
    public void setRequestTime(LocalDateTime requestTime)
    public int getPriority()
    public void setPriority(int priority)
    
    // Business Methods
    public boolean isLowFuel()           // Returns true if fuel < 20%
    public boolean isCriticalFuel()      // Returns true if fuel < 10%
    public void declareEmergency()       // Sets emergency=true, priority=1
    public String toString()             // Format: "Aircraft[id=AC001, callsign=BA101, fuel=45%]"
}
```

### 2.2 Runway.java (Model)
```java
package com.atc.core.models;

public class Runway {
    // Fields
    private String runwayId;             // "RW01", "RW02"
    private boolean isOpen;              // true/false
    private Aircraft currentAircraft;    // Currently using runway
    private LocalDateTime lastUsed;      // Last usage timestamp
    private String status;               // "FREE", "OCCUPIED", "CLOSED"
    
    // Constructors
    public Runway(String runwayId)
    public Runway(String runwayId, boolean isOpen)
    
    // Methods (ALL getters/setters)
    public String getRunwayId()
    public void setRunwayId(String runwayId)
    public boolean isOpen()
    public void setOpen(boolean open)
    public Aircraft getCurrentAircraft()
    public void setCurrentAircraft(Aircraft currentAircraft)
    public LocalDateTime getLastUsed()
    public void setLastUsed(LocalDateTime lastUsed)
    public String getStatus()
    public void setStatus(String status)
    
    // Business Methods
    public boolean isAvailable()         // Returns isOpen && currentAircraft == null
    public void occupy(Aircraft aircraft) // Sets currentAircraft, status="OCCUPIED"
    public void release()                // Sets currentAircraft=null, status="FREE", lastUsed=now
    public void close(String reason)     // Sets isOpen=false, status="CLOSED"
    public void open()                   // Sets isOpen=true, status="FREE"
}
```

### 2.3 LandingRequest.java (Model)
```java
package com.atc.core.models;

public class LandingRequest implements Comparable<LandingRequest> {
    // Fields
    private String requestId;            // UUID
    private Aircraft aircraft;           // Aircraft requesting landing
    private LocalDateTime requestTime;   // When request was made
    private int priority;                // 1=Emergency, 2=Low fuel, 3=Normal
    private String status;               // "PENDING", "APPROVED", "REJECTED"
    
    // Constructors
    public LandingRequest(Aircraft aircraft)
    public LandingRequest(Aircraft aircraft, int priority)
    
    // Methods (ALL getters/setters)
    public String getRequestId()
    public Aircraft getAircraft()
    public LocalDateTime getRequestTime()
    public int getPriority()
    public String getStatus()
    // ... setters
    
    // Comparable implementation for priority queue
    @Override
    public int compareTo(LandingRequest other) // Compare by priority, then by time
    
    // Business Methods
    public boolean isEmergency()         // Returns priority == 1
    public void approve()                // Sets status="APPROVED"
    public void reject(String reason)    // Sets status="REJECTED"
}
```

---

## 3. THREAD SPECIFICATIONS

### 3.1 LandingWorker.java (Thread Class)
```java
package com.atc.core.threads;

public class LandingWorker implements Runnable {
    // Fields
    private final BlockingQueue<LandingRequest> landingQueue;
    private final RunwayManager runwayManager;
    private final String workerId;
    private volatile boolean running;
    
    // Constructor
    public LandingWorker(BlockingQueue<LandingRequest> landingQueue, 
                        RunwayManager runwayManager, String workerId)
    
    // Thread Methods
    @Override
    public void run()                    // Main thread execution
    public void stop()                   // Sets running=false
    
    // Business Methods
    private void processLandingRequest(LandingRequest request)
    private void simulateLanding(Aircraft aircraft, Runway runway) // Sleep 3-5 seconds
    private void logLandingEvent(Aircraft aircraft, Runway runway)
}

// THREAD USAGE:
// - 3 LandingWorker threads running continuously
// - Each thread takes requests from landingQueue
// - Processes landing using runwayManager.acquireRunway()
// - Simulates landing time (Thread.sleep)
// - Releases runway using runwayManager.releaseRunway()
```

### 3.2 EmergencyWorker.java (Thread Class)
```java
package com.atc.core.threads;

public class EmergencyWorker implements Runnable {
    // Fields
    private final PriorityBlockingQueue<LandingRequest> emergencyQueue;
    private final RunwayManager runwayManager;
    private volatile boolean running;
    
    // Constructor
    public EmergencyWorker(PriorityBlockingQueue<LandingRequest> emergencyQueue,
                          RunwayManager runwayManager)
    
    // Thread Methods
    @Override
    public void run()                    // Main thread execution - HIGHEST PRIORITY
    public void stop()
    
    // Business Methods
    private void processEmergency(LandingRequest request)
    private void clearRunwayForEmergency() // Force release occupied runways
    private void notifyEmergencyServices(Aircraft aircraft)
}

// THREAD USAGE:
// - 1 EmergencyWorker thread with HIGH priority
// - Monitors emergencyQueue continuously
// - Can interrupt normal landing operations
// - Forces runway availability for emergencies
```

### 3.3 RunwayMonitor.java (Thread Class)
```java
package com.atc.core.threads;

public class RunwayMonitor implements Runnable {
    // Fields
    private final List<Runway> runways;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;
    
    // Constructor
    public RunwayMonitor(List<Runway> runways)
    
    // Thread Methods
    @Override
    public void run()                    // Monitors runway status every 2 seconds
    public void stop()
    
    // Business Methods
    private void checkRunwayStatus()     // Verify runway states
    private void detectStuckAircraft()  // Find aircraft occupying runway too long
    private void logRunwayStatistics()  // Log usage statistics
}

// THREAD USAGE:
// - 1 RunwayMonitor thread using ScheduledExecutorService
// - Runs every 2 seconds
// - Monitors runway utilization
// - Detects stuck aircraft (>10 minutes on runway)
```

---

## 4. MANAGER CLASSES

### 4.1 RunwayManager.java (Core Manager)
```java
package com.atc.core.managers;

public class RunwayManager {
    // Fields
    private final Semaphore runwaySemaphore;        // Controls runway access
    private final List<Runway> runways;             // Available runways
    private final ConcurrentHashMap<String, Runway> runwayMap; // Quick lookup
    private final AtomicInteger activeRunways;      // Count of open runways
    
    // Constructor
    public RunwayManager(int numberOfRunways)       // Creates runways RW01, RW02, etc.
    
    // SEMAPHORE METHODS
    public boolean acquireRunway(Aircraft aircraft) throws InterruptedException
    // - Calls runwaySemaphore.acquire()
    // - Finds available runway
    // - Assigns aircraft to runway
    // - Returns true if successful
    
    public void releaseRunway(String runwayId)
    // - Finds runway by ID
    // - Calls runway.release()
    // - Calls runwaySemaphore.release()
    // - Logs release event
    
    public boolean tryAcquireRunway(Aircraft aircraft, long timeout, TimeUnit unit)
    // - Calls runwaySemaphore.tryAcquire(timeout, unit)
    // - Non-blocking version with timeout
    
    // RUNWAY MANAGEMENT
    public void closeRunway(String runwayId, String reason)
    // - Sets runway.close(reason)
    // - Reduces semaphore permits
    // - Notifies all waiting aircraft
    
    public void openRunway(String runwayId)
    // - Sets runway.open()
    // - Increases semaphore permits
    
    public List<Runway> getAvailableRunways()       // Stream filter for available runways
    public Runway findAvailableRunway()             // Returns first available runway
    public int getAvailableRunwayCount()            // Returns semaphore.availablePermits()
    
    // STATISTICS
    public Map<String, Object> getRunwayStatistics() // Usage stats per runway
    public double getRunwayUtilization()            // Percentage of time runways occupied
}

// SEMAPHORE USAGE:
// - Semaphore permits = number of open runways
// - acquire() blocks until runway available
// - release() frees up runway for next aircraft
// - tryAcquire() for timeout-based attempts
```

### 4.2 ResourceManager.java (Resource Coordinator)
```java
package com.atc.core.managers;

public class ResourceManager {
    // Fields
    private final Semaphore gateSemaphore;          // Gates after landing
    private final Semaphore taxiwaySemaphore;       // Taxiway capacity
    private final AtomicInteger totalAircraft;      // Count of aircraft in system
    
    // Constructor
    public ResourceManager(int gates, int taxiwayCapacity)
    
    // SEMAPHORE METHODS
    public boolean acquireGate(Aircraft aircraft) throws InterruptedException
    public void releaseGate(Aircraft aircraft)
    public boolean acquireTaxiway(Aircraft aircraft) throws InterruptedException
    public void releaseTaxiway(Aircraft aircraft)
    
    // RESOURCE MONITORING
    public boolean hasAvailableResources()          // Check all semaphores
    public Map<String, Integer> getResourceStatus() // Available permits for each resource
    public void logResourceUsage()                  // Log current resource utilization
}
```

---

## 5. CONTROLLER CLASSES

### 5.1 LandingController.java (Main Controller)
```java
package com.atc.core.controllers;

public class LandingController {
    // THREAD POOLS
    private final ExecutorService landingThreadPool;    // 3 threads for landing operations
    private final ExecutorService emergencyThreadPool;  // 1 thread for emergencies
    private final ScheduledExecutorService monitorPool; // 1 thread for monitoring
    
    // QUEUES
    private final BlockingQueue<LandingRequest> landingQueue;        // Normal landing requests
    private final PriorityBlockingQueue<LandingRequest> emergencyQueue; // Emergency requests
    
    // MANAGERS
    private final RunwayManager runwayManager;
    private final ResourceManager resourceManager;
    
    // ATOMIC COUNTERS
    private final AtomicInteger totalLandings;
    private final AtomicInteger emergencyLandings;
    
    // Constructor
    public LandingController(RunwayManager runwayManager, ResourceManager resourceManager)
    
    // MAIN OPERATIONS
    public void requestLanding(Aircraft aircraft)
    // - Creates LandingRequest
    // - Determines priority (emergency/normal)
    // - Adds to appropriate queue (landingQueue or emergencyQueue)
    // - Logs request event
    
    public void declareEmergency(Aircraft aircraft)
    // - Sets aircraft.setEmergency(true)
    // - Creates high-priority LandingRequest
    // - Adds to emergencyQueue
    // - Interrupts normal operations if needed
    
    public void processLandings()
    // - Starts all worker threads
    // - Monitors queues
    // - Handles thread lifecycle
    
    // THREAD MANAGEMENT
    public void startLandingWorkers()               // Start 3 LandingWorker threads
    public void startEmergencyWorker()             // Start 1 EmergencyWorker thread
    public void startMonitoring()                  // Start RunwayMonitor thread
    public void shutdown()                          // Graceful shutdown of all threads
    
    // STATISTICS & MONITORING
    public int getQueueSize()                       // landingQueue.size()
    public int getEmergencyQueueSize()              // emergencyQueue.size()
    public List<Aircraft> getWaitingAircraft()     // All aircraft in queues
    public Map<String, Object> getLandingStatistics() // Performance metrics
}

// THREAD POOL CONFIGURATION:
// landingThreadPool = Executors.newFixedThreadPool(3)
// emergencyThreadPool = Executors.newSingleThreadExecutor()
// monitorPool = Executors.newScheduledThreadPool(1)
```

### 5.2 EmergencyController.java (Emergency Handler)
```java
package com.atc.core.controllers;

public class EmergencyController {
    // Fields
    private final PriorityBlockingQueue<Aircraft> emergencyQueue;
    private final RunwayManager runwayManager;
    private final ExecutorService emergencyPool;    // Single thread for emergencies
    
    // Constructor
    public EmergencyController(RunwayManager runwayManager)
    
    // EMERGENCY OPERATIONS
    public void handleEmergency(Aircraft aircraft)
    // - Sets highest priority
    // - Clears runway if needed
    // - Processes immediately
    
    public void clearAllRunways()                   // Force release all runways
    public void notifyEmergencyServices(Aircraft aircraft) // Log emergency event
    public void processEmergencyQueue()             // Continuous processing
    
    // PRIORITY MANAGEMENT
    public void escalatePriority(Aircraft aircraft) // Move to front of queue
    public boolean isEmergencyActive()              // Check if emergency in progress
}
```

---

## 6. DATABASE ACCESS OBJECTS (DAO)

### 6.1 AircraftDAO.java
```java
package com.atc.core.dao;

public class AircraftDAO {
    private final MongoCollection<Document> aircraftCollection;
    
    // Constructor
    public AircraftDAO(DatabaseManager dbManager)
    
    // CRUD OPERATIONS
    public void saveAircraft(Aircraft aircraft)     // Insert aircraft document
    public Aircraft findAircraftById(String id)     // Find by aircraftId
    public List<Aircraft> findAllAircraft()         // Get all aircraft
    public void updateAircraftStatus(String id, String status) // Update status field
    public void updateFuelLevel(String id, int fuelLevel) // Update fuel
    public void deleteAircraft(String id)           // Remove aircraft
    
    // QUERIES
    public List<Aircraft> findAircraftByStatus(String status) // Filter by status
    public List<Aircraft> findLowFuelAircraft()     // Find aircraft with fuel < 20%
    public List<Aircraft> findEmergencyAircraft()   // Find emergency aircraft
    
    // STATISTICS
    public long countAircraftByStatus(String status) // Count aircraft in status
    public double getAverageFuelLevel()              // Average fuel across all aircraft
}
```

### 6.2 RunwayEventDAO.java
```java
package com.atc.core.dao;

public class RunwayEventDAO {
    private final MongoCollection<Document> eventsCollection;
    
    // Constructor
    public RunwayEventDAO(DatabaseManager dbManager)
    
    // EVENT LOGGING
    public void logLandingEvent(Aircraft aircraft, Runway runway, LocalDateTime timestamp)
    public void logTakeoffEvent(Aircraft aircraft, Runway runway, LocalDateTime timestamp)
    public void logEmergencyEvent(Aircraft aircraft, String eventType, LocalDateTime timestamp)
    public void logRunwayStatusChange(String runwayId, String oldStatus, String newStatus)
    
    // EVENT QUERIES
    public List<Document> getEventsByAircraft(String aircraftId) // All events for aircraft
    public List<Document> getEventsByRunway(String runwayId)     // All events for runway
    public List<Document> getEventsByTimeRange(LocalDateTime start, LocalDateTime end)
    public List<Document> getEmergencyEvents()                  // All emergency events
    
    // STATISTICS
    public long countEventsByType(String eventType)             // Count specific event types
    public Map<String, Long> getEventStatistics()               // Event counts by type
}
```

---

## 7. MONGODB COLLECTIONS STRUCTURE

### 7.1 aircraft Collection
```json
{
  "_id": ObjectId("..."),
  "aircraftId": "AC001",
  "callsign": "BA101",
  "fuelLevel": 45,
  "status": "APPROACHING",
  "isEmergency": false,
  "requestTime": ISODate("2024-01-15T10:30:00Z"),
  "priority": 3,
  "createdAt": ISODate("2024-01-15T10:25:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z")
}
```

### 7.2 runway_events Collection
```json
{
  "_id": ObjectId("..."),
  "eventType": "LANDING_REQUEST",
  "aircraftId": "AC001",
  "runwayId": "RW01",
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "details": {
    "fuelLevel": 45,
    "isEmergency": false,
    "priority": 3
  },
  "workerId": "LandingWorker-1"
}
```

### 7.3 runways Collection
```json
{
  "_id": ObjectId("..."),
  "runwayId": "RW01",
  "isOpen": true,
  "status": "FREE",
  "currentAircraft": null,
  "lastUsed": ISODate("2024-01-15T10:25:00Z"),
  "totalUsage": 156,
  "createdAt": ISODate("2024-01-15T08:00:00Z")
}
```

---

## 8. THREAD EXECUTION FLOW

### 8.1 Normal Landing Process
```
1. Aircraft requests landing → LandingController.requestLanding()
2. LandingRequest created → Added to landingQueue
3. LandingWorker thread takes request → landingQueue.take()
4. Worker calls runwayManager.acquireRunway() → Semaphore.acquire()
5. Runway assigned → Aircraft lands (Thread.sleep simulation)
6. Worker calls runwayManager.releaseRunway() → Semaphore.release()
7. Event logged to MongoDB → RunwayEventDAO.logLandingEvent()
```

### 8.2 Emergency Landing Process
```
1. Aircraft declares emergency → EmergencyController.handleEmergency()
2. Emergency LandingRequest created → Added to emergencyQueue (priority=1)
3. EmergencyWorker thread processes → emergencyQueue.take()
4. Clear runway if needed → runwayManager.clearAllRunways()
5. Force acquire runway → runwayManager.acquireRunway()
6. Emergency landing → Immediate processing
7. Release runway → runwayManager.releaseRunway()
8. Log emergency event → RunwayEventDAO.logEmergencyEvent()
```

---

## 9. TESTING SCENARIOS FOR PART 1

### 9.1 Scenario 1: Multiple Landing Requests
```java
// Test Method: testMultipleLandingRequests()
// Create 5 aircraft requesting landing simultaneously
// Verify: Only 2 aircraft land at once (semaphore limit)
// Verify: FIFO order maintained
// Verify: All aircraft eventually land
```

### 9.2 Scenario 2: Emergency Priority
```java
// Test Method: testEmergencyPriority()
// Create 3 normal aircraft in landing queue
// Add 1 emergency aircraft
// Verify: Emergency aircraft lands first
// Verify: Normal queue resumes after emergency
```

### 9.3 Scenario 3: Runway Closure
```java
// Test Method: testRunwayClosureHandling()
// Start with 2 open runways
// Close 1 runway during operations
// Verify: Semaphore permits reduced
// Verify: Operations continue with 1 runway
// Verify: Runway reopening restores capacity
```

---

## 10. PERFORMANCE REQUIREMENTS

### 10.1 Thread Performance
- **Landing Workers**: Process 1 landing every 3-5 seconds
- **Emergency Worker**: Process emergency within 1 second
- **Runway Monitor**: Check status every 2 seconds
- **Queue Capacity**: Handle 50+ concurrent landing requests

### 10.2 Semaphore Usage
- **Runway Semaphore**: 2 permits (2 runways)
- **Gate Semaphore**: 5 permits (5 gates)
- **Taxiway Semaphore**: 10 permits (taxiway capacity)

### 10.3 Database Performance
- **Insert Operations**: < 100ms per event
- **Query Operations**: < 50ms per query
- **Concurrent Connections**: Support 10+ simultaneous operations

---

## 11. INTEGRATION POINTS WITH PART 2

### 11.1 Shared Classes You'll Use
- `DatabaseManager` - MongoDB connection management
- `Event` - Base event class for logging
- `AirTrafficGUI` - GUI integration points

### 11.2 Methods Part 2 Will Call
```java
// From LandingController
public void requestLanding(Aircraft aircraft)        // Called by FlightScheduler
public List<Aircraft> getWaitingAircraft()          // Called by WeatherService
public void declareEmergency(Aircraft aircraft)     // Called by EmergencyHandler

// From RunwayManager  
public int getAvailableRunwayCount()                 // Called by FlightScheduler
public void closeRunway(String runwayId, String reason) // Called by WeatherService
```

### 11.3 Events You'll Generate for Part 2
- `LANDING_COMPLETED` - Aircraft successfully landed
- `EMERGENCY_DECLARED` - Emergency situation started
- `RUNWAY_CLOSED` - Runway became unavailable
- `FUEL_CRITICAL` - Aircraft fuel below 10%

---

## 12. DELIVERABLES CHECKLIST

### 12.1 Code Files (11 files)
- [ ] `Aircraft.java` - Complete with all methods
- [ ] `Runway.java` - Complete with all methods  
- [ ] `LandingRequest.java` - Complete with Comparable
- [ ] `LandingWorker.java` - Runnable thread class
- [ ] `EmergencyWorker.java` - Runnable thread class
- [ ] `RunwayMonitor.java` - Scheduled thread class
- [ ] `RunwayManager.java` - Semaphore management
- [ ] `ResourceManager.java` - Multi-semaphore coordination
- [ ] `LandingController.java` - Main controller with thread pools
- [ ] `EmergencyController.java` - Emergency handling
- [ ] `AircraftDAO.java` - MongoDB operations
- [ ] `RunwayEventDAO.java` - Event logging

### 12.2 Thread Implementation
- [ ] 3 LandingWorker threads in ExecutorService
- [ ] 1 EmergencyWorker thread with high priority
- [ ] 1 RunwayMonitor thread with ScheduledExecutorService
- [ ] Proper thread shutdown in all controllers

### 12.3 Semaphore Implementation  
- [ ] Runway semaphore with 2 permits
- [ ] Gate semaphore with 5 permits
- [ ] Taxiway semaphore with 10 permits
- [ ] Proper acquire/release patterns

### 12.4 Queue Implementation
- [ ] BlockingQueue for normal landing requests
- [ ] PriorityBlockingQueue for emergency requests
- [ ] Proper queue monitoring and statistics

### 12.5 Database Integration
- [ ] MongoDB collections created
- [ ] All CRUD operations implemented
- [ ] Event logging functional
- [ ] Query methods working

### 12.6 Testing
- [ ] Unit tests for all classes
- [ ] Integration tests for 3 scenarios
- [ ] Thread safety verification
- [ ] Performance benchmarks

---

**CRITICAL SUCCESS FACTORS:**
1. **Semaphore Usage**: Must properly control runway access
2. **Thread Safety**: All shared data must be thread-safe
3. **Queue Management**: Proper FIFO and priority handling
4. **Database Integration**: All events must be logged
5. **Emergency Handling**: Must interrupt normal operations
6. **Resource Cleanup**: Proper thread and resource shutdown

**ESTIMATED EFFORT:** 40-50 hours of development time