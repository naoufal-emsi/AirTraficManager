# AIR TRAFFIC MANAGER - PROJECT GUIDE
## What Are We Actually Building?

**Think of this like a simple airport simulation game, but focused on Java programming concepts.**

---
  

## THE BIG PICTURE - What's This Project About?

Imagine you're managing a small airport. You have:
- **2 runways** where planes can land or take off
- **Planes** that want to land (they're like customers waiting in line)
- **Air traffic controllers** (like cashiers) who help planes land safely
- **Weather problems** that can delay flights
- **Fuel issues** where planes might run low on fuel

**Your job:** Build a Java program that simulates this airport, but the twist is you must use specific Java concepts we learned in class:
- **Threads** (multiple things happening at the same time)
- **Semaphores** (controlling access to limited resources like runways)
- **Streams & Lambdas** (processing lists of data efficiently)
- **MongoDB** (storing all the data)
- **Simple GUI** (buttons to trigger different scenarios)

---

## WHY THIS PROJECT EXISTS

**Real reason:** This isn't about becoming a pilot or air traffic controller. It's about learning Java concurrency (multiple threads working together safely).

**The airport theme** is just a fun way to practice:
- Managing shared resources (runways = limited resources)
- Handling priorities (emergency planes go first)
- Processing data efficiently (finding delayed flights)
- Coordinating multiple workers (threads) safely

**Think of it like:** A restaurant simulation where multiple chefs (threads) share limited stoves (semaphores), but we're using airports because it's more interesting.

---

## WHAT EXACTLY WILL YOUR PROGRAM DO?

### The Simple Version:
1. **Planes request to land** → Your program puts them in a queue
2. **Air traffic controllers** (threads) process the queue
3. **Only 2 planes can use runways at once** (semaphore with 2 permits)
4. **Emergency planes cut the line** (priority handling)
5. **Weather can delay flights** (streams filter affected flights)
6. **Low fuel planes get priority** (monitoring and alerts)
7. **Everything gets saved to MongoDB** (persistence)
8. **Simple GUI shows what's happening** (JavaFX with buttons)

### The Scenarios You'll Build:
1. **"Multiple Planes Want to Land"** - Test your semaphore and queue system
2. **"Emergency Plane!"** - Test priority handling and thread interruption
3. **"Bad Weather"** - Test streams/lambdas filtering affected flights
4. **"Plane Running Low on Fuel"** - Test monitoring threads and alerts
5. **"Runway Closed"** - Test resource management when things break

---

## THE TWO PARTS EXPLAINED SIMPLY

### Part 1: Runway & Landing Management (Your Partner)
**What they build:**
- **Runway system** with semaphores (only 2 planes land at once)
- **Landing queue** where planes wait their turn
- **Emergency system** that lets urgent planes skip the line
- **Threads** that act like air traffic controllers processing landings

**Key Java concepts they practice:**
- Semaphores (controlling runway access)
- Thread pools (multiple controllers working)
- BlockingQueues (planes waiting in line)
- Thread safety (no crashes when multiple threads work together)

### Part 2: Flight Operations & Weather (You)
**What you build:**
- **Flight scheduling** system that manages when planes should arrive/depart
- **Weather system** that can delay flights
- **Fuel monitoring** that watches plane fuel levels
- **The GUI** that lets users trigger different scenarios

**Key Java concepts you practice:**
- Streams & Lambdas (filtering flights, calculating statistics)
- More complex threading (monitoring, async operations)
- Data processing (finding delayed flights, weather impacts)
- GUI development (JavaFX)

---

## WHAT DOES "NO SYNCHRONIZED" MEAN?

**Simple explanation:** In Java, when multiple threads share data, you usually use `synchronized` to prevent crashes. But in this project, you're forbidden from using it.

**Instead, you must use:**
- `Semaphore` - for controlling access to runways
- `ConcurrentHashMap` - for thread-safe data storage
- `BlockingQueue` - for thread-safe queues
- `AtomicInteger` - for thread-safe counters

**Why?** To force you to learn these more advanced concurrency tools.

---

## THE MONGODB PART - WHAT DATA DO WE STORE?

**Think of MongoDB like a filing cabinet with different drawers:**

### Drawer 1: Aircraft Information
```
Aircraft AC001:
- Name: "British Airways 101"
- Fuel Level: 45%
- Status: "Approaching airport"
- Emergency: No
```

### Drawer 2: Flight Schedules
```
Flight FL001:
- From: New York
- To: London
- Scheduled: 2:30 PM
- Status: "Delayed by weather"
- Delay: 30 minutes
```

### Drawer 3: Events Log (Everything that happens)
```
Event 1: "Aircraft AC001 requested landing at 2:15 PM"
Event 2: "Runway RW01 assigned to AC001 at 2:16 PM"
Event 3: "AC001 landed successfully at 2:20 PM"
```

**Why MongoDB?** Because it's easy to store this kind of data, and you need to practice database integration.

---

## THE GUI - WHAT WILL USERS SEE?

**Simple window with:**
- **Table showing all planes** (their fuel, status, etc.)
- **Table showing all flights** (delays, cancellations, etc.)
- **Buttons to trigger scenarios:**
  - "Add New Plane" → Creates a plane that wants to land
  - "Create Emergency" → Makes a plane declare emergency
  - "Bad Weather Alert" → Delays flights due to weather
  - "Low Fuel Warning" → Simulates a plane running low on fuel
  - "Close Runway" → Simulates runway maintenance

**The tables update automatically** as your threads work in the background.

---

## STEP-BY-STEP: WHAT HAPPENS WHEN YOU RUN THE PROGRAM

1. **Program starts** → Connects to MongoDB, creates 2 runways, starts background threads
2. **User clicks "Add New Plane"** → Creates aircraft "AC001" wanting to land
3. **Your code adds AC001 to landing queue** → Plane waits its turn
4. **Landing controller thread picks up AC001** → Tries to get a runway
5. **Semaphore gives permission** → AC001 gets runway RW01
6. **Thread simulates landing** → Sleeps for 3 seconds (pretend landing time)
7. **Runway released** → RW01 becomes available for next plane
8. **Event logged to MongoDB** → "AC001 landed at 2:20 PM"
9. **GUI updates** → Shows AC001 status changed to "Landed"

**If emergency happens:** Emergency plane cuts the line and lands immediately.
**If weather hits:** Streams find all affected flights and add delays.
**If fuel low:** Monitoring thread detects it and escalates to emergency.

---

## SUCCESS CRITERIA - HOW DO YOU KNOW IT WORKS?

### Technical Requirements:
- ✅ **No `synchronized` keyword anywhere in your code**
- ✅ **Semaphores control runway access** (max 2 planes at once)
- ✅ **Multiple threads run without crashing** (thread safety)
- ✅ **Streams/lambdas process flight data** (filtering, grouping, statistics)
- ✅ **MongoDB stores and retrieves data** (persistence)
- ✅ **GUI responds to user clicks** (JavaFX working)

### Functional Requirements:
- ✅ **Planes can request landing and actually land**
- ✅ **Emergency planes get priority**
- ✅ **Weather delays affect multiple flights**
- ✅ **Low fuel triggers alerts and priority**
- ✅ **Runway closure redirects traffic**

### Demo Requirements:
- ✅ **You can click buttons and see things happen**
- ✅ **Tables update with real data**
- ✅ **Multiple scenarios work without crashes**
- ✅ **Data persists between program runs** (MongoDB)

---

## WHAT YOU'RE ACTUALLY LEARNING

**This project teaches you:**
1. **Thread coordination** - Multiple workers sharing limited resources safely
2. **Resource management** - Semaphores controlling access to runways
3. **Data processing** - Streams/lambdas for efficient data handling
4. **Event-driven programming** - Everything happens through events
5. **Database integration** - Storing and retrieving data efficiently
6. **GUI development** - Creating responsive user interfaces
7. **System design** - Breaking complex problems into manageable parts

**These are real-world skills** used in:
- Web servers (handling multiple user requests)
- Game development (multiple players, limited resources)
- Financial systems (processing transactions safely)
- Any system where multiple things happen simultaneously

---

## COMMON QUESTIONS

**Q: Do I need to know about real airports?**
A: No! The airport theme is just for context. Focus on the Java concepts.

**Q: What if I don't understand aviation terms?**
A: Ignore them. "Aircraft" = object with properties. "Runway" = limited resource. "Landing" = using a resource.

**Q: How complex should the GUI be?**
A: Very simple. Just tables and buttons. No fancy graphics needed.

**Q: What if my threads crash?**
A: That's the main challenge! Use proper concurrent collections and avoid shared mutable state.

**Q: How do I test if it works?**
A: Click buttons rapidly, try to break it. If it handles multiple operations without crashing, you're good.

---

## FINAL ADVICE

**Start simple:**
1. Get basic threads working
2. Add semaphore for runway control
3. Add MongoDB storage
4. Add GUI
5. Add complex scenarios

**Focus on Java concepts, not aviation realism.**
**If it works without crashing under load, you've succeeded!**

---

## DETAILED TECHNICAL REQUIREMENTS

### Part 1 (Your Partner) - Runway Management:
- Create `Semaphore runwaySemaphore = new Semaphore(2)` for 2 runways
- Use `ExecutorService` with 3-5 threads for landing controllers
- Use `BlockingQueue<Aircraft>` for landing requests
- Use `PriorityBlockingQueue<Aircraft>` for emergency requests
- Create classes: `Aircraft`, `Runway`, `LandingController`, `RunwayManager`
- MongoDB collections: `aircraft`, `runways`, `landing_events`

### Part 2 (You) - Flight Operations:
- Use streams extensively: `flights.stream().filter().collect()`
- Use lambdas for data processing: `flights.forEach(flight -> ...)`
- Create weather monitoring with `ScheduledExecutorService`
- Create fuel monitoring threads
- Build JavaFX GUI with tables and buttons
- Create classes: `Flight`, `WeatherAlert`, `FlightScheduler`, `WeatherService`
- MongoDB collections: `flights`, `weather_alerts`, `fuel_alerts`

### Shared Components:
- `DatabaseManager` for MongoDB connection
- `Event` class for logging all activities
- Integration points between both parts

**Remember: This is about Java programming skills, not aviation knowledge!**