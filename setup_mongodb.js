// MongoDB Setup Script for Air Traffic Control System

use airTrafficControl;

// Create collections
db.createCollection("aircraft");
db.createCollection("runway_events");
db.createCollection("emergency_events");
db.createCollection("weather_events");

// Create indexes for better performance
db.aircraft.createIndex({ "callsign": 1 });
db.aircraft.createIndex({ "timestamp": -1 });
db.runway_events.createIndex({ "runwayId": 1 });
db.runway_events.createIndex({ "timestamp": -1 });
db.emergency_events.createIndex({ "callsign": 1 });
db.emergency_events.createIndex({ "emergencyType": 1 });
db.emergency_events.createIndex({ "timestamp": -1 });
db.weather_events.createIndex({ "timestamp": -1 });

print("✓ Database 'airTrafficControl' created");
print("✓ Collections created: aircraft, runway_events, emergency_events, weather_events");
print("✓ Indexes created for performance");
print("");
print("Database setup complete!");
