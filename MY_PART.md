# MY PART - Aircraft & Emergency System

## Files I Own

### Models
- Aircraft.java - Core aircraft model with state and physics
- AircraftType.java - Enum: B737, A320, B777, A330, B787
- FlightPlan.java - Flight route and fuel calculations

### Workers (3 Threads)
- AircraftUpdateWorker.java - Thread 1: Position/fuel updates every 2s
- FuelMonitoringWorker.java - Thread 2: Fuel crash detection every 2s
- EmergencyHandlerWorker.java - Thread 3: Emergency processing every 2s

### Controllers
- EmergencyController.java - Emergency declaration logic

### GUI Components
- Emergency buttons panel (6 buttons)
- Aircraft table display
- generateAircraft() method
- triggerEmergency() method

## What I Build

### Aircraft.java
Fields: callsign, aircraftType, fuel, speed, distance, origin, destination, status, emergency, priority, emergencyTimestamp, assignedRunway

Methods:
- updatePosition(timeStep) - distance -= speed * timeStep
- consumeFuel(amount) - fuel -= amount
- isCriticalFuel() - check if fuel low
- declareEmergency(type, priority) - set emergency state

Status transitions:
- distance > 5000m → APPROACHING
- distance ≤ 5000m → READY_TO_LAND
- distance ≤ 50m → LANDED

### AircraftType.java
B737(150.0, 6000.0, 2.5) - speed, fuelCapacity, burnRate
A320(145.0, 6400.0, 2.3)
B777(180.0, 11000.0, 4.0)
A330(175.0, 10500.0, 3.8)
B787(185.0, 12500.0, 3.5)

### AircraftUpdateWorker.java
Every 2 seconds:
- Get all aircraft from DB
- Update position: distance -= speed * 2.0
- Update fuel: fuel -= burnRate * 2.0
- Update status based on distance
- Save to DB

### FuelMonitoringWorker.java
Every 2 seconds:
- Get all aircraft from DB
- If fuel <= 0 and not LANDED: CRASH
- Set status = CRASHED
- Set emergency = FUEL_CRITICAL
- Log to emergency_events

### EmergencyHandlerWorker.java
Every 2 seconds:
- Get all aircraft from DB
- If emergency != NONE and not logged
- Save to emergency_events collection
- Mark as logged

### EmergencyController.java
Priority mapping:
- FIRE → 1
- MEDICAL → 5
- SECURITY → 8
- FUEL_CRITICAL → 10
- WEATHER_STORM → 30
- FUEL_LOW → 50
- NONE → 100

Method: declareEmergency(callsign, type)
- Get aircraft from DB
- Set priority based on type
- Set emergencyTimestamp
- Update DB

## Database Methods I Use

Write:
- generateAndInsertRealisticFlight()
- insertActiveAircraft(aircraft)
- updateActiveAircraft(aircraft)
- saveEmergencyEvent(callsign, details)

Read:
- getAllActiveAircraft()
- getAircraftByCallsign(callsign)

## My Data Flow

1. User clicks "Generate Aircraft"
2. generateAndInsertRealisticFlight() → DB
3. AircraftUpdateWorker updates position/fuel → DB
4. When distance ≤ 5000m → status = READY_TO_LAND → DB
5. Partner assigns runway → DB updates assignedRunway
6. AircraftUpdateWorker continues moving
7. When distance ≤ 50m → status = LANDED → DB
8. Partner releases runway

Emergency Flow:
1. User selects aircraft + clicks emergency button
2. triggerEmergency() → updates priority & type → DB
3. EmergencyHandlerWorker logs → DB (emergency_events)
4. Partner reads priority → assigns runway first

## Testing Checklist

- Generate aircraft → verify in DB
- Position updates every 2s → distance decreases
- Fuel updates every 2s → fuel decreases
- Status APPROACHING → READY_TO_LAND at 5000m
- Status READY_TO_LAND → LANDED at 50m
- Emergency declaration → priority updated
- Fuel = 0 → CRASHED status
- Emergency logged to emergency_events

## Critical Rules

- NEVER modify runway status
- NEVER assign runways
- ONLY write to: active_aircraft, emergency_events
- Update every 2 seconds exactly
- Priority: Lower = Higher (1 is highest)
- Status flow: APPROACHING → READY_TO_LAND → LANDING → LANDED
- Crash if: fuel <= 0 && status != LANDED

## Integration with Partner

I provide:
- Aircraft with status READY_TO_LAND
- Emergency priority for sorting

Partner provides:
- Runway assignment (assignedRunway field)
- Runway release when LANDED

Communication: 100% through MongoDB
