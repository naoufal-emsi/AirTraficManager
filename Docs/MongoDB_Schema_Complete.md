# MongoDB Schema for Air Traffic Manager
## Complete Database Structure and Setup

---

## DATABASE SETUP

```javascript
// Connect to MongoDB
use airTrafficManager

// Drop existing collections (for fresh start)
db.aircraft.drop()
db.runways.drop() 
db.flights.drop()
db.landing_events.drop()
db.weather_alerts.drop()
db.fuel_alerts.drop()
db.system_events.drop()
```

---

## COLLECTION 1: AIRCRAFT
**Purpose:** Store all aircraft information (Part 1 & Part 2)

### Schema Definition:
```javascript
db.createCollection("aircraft", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["aircraftId", "callsign", "fuelLevel", "status"],
         properties: {
            aircraftId: {
               bsonType: "string",
               description: "Unique aircraft identifier - required"
            },
            callsign: {
               bsonType: "string", 
               description: "Flight callsign like BA101, UA202 - required"
            },
            fuelLevel: {
               bsonType: "int",
               minimum: 0,
               maximum: 100,
               description: "Fuel percentage 0-100 - required"
            },
            status: {
               bsonType: "string",
               enum: ["SCHEDULED", "APPROACHING", "LANDING", "LANDED", "AIRBORNE", "EMERGENCY"],
               description: "Current aircraft status - required"
            },
            isEmergency: {
               bsonType: "bool",
               description: "Emergency flag - optional"
            },
            priority: {
               bsonType: "int",
               minimum: 1,
               maximum: 3,
               description: "Priority: 1=Emergency, 2=Low fuel, 3=Normal"
            },
            requestTime: {
               bsonType: "date",
               description: "When landing was requested"
            },
            assignedRunway: {
               bsonType: "string",
               description: "Currently assigned runway ID"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample aircraft
db.aircraft.insertMany([
   {
      aircraftId: "AC001",
      callsign: "BA101", 
      fuelLevel: 75,
      status: "APPROACHING",
      isEmergency: false,
      priority: 3,
      requestTime: new Date(),
      assignedRunway: null,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      aircraftId: "AC002",
      callsign: "UA202",
      fuelLevel: 15,
      status: "AIRBORNE", 
      isEmergency: false,
      priority: 2,
      requestTime: new Date(),
      assignedRunway: null,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      aircraftId: "AC003",
      callsign: "AF303",
      fuelLevel: 5,
      status: "EMERGENCY",
      isEmergency: true,
      priority: 1,
      requestTime: new Date(),
      assignedRunway: "RW01",
      createdAt: new Date(),
      updatedAt: new Date()
   }
])
```

### Indexes:
```javascript
db.aircraft.createIndex({ "aircraftId": 1 }, { unique: true })
db.aircraft.createIndex({ "status": 1 })
db.aircraft.createIndex({ "fuelLevel": 1 })
db.aircraft.createIndex({ "isEmergency": 1 })
db.aircraft.createIndex({ "priority": 1 })
db.aircraft.createIndex({ "assignedRunway": 1 })
```

---

## COLLECTION 2: RUNWAYS
**Purpose:** Store runway information and status (Part 1)

### Schema Definition:
```javascript
db.createCollection("runways", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["runwayId", "isOpen", "status"],
         properties: {
            runwayId: {
               bsonType: "string",
               description: "Runway identifier like RW01, RW02 - required"
            },
            isOpen: {
               bsonType: "bool",
               description: "Whether runway is operational - required"
            },
            status: {
               bsonType: "string",
               enum: ["FREE", "OCCUPIED", "CLOSED", "MAINTENANCE"],
               description: "Current runway status - required"
            },
            currentAircraft: {
               bsonType: ["string", "null"],
               description: "Aircraft currently using runway"
            },
            lastUsed: {
               bsonType: "date",
               description: "Last time runway was used"
            },
            closureReason: {
               bsonType: ["string", "null"],
               description: "Reason for closure if closed"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample runways
db.runways.insertMany([
   {
      runwayId: "RW01",
      isOpen: true,
      status: "OCCUPIED",
      currentAircraft: "AC003",
      lastUsed: new Date(),
      closureReason: null,
      totalUsage: 45,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      runwayId: "RW02", 
      isOpen: true,
      status: "FREE",
      currentAircraft: null,
      lastUsed: new Date(Date.now() - 300000), // 5 minutes ago
      closureReason: null,
      totalUsage: 38,
      createdAt: new Date(),
      updatedAt: new Date()
   }
])
```

### Indexes:
```javascript
db.runways.createIndex({ "runwayId": 1 }, { unique: true })
db.runways.createIndex({ "status": 1 })
db.runways.createIndex({ "isOpen": 1 })
db.runways.createIndex({ "currentAircraft": 1 })
```

---

## COLLECTION 3: FLIGHTS
**Purpose:** Store flight schedule and status information (Part 2)

### Schema Definition:
```javascript
db.createCollection("flights", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["flightId", "flightNumber", "origin", "destination", "status"],
         properties: {
            flightId: {
               bsonType: "string",
               description: "Unique flight identifier - required"
            },
            flightNumber: {
               bsonType: "string",
               description: "Flight number like BA101 - required"
            },
            aircraftId: {
               bsonType: "string",
               description: "Associated aircraft ID"
            },
            origin: {
               bsonType: "string",
               description: "Origin airport code - required"
            },
            destination: {
               bsonType: "string", 
               description: "Destination airport code - required"
            },
            scheduledDeparture: {
               bsonType: "date",
               description: "Scheduled departure time"
            },
            scheduledArrival: {
               bsonType: "date",
               description: "Scheduled arrival time"
            },
            actualDeparture: {
               bsonType: ["date", "null"],
               description: "Actual departure time"
            },
            actualArrival: {
               bsonType: ["date", "null"],
               description: "Actual arrival time"
            },
            status: {
               bsonType: "string",
               enum: ["SCHEDULED", "DELAYED", "CANCELLED", "IN_FLIGHT", "LANDED", "BOARDING"],
               description: "Current flight status - required"
            },
            delayMinutes: {
               bsonType: "int",
               minimum: 0,
               description: "Delay in minutes"
            },
            delayReason: {
               bsonType: ["string", "null"],
               enum: ["WEATHER", "FUEL", "TECHNICAL", "ATC", "RUNWAY", null],
               description: "Reason for delay"
            },
            weatherAlerts: {
               bsonType: "array",
               items: {
                  bsonType: "string"
               },
               description: "Array of weather alert IDs affecting this flight"
            },
            isAffectedByWeather: {
               bsonType: "bool",
               description: "Whether flight is affected by weather"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample flights
db.flights.insertMany([
   {
      flightId: "FL001",
      flightNumber: "BA101",
      aircraftId: "AC001",
      origin: "JFK",
      destination: "LHR",
      scheduledDeparture: new Date(Date.now() + 3600000), // 1 hour from now
      scheduledArrival: new Date(Date.now() + 28800000),  // 8 hours from now
      actualDeparture: null,
      actualArrival: null,
      status: "SCHEDULED",
      delayMinutes: 0,
      delayReason: null,
      weatherAlerts: [],
      isAffectedByWeather: false,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      flightId: "FL002",
      flightNumber: "UA202", 
      aircraftId: "AC002",
      origin: "LAX",
      destination: "JFK",
      scheduledDeparture: new Date(Date.now() - 1800000), // 30 minutes ago
      scheduledArrival: new Date(Date.now() + 16200000),  // 4.5 hours from now
      actualDeparture: new Date(Date.now() - 1800000),
      actualArrival: null,
      status: "IN_FLIGHT",
      delayMinutes: 0,
      delayReason: null,
      weatherAlerts: [],
      isAffectedByWeather: false,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      flightId: "FL003",
      flightNumber: "AF303",
      aircraftId: "AC003", 
      origin: "CDG",
      destination: "JFK",
      scheduledDeparture: new Date(Date.now() - 7200000), // 2 hours ago
      scheduledArrival: new Date(Date.now() + 3600000),   // 1 hour from now
      actualDeparture: new Date(Date.now() - 5400000),    // 1.5 hours ago
      actualArrival: null,
      status: "DELAYED",
      delayMinutes: 45,
      delayReason: "WEATHER",
      weatherAlerts: ["WA001"],
      isAffectedByWeather: true,
      createdAt: new Date(),
      updatedAt: new Date()
   }
])
```

### Indexes:
```javascript
db.flights.createIndex({ "flightId": 1 }, { unique: true })
db.flights.createIndex({ "flightNumber": 1 })
db.flights.createIndex({ "aircraftId": 1 })
db.flights.createIndex({ "status": 1 })
db.flights.createIndex({ "origin": 1 })
db.flights.createIndex({ "destination": 1 })
db.flights.createIndex({ "scheduledDeparture": 1 })
db.flights.createIndex({ "scheduledArrival": 1 })
db.flights.createIndex({ "delayReason": 1 })
db.flights.createIndex({ "isAffectedByWeather": 1 })
```

---

## COLLECTION 4: LANDING_EVENTS
**Purpose:** Log all landing-related events (Part 1)

### Schema Definition:
```javascript
db.createCollection("landing_events", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["eventType", "aircraftId", "timestamp"],
         properties: {
            eventType: {
               bsonType: "string",
               enum: ["LANDING_REQUEST", "RUNWAY_ASSIGNED", "LANDING_STARTED", "LANDING_COMPLETED", "RUNWAY_RELEASED", "EMERGENCY_DECLARED", "PRIORITY_ESCALATED"],
               description: "Type of landing event - required"
            },
            aircraftId: {
               bsonType: "string",
               description: "Aircraft involved in event - required"
            },
            runwayId: {
               bsonType: ["string", "null"],
               description: "Runway involved in event"
            },
            timestamp: {
               bsonType: "date",
               description: "When event occurred - required"
            },
            workerId: {
               bsonType: ["string", "null"],
               description: "Thread/worker that processed event"
            },
            details: {
               bsonType: "object",
               description: "Additional event details"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample landing events
db.landing_events.insertMany([
   {
      eventType: "LANDING_REQUEST",
      aircraftId: "AC001",
      runwayId: null,
      timestamp: new Date(Date.now() - 600000), // 10 minutes ago
      workerId: "LandingWorker-1",
      details: {
         fuelLevel: 75,
         priority: 3,
         isEmergency: false
      }
   },
   {
      eventType: "RUNWAY_ASSIGNED",
      aircraftId: "AC001", 
      runwayId: "RW02",
      timestamp: new Date(Date.now() - 540000), // 9 minutes ago
      workerId: "LandingWorker-1",
      details: {
         queuePosition: 1,
         waitTime: 60
      }
   },
   {
      eventType: "EMERGENCY_DECLARED",
      aircraftId: "AC003",
      runwayId: null,
      timestamp: new Date(Date.now() - 300000), // 5 minutes ago
      workerId: "EmergencyWorker-1",
      details: {
         fuelLevel: 5,
         reason: "FUEL_CRITICAL",
         priorityEscalated: true
      }
   },
   {
      eventType: "RUNWAY_ASSIGNED",
      aircraftId: "AC003",
      runwayId: "RW01", 
      timestamp: new Date(Date.now() - 240000), // 4 minutes ago
      workerId: "EmergencyWorker-1",
      details: {
         emergencyOverride: true,
         previousAircraftCleared: "AC002"
      }
   },
   {
      eventType: "LANDING_COMPLETED",
      aircraftId: "AC003",
      runwayId: "RW01",
      timestamp: new Date(Date.now() - 60000), // 1 minute ago
      workerId: "EmergencyWorker-1",
      details: {
         landingDuration: 180,
         emergencyServicesNotified: true
      }
   }
])
```

### Indexes:
```javascript
db.landing_events.createIndex({ "timestamp": -1 })
db.landing_events.createIndex({ "aircraftId": 1, "timestamp": -1 })
db.landing_events.createIndex({ "eventType": 1 })
db.landing_events.createIndex({ "runwayId": 1, "timestamp": -1 })
db.landing_events.createIndex({ "workerId": 1 })
```

---

## COLLECTION 5: WEATHER_ALERTS
**Purpose:** Store weather alerts and their impacts (Part 2)

### Schema Definition:
```javascript
db.createCollection("weather_alerts", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["alertId", "alertType", "severity", "affectedAirport"],
         properties: {
            alertId: {
               bsonType: "string",
               description: "Unique alert identifier - required"
            },
            alertType: {
               bsonType: "string",
               enum: ["STORM", "FOG", "WIND", "SNOW", "RAIN", "TURBULENCE"],
               description: "Type of weather alert - required"
            },
            severity: {
               bsonType: "string",
               enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"],
               description: "Alert severity level - required"
            },
            affectedAirport: {
               bsonType: "string",
               description: "Airport code affected by weather - required"
            },
            affectedRunways: {
               bsonType: "array",
               items: {
                  bsonType: "string"
               },
               description: "Array of runway IDs affected"
            },
            startTime: {
               bsonType: "date",
               description: "When alert becomes active"
            },
            endTime: {
               bsonType: ["date", "null"],
               description: "Expected end time of alert"
            },
            description: {
               bsonType: "string",
               description: "Human readable description"
            },
            isActive: {
               bsonType: "bool",
               description: "Whether alert is currently active"
            },
            affectedFlights: {
               bsonType: "array",
               items: {
                  bsonType: "string"
               },
               description: "Array of flight IDs affected by this alert"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample weather alerts
db.weather_alerts.insertMany([
   {
      alertId: "WA001",
      alertType: "STORM",
      severity: "HIGH",
      affectedAirport: "JFK",
      affectedRunways: ["RW01", "RW02"],
      startTime: new Date(Date.now() - 1800000), // 30 minutes ago
      endTime: new Date(Date.now() + 3600000),   // 1 hour from now
      description: "Severe thunderstorm with heavy rain and strong winds",
      isActive: true,
      affectedFlights: ["FL003", "FL004", "FL005"],
      createdAt: new Date(Date.now() - 1800000),
      updatedAt: new Date()
   },
   {
      alertId: "WA002",
      alertType: "FOG",
      severity: "MEDIUM", 
      affectedAirport: "LAX",
      affectedRunways: ["RW03"],
      startTime: new Date(Date.now() - 3600000), // 1 hour ago
      endTime: new Date(Date.now() + 1800000),   // 30 minutes from now
      description: "Dense fog reducing visibility to 200 meters",
      isActive: true,
      affectedFlights: ["FL006"],
      createdAt: new Date(Date.now() - 3600000),
      updatedAt: new Date()
   },
   {
      alertId: "WA003",
      alertType: "WIND",
      severity: "LOW",
      affectedAirport: "LHR",
      affectedRunways: [],
      startTime: new Date(Date.now() - 7200000), // 2 hours ago
      endTime: new Date(Date.now() - 1800000),   // 30 minutes ago (ended)
      description: "Strong crosswinds 25-30 knots",
      isActive: false,
      affectedFlights: ["FL001"],
      createdAt: new Date(Date.now() - 7200000),
      updatedAt: new Date(Date.now() - 1800000)
   }
])
```

### Indexes:
```javascript
db.weather_alerts.createIndex({ "alertId": 1 }, { unique: true })
db.weather_alerts.createIndex({ "alertType": 1 })
db.weather_alerts.createIndex({ "severity": 1 })
db.weather_alerts.createIndex({ "affectedAirport": 1 })
db.weather_alerts.createIndex({ "isActive": 1 })
db.weather_alerts.createIndex({ "startTime": 1 })
db.weather_alerts.createIndex({ "endTime": 1 })
```

---

## COLLECTION 6: FUEL_ALERTS
**Purpose:** Store fuel-related alerts and monitoring (Part 2)

### Schema Definition:
```javascript
db.createCollection("fuel_alerts", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["alertId", "aircraftId", "currentFuelLevel", "alertLevel"],
         properties: {
            alertId: {
               bsonType: "string",
               description: "Unique fuel alert identifier - required"
            },
            aircraftId: {
               bsonType: "string",
               description: "Aircraft with fuel issue - required"
            },
            flightId: {
               bsonType: ["string", "null"],
               description: "Associated flight ID"
            },
            currentFuelLevel: {
               bsonType: "int",
               minimum: 0,
               maximum: 100,
               description: "Current fuel percentage - required"
            },
            criticalThreshold: {
               bsonType: "int",
               minimum: 0,
               maximum: 100,
               description: "Critical fuel threshold (default 10%)"
            },
            lowThreshold: {
               bsonType: "int", 
               minimum: 0,
               maximum: 100,
               description: "Low fuel threshold (default 20%)"
            },
            alertTime: {
               bsonType: "date",
               description: "When alert was generated"
            },
            alertLevel: {
               bsonType: "string",
               enum: ["LOW", "CRITICAL", "EMERGENCY"],
               description: "Fuel alert severity - required"
            },
            isResolved: {
               bsonType: "bool",
               description: "Whether alert has been resolved"
            },
            resolution: {
               bsonType: ["string", "null"],
               description: "How alert was resolved"
            },
            escalatedToEmergency: {
               bsonType: "bool",
               description: "Whether alert was escalated to emergency"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample fuel alerts
db.fuel_alerts.insertMany([
   {
      alertId: "FA001",
      aircraftId: "AC002",
      flightId: "FL002",
      currentFuelLevel: 15,
      criticalThreshold: 10,
      lowThreshold: 20,
      alertTime: new Date(Date.now() - 900000), // 15 minutes ago
      alertLevel: "LOW",
      isResolved: false,
      resolution: null,
      escalatedToEmergency: false,
      createdAt: new Date(Date.now() - 900000),
      updatedAt: new Date()
   },
   {
      alertId: "FA002",
      aircraftId: "AC003",
      flightId: "FL003",
      currentFuelLevel: 5,
      criticalThreshold: 10,
      lowThreshold: 20,
      alertTime: new Date(Date.now() - 300000), // 5 minutes ago
      alertLevel: "EMERGENCY",
      isResolved: true,
      resolution: "EMERGENCY_LANDING_COMPLETED",
      escalatedToEmergency: true,
      createdAt: new Date(Date.now() - 300000),
      updatedAt: new Date(Date.now() - 60000)
   },
   {
      alertId: "FA003",
      aircraftId: "AC004",
      flightId: "FL004",
      currentFuelLevel: 8,
      criticalThreshold: 10,
      lowThreshold: 20,
      alertTime: new Date(Date.now() - 600000), // 10 minutes ago
      alertLevel: "CRITICAL",
      isResolved: false,
      resolution: null,
      escalatedToEmergency: true,
      createdAt: new Date(Date.now() - 600000),
      updatedAt: new Date(Date.now() - 120000)
   }
])
```

### Indexes:
```javascript
db.fuel_alerts.createIndex({ "alertId": 1 }, { unique: true })
db.fuel_alerts.createIndex({ "aircraftId": 1 })
db.fuel_alerts.createIndex({ "flightId": 1 })
db.fuel_alerts.createIndex({ "alertLevel": 1 })
db.fuel_alerts.createIndex({ "isResolved": 1 })
db.fuel_alerts.createIndex({ "alertTime": -1 })
db.fuel_alerts.createIndex({ "escalatedToEmergency": 1 })
```

---

## COLLECTION 7: SYSTEM_EVENTS
**Purpose:** Log all system-wide events (Shared between Part 1 & Part 2)

### Schema Definition:
```javascript
db.createCollection("system_events", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["eventType", "timestamp", "source"],
         properties: {
            eventType: {
               bsonType: "string",
               enum: ["SYSTEM_START", "SYSTEM_SHUTDOWN", "THREAD_STARTED", "THREAD_STOPPED", "DATABASE_CONNECTED", "DATABASE_ERROR", "GUI_ACTION", "INTEGRATION_EVENT"],
               description: "Type of system event - required"
            },
            timestamp: {
               bsonType: "date",
               description: "When event occurred - required"
            },
            source: {
               bsonType: "string",
               enum: ["PART1", "PART2", "SHARED", "GUI", "DATABASE"],
               description: "Which part of system generated event - required"
            },
            component: {
               bsonType: ["string", "null"],
               description: "Specific component that generated event"
            },
            message: {
               bsonType: "string",
               description: "Human readable event message"
            },
            details: {
               bsonType: "object",
               description: "Additional event details"
            },
            severity: {
               bsonType: "string",
               enum: ["INFO", "WARNING", "ERROR", "CRITICAL"],
               description: "Event severity level"
            }
         }
      }
   }
})
```

### Sample Documents:
```javascript
// Insert sample system events
db.system_events.insertMany([
   {
      eventType: "SYSTEM_START",
      timestamp: new Date(Date.now() - 3600000), // 1 hour ago
      source: "SHARED",
      component: "AirTrafficSystem",
      message: "Air Traffic Management System started successfully",
      details: {
         version: "1.0.0",
         javaVersion: "11.0.2",
         mongodbVersion: "4.4.0"
      },
      severity: "INFO"
   },
   {
      eventType: "THREAD_STARTED",
      timestamp: new Date(Date.now() - 3540000), // 59 minutes ago
      source: "PART1",
      component: "LandingController",
      message: "Landing worker threads started",
      details: {
         threadCount: 3,
         threadNames: ["LandingWorker-1", "LandingWorker-2", "LandingWorker-3"]
      },
      severity: "INFO"
   },
   {
      eventType: "THREAD_STARTED",
      timestamp: new Date(Date.now() - 3480000), // 58 minutes ago
      source: "PART2",
      component: "FlightScheduler",
      message: "Flight processing threads started",
      details: {
         threadCount: 5,
         monitoringEnabled: true
      },
      severity: "INFO"
   },
   {
      eventType: "GUI_ACTION",
      timestamp: new Date(Date.now() - 600000), // 10 minutes ago
      source: "GUI",
      component: "MainController",
      message: "User triggered emergency scenario",
      details: {
         action: "DECLARE_EMERGENCY",
         aircraftId: "AC003",
         userId: "user1"
      },
      severity: "INFO"
   },
   {
      eventType: "INTEGRATION_EVENT",
      timestamp: new Date(Date.now() - 300000), // 5 minutes ago
      source: "SHARED",
      component: "EventBridge",
      message: "Part 2 notified Part 1 of fuel emergency",
      details: {
         fromPart: "PART2",
         toPart: "PART1",
         eventData: {
            aircraftId: "AC003",
            fuelLevel: 5,
            escalationType: "EMERGENCY"
         }
      },
      severity: "WARNING"
   }
])
```

### Indexes:
```javascript
db.system_events.createIndex({ "timestamp": -1 })
db.system_events.createIndex({ "eventType": 1 })
db.system_events.createIndex({ "source": 1 })
db.system_events.createIndex({ "component": 1 })
db.system_events.createIndex({ "severity": 1 })
```

---

## USEFUL QUERIES FOR DEVELOPMENT

### Part 1 Queries (Runway Management):
```javascript
// Find all aircraft waiting to land
db.aircraft.find({ "status": "APPROACHING" })

// Find available runways
db.runways.find({ "status": "FREE", "isOpen": true })

// Get landing events for specific aircraft
db.landing_events.find({ "aircraftId": "AC001" }).sort({ "timestamp": -1 })

// Find emergency aircraft
db.aircraft.find({ "isEmergency": true })

// Get runway utilization statistics
db.landing_events.aggregate([
   { $match: { "eventType": "RUNWAY_ASSIGNED" } },
   { $group: { _id: "$runwayId", count: { $sum: 1 } } }
])
```

### Part 2 Queries (Flight Operations):
```javascript
// Find delayed flights
db.flights.find({ "delayMinutes": { $gt: 0 } })

// Find flights affected by weather
db.flights.find({ "isAffectedByWeather": true })

// Get active weather alerts
db.weather_alerts.find({ "isActive": true })

// Find aircraft with low fuel
db.aircraft.find({ "fuelLevel": { $lte: 20 } })

// Get fuel alerts by severity
db.fuel_alerts.find({ "alertLevel": "CRITICAL", "isResolved": false })

// Calculate average delay by reason
db.flights.aggregate([
   { $match: { "delayMinutes": { $gt: 0 } } },
   { $group: { _id: "$delayReason", avgDelay: { $avg: "$delayMinutes" } } }
])
```

### Integration Queries (Both Parts):
```javascript
// Find all events for specific aircraft across collections
db.landing_events.find({ "aircraftId": "AC001" })
db.fuel_alerts.find({ "aircraftId": "AC001" })
db.system_events.find({ "details.aircraftId": "AC001" })

// Get system health overview
db.system_events.aggregate([
   { $match: { "timestamp": { $gte: new Date(Date.now() - 3600000) } } },
   { $group: { _id: "$severity", count: { $sum: 1 } } }
])

// Find correlation between fuel and landing events
db.aircraft.aggregate([
   {
      $lookup: {
         from: "fuel_alerts",
         localField: "aircraftId",
         foreignField: "aircraftId",
         as: "fuelAlerts"
      }
   },
   {
      $lookup: {
         from: "landing_events", 
         localField: "aircraftId",
         foreignField: "aircraftId",
         as: "landingEvents"
      }
   }
])
```

---

## DATABASE PERFORMANCE OPTIMIZATION

### Compound Indexes for Complex Queries:
```javascript
// For finding delayed flights by airport and time
db.flights.createIndex({ "origin": 1, "scheduledDeparture": 1, "delayMinutes": 1 })

// For aircraft status and fuel monitoring
db.aircraft.createIndex({ "status": 1, "fuelLevel": 1, "isEmergency": 1 })

// For event timeline queries
db.landing_events.createIndex({ "aircraftId": 1, "timestamp": -1, "eventType": 1 })
db.system_events.createIndex({ "source": 1, "timestamp": -1, "severity": 1 })
```

### Text Search Indexes:
```javascript
// For searching event messages and descriptions
db.system_events.createIndex({ "message": "text", "component": "text" })
db.weather_alerts.createIndex({ "description": "text" })
```

---

## DATA VALIDATION RULES

### Business Logic Constraints:
```javascript
// Ensure fuel level consistency
db.aircraft.find({ "fuelLevel": { $lt: 0 } })  // Should return empty
db.aircraft.find({ "fuelLevel": { $gt: 100 } }) // Should return empty

// Ensure runway assignment consistency  
db.runways.find({ "status": "OCCUPIED", "currentAircraft": null }) // Should return empty

// Ensure flight timing consistency
db.flights.find({ 
   $expr: { $gt: ["$actualDeparture", "$actualArrival"] } 
}) // Should return empty (departure can't be after arrival)
```

---

## BACKUP AND MAINTENANCE

### Regular Maintenance Commands:
```javascript
// Clean up old events (older than 30 days)
db.landing_events.deleteMany({ 
   "timestamp": { $lt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } 
})

db.system_events.deleteMany({ 
   "timestamp": { $lt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } 
})

// Update statistics
db.runways.updateMany({}, { 
   $inc: { "totalUsage": 0 } // Reset or update usage counters
})

// Backup important collections
mongodump --db airTrafficManager --collection aircraft
mongodump --db airTrafficManager --collection flights
mongodump --db airTrafficManager --collection weather_alerts
```

---

## CONNECTION STRING AND SETUP

### Java Connection Configuration:
```javascript
// MongoDB Connection String
mongodb://localhost:27017/airTrafficManager

// Connection Options for Java Driver
MongoClientOptions.builder()
    .connectionsPerHost(10)
    .threadsAllowedToBlockForConnectionMultiplier(5)
    .maxWaitTime(120000)
    .maxConnectionIdleTime(0)
    .maxConnectionLifeTime(0)
    .connectTimeout(10000)
    .socketTimeout(0)
    .socketKeepAlive(true)
    .sslEnabled(false)
    .build()
```

### Database Initialization Script:
```javascript
// Run this script to set up the complete database
use airTrafficManager

// Create all collections with validators (run all collection creation commands above)
// Insert all sample data (run all insertMany commands above)  
// Create all indexes (run all createIndex commands above)

// Verify setup
db.stats()
show collections
db.aircraft.count()
db.flights.count()
db.landing_events.count()
db.weather_alerts.count()
db.fuel_alerts.count()
db.system_events.count()

print("Air Traffic Manager Database Setup Complete!")
```

**This schema provides everything you need for both Part 1 and Part 2 of your project!**