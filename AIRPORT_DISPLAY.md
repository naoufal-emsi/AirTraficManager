# 🛫 Airport Display Board - Real FIDS Interface

## What This Is
A **realistic Flight Information Display System (FIDS)** like you see at real airports - black background with colored text showing live arrivals and departures.

## Features

### 🛬 ARRIVALS BOARD (Left Side - Green)
Shows all aircraft currently landing:
- **TIME**: When aircraft requested landing
- **FLIGHT**: Aircraft callsign (FL123, FL456, etc.)
- **CITY**: Origin city
- **GATE/RWY**: Assigned runway (RW01, RW02) or "HOLDING"
- **STATUS**: APPROACHING → LANDING → LANDED
- **REMARKS**: Fuel warnings (⛽ CRITICAL, ⛽ LOW FUEL) + ⚠ PRIORITY for emergencies

### 🛫 DEPARTURES BOARD (Right Side - Gold)
Shows scheduled departing flights:
- **TIME**: Scheduled departure time
- **FLIGHT**: Flight number (AA123, AA456, etc.)
- **CITY**: Destination city
- **GATE/RWY**: Gate assignment
- **STATUS**: ON TIME / DELAYED / DEPARTED
- **REMARKS**: Delay information (+30min, etc.)

### 🛬 RUNWAY STATUS (Bottom)
Real-time runway availability:
- 🟢 AVAILABLE - Runway free
- 🔴 OCCUPIED - Aircraft landing
- Shows which aircraft is using which runway
- Statistics: Total aircraft, landing count, holding count

### ⏰ LIVE UPDATES
- Clock updates every second
- Flight boards refresh every second
- Weather alerts shown in header
- Auto-generates new flights every 15 seconds

## Color Coding

### Status Colors:
- 🟢 **GREEN**: ON TIME, LANDED, BOARDING, NORMAL
- 🟡 **YELLOW/GOLD**: DELAYED, FINAL CALL, APPROACHING
- 🔴 **RED**: CANCELLED, EMERGENCY, CRITICAL
- 🔵 **BLUE**: LANDING, IN PROGRESS

### Display Style:
- **Black background** (like real airport displays)
- **Large bold fonts** (easy to read from distance)
- **Color-coded information** (quick visual scanning)
- **Monospace fonts** for data (professional look)

## How to Run

```bash
# Compile
mvn clean compile

# Run the airport display board
mvn exec:java -Dexec.mainClass="com.atc.gui.AirportDisplayBoard"
```

## Auto-Generation
The system automatically generates:
- **Arrivals**: New aircraft every 15 seconds (70% chance)
- **Departures**: New flights every 15 seconds (50% chance)
- **Realistic data**: Random cities, flight numbers, fuel levels

## What You'll See

```
┌─────────────────────────────────────────────────────────────────┐
│  ✈ INTERNATIONAL AIRPORT - FLIGHT INFORMATION                   │
│  🕐 14:23:45          ⛅ CLEAR - ALL OPERATIONS NORMAL          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  🛬 ARRIVALS              │  🛫 DEPARTURES                       │
│  ─────────────────────    │  ─────────────────────              │
│  TIME  FLIGHT  CITY       │  TIME  FLIGHT  CITY                 │
│  14:20 FL456   LONDON     │  14:30 AA123   TOKYO                │
│  14:21 FL789   PARIS      │  14:35 AA456   DUBAI                │
│  14:22 FL234   NEW YORK   │  14:40 AA789   SINGAPORE            │
│                           │                                      │
│  STATUS: LANDING          │  STATUS: ON TIME                    │
│  RUNWAY: RW01             │  GATE: GATE 12                      │
│  REMARKS: ⛽ LOW FUEL     │  REMARKS: BOARDING                  │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│  🛬 RUNWAY STATUS & ACTIVE AIRCRAFT                             │
│  RW01     🟢 AVAILABLE                                          │
│  RW02     🔴 OCCUPIED - Aircraft: AC123                         │
│  ─────────────────────────────────────────────────────────────  │
│  📊 Total Aircraft: 5  |  Landing: 2  |  Holding: 3            │
└─────────────────────────────────────────────────────────────────┘
```

## Realistic Features

✅ **Live clock** - Updates every second  
✅ **Real-time status** - See aircraft move through landing phases  
✅ **Fuel monitoring** - Critical/low fuel warnings  
✅ **Emergency priority** - ⚠ PRIORITY flag for emergencies  
✅ **Weather alerts** - Shown in header when active  
✅ **Runway occupancy** - See which runway is in use  
✅ **Auto-population** - Continuous stream of flights  
✅ **Color-coded status** - Quick visual identification  

## Differences from Old GUI

| Old GUI | New Airport Display |
|---------|-------------------|
| Tables with buttons | Pure information display |
| Manual flight creation | Auto-generates flights |
| Small updates | Full-screen boards |
| Generic look | Airport-authentic style |
| Static appearance | Dynamic color coding |

## Perfect For
- ✈️ Demonstrations
- 📊 Monitoring operations
- 🎓 Presentations
- 👀 Visual appeal
- 🏢 Professional display

**This is what you see at real airports!** 🛫
