// ============================================================================
// AIR TRAFFIC MANAGER - COMPLETE MONGODB SETUP SCRIPT
// ============================================================================
// Run this script in MongoDB shell or MongoDB Compass to set up everything
// Usage: mongosh < setup_database.js
// ============================================================================

print("🚀 Starting Air Traffic Manager Database Setup...");

// Connect to database
use airTrafficManager;

print("📂 Connected to database: airTrafficManager");

// ============================================================================
// 1. DROP EXISTING COLLECTIONS (Clean Start)
// ============================================================================
print("🗑️  Dropping existing collections...");

db.aircraft.drop();
db.runways.drop();
db.flights.drop();
db.landing_events.drop();
db.weather_alerts.drop();
db.fuel_alerts.drop();
db.system_events.drop();

print("✅ Existing collections dropped");

// ============================================================================
// 2. CREATE COLLECTIONS WITH VALIDATORS
// ============================================================================
print("📋 Creating collections with validators...");

// AIRCRAFT COLLECTION
db.createCollection("aircraft", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["aircraftId", "callsign", "fuelLevel", "status"],
         properties: {
            aircraftId: { bsonType: "string" },
            callsign: { bsonType: "string" },
            fuelLevel: { bsonType: "int", minimum: 0, maximum: 100 },
            status: { 
               bsonType: "string",
               enum: ["SCHEDULED", "APPROACHING", "LANDING", "LANDED", "AIRBORNE", "EMERGENCY"]
            },
            isEmergency: { bsonType: "bool" },
            priority: { bsonType: "int", minimum: 1, maximum: 3 },
            requestTime: { bsonType: "date" },
            assignedRunway: { bsonType: ["string", "null"] }
         }
      }
   }
});

// RUNWAYS COLLECTION
db.createCollection("runways", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["runwayId", "isOpen", "status"],
         properties: {
            runwayId: { bsonType: "string" },
            isOpen: { bsonType: "bool" },
            status: { 
               bsonType: "string",
               enum: ["FREE", "OCCUPIED", "CLOSED", "MAINTENANCE"]
            },
            currentAircraft: { bsonType: ["string", "null"] },
            lastUsed: { bsonType: "date" },
            closureReason: { bsonType: ["string", "null"] }
         }
      }
   }
});

// FLIGHTS COLLECTION
db.createCollection("flights", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["flightId", "flightNumber", "origin", "destination", "status"],
         properties: {
            flightId: { bsonType: "string" },
            flightNumber: { bsonType: "string" },
            aircraftId: { bsonType: "string" },
            origin: { bsonType: "string" },
            destination: { bsonType: "string" },
            scheduledDeparture: { bsonType: "date" },
            scheduledArrival: { bsonType: "date" },
            actualDeparture: { bsonType: ["date", "null"] },
            actualArrival: { bsonType: ["date", "null"] },
            status: {
               bsonType: "string",
               enum: ["SCHEDULED", "DELAYED", "CANCELLED", "IN_FLIGHT", "LANDED", "BOARDING"]
            },
            delayMinutes: { bsonType: "int", minimum: 0 },
            delayReason: {
               bsonType: ["string", "null"],
               enum: ["WEATHER", "FUEL", "TECHNICAL", "ATC", "RUNWAY", null]
            },
            weatherAlerts: { bsonType: "array" },
            isAffectedByWeather: { bsonType: "bool" }
         }
      }
   }
});

// LANDING EVENTS COLLECTION
db.createCollection("landing_events", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["eventType", "aircraftId", "timestamp"],
         properties: {
            eventType: {
               bsonType: "string",
               enum: ["LANDING_REQUEST", "RUNWAY_ASSIGNED", "LANDING_STARTED", "LANDING_COMPLETED", "RUNWAY_RELEASED", "EMERGENCY_DECLARED", "PRIORITY_ESCALATED"]
            },
            aircraftId: { bsonType: "string" },
            runwayId: { bsonType: ["string", "null"] },
            timestamp: { bsonType: "date" },
            workerId: { bsonType: ["string", "null"] },
            details: { bsonType: "object" }
         }
      }
   }
});

// WEATHER ALERTS COLLECTION
db.createCollection("weather_alerts", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["alertId", "alertType", "severity", "affectedAirport"],
         properties: {
            alertId: { bsonType: "string" },
            alertType: {
               bsonType: "string",
               enum: ["STORM", "FOG", "WIND", "SNOW", "RAIN", "TURBULENCE"]
            },
            severity: {
               bsonType: "string",
               enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
            },
            affectedAirport: { bsonType: "string" },
            affectedRunways: { bsonType: "array" },
            startTime: { bsonType: "date" },
            endTime: { bsonType: ["date", "null"] },
            description: { bsonType: "string" },
            isActive: { bsonType: "bool" },
            affectedFlights: { bsonType: "array" }
         }
      }
   }
});

// FUEL ALERTS COLLECTION
db.createCollection("fuel_alerts", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["alertId", "aircraftId", "currentFuelLevel", "alertLevel"],
         properties: {
            alertId: { bsonType: "string" },
            aircraftId: { bsonType: "string" },
            flightId: { bsonType: ["string", "null"] },
            currentFuelLevel: { bsonType: "int", minimum: 0, maximum: 100 },
            criticalThreshold: { bsonType: "int", minimum: 0, maximum: 100 },
            lowThreshold: { bsonType: "int", minimum: 0, maximum: 100 },
            alertTime: { bsonType: "date" },
            alertLevel: {
               bsonType: "string",
               enum: ["LOW", "CRITICAL", "EMERGENCY"]
            },
            isResolved: { bsonType: "bool" },
            resolution: { bsonType: ["string", "null"] },
            escalatedToEmergency: { bsonType: "bool" }
         }
      }
   }
});

// SYSTEM EVENTS COLLECTION
db.createCollection("system_events", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["eventType", "timestamp", "source"],
         properties: {
            eventType: {
               bsonType: "string",
               enum: ["SYSTEM_START", "SYSTEM_SHUTDOWN", "THREAD_STARTED", "THREAD_STOPPED", "DATABASE_CONNECTED", "DATABASE_ERROR", "GUI_ACTION", "INTEGRATION_EVENT"]
            },
            timestamp: { bsonType: "date" },
            source: {
               bsonType: "string",
               enum: ["core", "PART2", "SHARED", "GUI", "DATABASE"]
            },
            component: { bsonType: ["string", "null"] },
            message: { bsonType: "string" },
            details: { bsonType: "object" },
            severity: {
               bsonType: "string",
               enum: ["INFO", "WARNING", "ERROR", "CRITICAL"]
            }
         }
      }
   }
});

print("✅ Collections created with validators");

// ============================================================================
// 3. CREATE INDEXES FOR PERFORMANCE
// ============================================================================
print("🔍 Creating indexes...");

// AIRCRAFT INDEXES
db.aircraft.createIndex({ "aircraftId": 1 }, { unique: true });
db.aircraft.createIndex({ "status": 1 });
db.aircraft.createIndex({ "fuelLevel": 1 });
db.aircraft.createIndex({ "isEmergency": 1 });
db.aircraft.createIndex({ "priority": 1 });
db.aircraft.createIndex({ "assignedRunway": 1 });

// RUNWAYS INDEXES
db.runways.createIndex({ "runwayId": 1 }, { unique: true });
db.runways.createIndex({ "status": 1 });
db.runways.createIndex({ "isOpen": 1 });
db.runways.createIndex({ "currentAircraft": 1 });

// FLIGHTS INDEXES
db.flights.createIndex({ "flightId": 1 }, { unique: true });
db.flights.createIndex({ "flightNumber": 1 });
db.flights.createIndex({ "aircraftId": 1 });
db.flights.createIndex({ "status": 1 });
db.flights.createIndex({ "origin": 1 });
db.flights.createIndex({ "destination": 1 });
db.flights.createIndex({ "scheduledDeparture": 1 });
db.flights.createIndex({ "scheduledArrival": 1 });
db.flights.createIndex({ "delayReason": 1 });
db.flights.createIndex({ "isAffectedByWeather": 1 });

// LANDING EVENTS INDEXES
db.landing_events.createIndex({ "timestamp": -1 });
db.landing_events.createIndex({ "aircraftId": 1, "timestamp": -1 });
db.landing_events.createIndex({ "eventType": 1 });
db.landing_events.createIndex({ "runwayId": 1, "timestamp": -1 });
db.landing_events.createIndex({ "workerId": 1 });

// WEATHER ALERTS INDEXES
db.weather_alerts.createIndex({ "alertId": 1 }, { unique: true });
db.weather_alerts.createIndex({ "alertType": 1 });
db.weather_alerts.createIndex({ "severity": 1 });
db.weather_alerts.createIndex({ "affectedAirport": 1 });
db.weather_alerts.createIndex({ "isActive": 1 });
db.weather_alerts.createIndex({ "startTime": 1 });
db.weather_alerts.createIndex({ "endTime": 1 });

// FUEL ALERTS INDEXES
db.fuel_alerts.createIndex({ "alertId": 1 }, { unique: true });
db.fuel_alerts.createIndex({ "aircraftId": 1 });
db.fuel_alerts.createIndex({ "flightId": 1 });
db.fuel_alerts.createIndex({ "alertLevel": 1 });
db.fuel_alerts.createIndex({ "isResolved": 1 });
db.fuel_alerts.createIndex({ "alertTime": -1 });
db.fuel_alerts.createIndex({ "escalatedToEmergency": 1 });

// SYSTEM EVENTS INDEXES
db.system_events.createIndex({ "timestamp": -1 });
db.system_events.createIndex({ "eventType": 1 });
db.system_events.createIndex({ "source": 1 });
db.system_events.createIndex({ "component": 1 });
db.system_events.createIndex({ "severity": 1 });

// COMPOUND INDEXES FOR COMPLEX QUERIES
db.flights.createIndex({ "origin": 1, "scheduledDeparture": 1, "delayMinutes": 1 });
db.aircraft.createIndex({ "status": 1, "fuelLevel": 1, "isEmergency": 1 });
db.landing_events.createIndex({ "aircraftId": 1, "timestamp": -1, "eventType": 1 });
db.system_events.createIndex({ "source": 1, "timestamp": -1, "severity": 1 });

print("✅ Indexes created");

// ============================================================================
// 4. INSERT SAMPLE DATA
// ============================================================================
print("📊 Inserting sample data...");

// SAMPLE RUNWAYS
db.runways.insertMany([
   {
      runwayId: "RW01",
      isOpen: true,
      status: "FREE",
      currentAircraft: null,
      lastUsed: new Date(Date.now() - 300000),
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
      lastUsed: new Date(Date.now() - 600000),
      closureReason: null,
      totalUsage: 38,
      createdAt: new Date(),
      updatedAt: new Date()
   }
]);

// SAMPLE AIRCRAFT
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
   },
   {
      aircraftId: "AC004",
      callsign: "LH404",
      fuelLevel: 85,
      status: "SCHEDULED",
      isEmergency: false,
      priority: 3,
      requestTime: null,
      assignedRunway: null,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      aircraftId: "AC005",
      callsign: "EK505",
      fuelLevel: 45,
      status: "APPROACHING",
      isEmergency: false,
      priority: 3,
      requestTime: new Date(),
      assignedRunway: null,
      createdAt: new Date(),
      updatedAt: new Date()
   }
]);

// SAMPLE FLIGHTS
db.flights.insertMany([
   {
      flightId: "FL001",
      flightNumber: "BA101",
      aircraftId: "AC001",
      origin: "JFK",
      destination: "LHR",
      scheduledDeparture: new Date(Date.now() + 3600000),
      scheduledArrival: new Date(Date.now() + 28800000),
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
      scheduledDeparture: new Date(Date.now() - 1800000),
      scheduledArrival: new Date(Date.now() + 16200000),
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
      scheduledDeparture: new Date(Date.now() - 7200000),
      scheduledArrival: new Date(Date.now() + 3600000),
      actualDeparture: new Date(Date.now() - 5400000),
      actualArrival: null,
      status: "DELAYED",
      delayMinutes: 45,
      delayReason: "WEATHER",
      weatherAlerts: ["WA001"],
      isAffectedByWeather: true,
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      flightId: "FL004",
      flightNumber: "LH404",
      aircraftId: "AC004",
      origin: "FRA",
      destination: "JFK",
      scheduledDeparture: new Date(Date.now() + 7200000),
      scheduledArrival: new Date(Date.now() + 36000000),
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
      flightId: "FL005",
      flightNumber: "EK505",
      aircraftId: "AC005",
      origin: "DXB",
      destination: "JFK",
      scheduledDeparture: new Date(Date.now() - 3600000),
      scheduledArrival: new Date(Date.now() + 10800000),
      actualDeparture: new Date(Date.now() - 3600000),
      actualArrival: null,
      status: "IN_FLIGHT",
      delayMinutes: 0,
      delayReason: null,
      weatherAlerts: [],
      isAffectedByWeather: false,
      createdAt: new Date(),
      updatedAt: new Date()
   }
]);

// SAMPLE WEATHER ALERTS
db.weather_alerts.insertMany([
   {
      alertId: "WA001",
      alertType: "STORM",
      severity: "HIGH",
      affectedAirport: "JFK",
      affectedRunways: ["RW01", "RW02"],
      startTime: new Date(Date.now() - 1800000),
      endTime: new Date(Date.now() + 3600000),
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
      startTime: new Date(Date.now() - 3600000),
      endTime: new Date(Date.now() + 1800000),
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
      startTime: new Date(Date.now() - 7200000),
      endTime: new Date(Date.now() - 1800000),
      description: "Strong crosswinds 25-30 knots",
      isActive: false,
      affectedFlights: ["FL001"],
      createdAt: new Date(Date.now() - 7200000),
      updatedAt: new Date(Date.now() - 1800000)
   }
]);

// SAMPLE FUEL ALERTS
db.fuel_alerts.insertMany([
   {
      alertId: "FA001",
      aircraftId: "AC002",
      flightId: "FL002",
      currentFuelLevel: 15,
      criticalThreshold: 10,
      lowThreshold: 20,
      alertTime: new Date(Date.now() - 900000),
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
      alertTime: new Date(Date.now() - 300000),
      alertLevel: "EMERGENCY",
      isResolved: true,
      resolution: "EMERGENCY_LANDING_COMPLETED",
      escalatedToEmergency: true,
      createdAt: new Date(Date.now() - 300000),
      updatedAt: new Date(Date.now() - 60000)
   }
]);

// SAMPLE LANDING EVENTS
db.landing_events.insertMany([
   {
      eventType: "LANDING_REQUEST",
      aircraftId: "AC001",
      runwayId: null,
      timestamp: new Date(Date.now() - 600000),
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
      timestamp: new Date(Date.now() - 540000),
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
      timestamp: new Date(Date.now() - 300000),
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
      timestamp: new Date(Date.now() - 240000),
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
      timestamp: new Date(Date.now() - 60000),
      workerId: "EmergencyWorker-1",
      details: {
         landingDuration: 180,
         emergencyServicesNotified: true
      }
   }
]);

// SAMPLE SYSTEM EVENTS
db.system_events.insertMany([
   {
      eventType: "SYSTEM_START",
      timestamp: new Date(Date.now() - 3600000),
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
      timestamp: new Date(Date.now() - 3540000),
      source: "core",
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
      timestamp: new Date(Date.now() - 3480000),
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
      timestamp: new Date(Date.now() - 600000),
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
      timestamp: new Date(Date.now() - 300000),
      source: "SHARED",
      component: "EventBridge",
      message: "Part 2 notified Part 1 of fuel emergency",
      details: {
         fromPart: "PART2",
         toPart: "core",
         eventData: {
            aircraftId: "AC003",
            fuelLevel: 5,
            escalationType: "EMERGENCY"
         }
      },
      severity: "WARNING"
   }
]);

print("✅ Sample data inserted");

// ============================================================================
// 5. VERIFY SETUP
// ============================================================================
print("🔍 Verifying database setup...");

print("📊 Collection Statistics:");
print("- Aircraft: " + db.aircraft.countDocuments());
print("- Runways: " + db.runways.countDocuments());
print("- Flights: " + db.flights.countDocuments());
print("- Landing Events: " + db.landing_events.countDocuments());
print("- Weather Alerts: " + db.weather_alerts.countDocuments());
print("- Fuel Alerts: " + db.fuel_alerts.countDocuments());
print("- System Events: " + db.system_events.countDocuments());

print("🔍 Index Statistics:");
db.runCommand("listCollections").cursor.firstBatch.forEach(function(collection) {
    var collName = collection.name;
    var indexes = db[collName].getIndexes();
    print("- " + collName + ": " + indexes.length + " indexes");
});

// ============================================================================
// 6. TEST QUERIES
// ============================================================================
print("🧪 Testing sample queries...");

print("✅ Active aircraft: " + db.aircraft.countDocuments({"status": {$ne: "LANDED"}}));
print("✅ Available runways: " + db.runways.countDocuments({"status": "FREE", "isOpen": true}));
print("✅ Emergency aircraft: " + db.aircraft.countDocuments({"isEmergency": true}));
print("✅ Delayed flights: " + db.flights.countDocuments({"delayMinutes": {$gt: 0}}));
print("✅ Active weather alerts: " + db.weather_alerts.countDocuments({"isActive": true}));
print("✅ Unresolved fuel alerts: " + db.fuel_alerts.countDocuments({"isResolved": false}));

// ============================================================================
// 7. SETUP COMPLETE
// ============================================================================
print("");
print("🎉 AIR TRAFFIC MANAGER DATABASE SETUP COMPLETE!");
print("");
print("📋 Summary:");
print("- Database: airTrafficManager");
print("- Collections: 7 created with validators");
print("- Indexes: " + db.runCommand("listCollections").cursor.firstBatch.length * 5 + "+ indexes created");
print("- Sample Data: Ready for testing");
print("");
print("🚀 Your database is ready for the Air Traffic Manager application!");
print("📖 Connection String: mongodb://localhost:27017/airTrafficManager");
print("");
print("Next Steps:");
print("1. Add MongoDB Java Driver to your project dependencies");
print("2. Update DatabaseManager.java with connection details");
print("3. Start implementing your DAO classes");
print("4. Test with the sample data provided");
print("");