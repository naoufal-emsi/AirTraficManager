# Cahier des Charges - Air Traffic Control System

## 1. Présentation du Projet

### 1.1 Objectif
Développer un système de contrôle du trafic aérien multi-threadé avec gestion des urgences, simulation physique des aéronefs, et persistance MongoDB.

### 1.2 Architecture Technique
- **Langage**: Java 25
- **Base de données**: MongoDB (localhost:27017)
- **Pattern**: Multi-threading avec 8 threads concurrents
- **Interface**: Swing GUI
- **Build**: Maven

## 2. Spécifications Fonctionnelles

### 2.1 Gestion des Aéronefs
- Génération d'aéronefs avec plans de vol réalistes
- Types supportés: B737, A320, B777, A330, B787
- Attributs: callsign, type, carburant, vitesse, distance, origine, destination
- États: APPROACHING → READY_TO_LAND → LANDING → LANDED

### 2.2 Système d'Urgences
- 6 types d'urgences avec priorités:
  - FIRE (priorité 1) - Urgence maximale
  - MEDICAL (priorité 5) - Urgence médicale
  - SECURITY (priorité 8) - Menace sécurité
  - FUEL_CRITICAL (priorité 10) - Carburant critique
  - WEATHER_STORM (priorité 30) - Conditions météo
  - FUEL_LOW (priorité 50) - Carburant bas
- Priorité normale: 100
- Règle: Plus le nombre est bas, plus la priorité est haute

### 2.3 Gestion des Pistes
- Création dynamique de pistes (RWY-09L, RWY-27R, etc.)
- États: AVAILABLE, OCCUPIED
- Attribution basée sur file de priorité (urgence → timestamp)
- Libération automatique après atterrissage

### 2.4 Simulation Physique
- Mise à jour position toutes les 2 secondes
- Calcul: distance -= vitesse × temps
- Consommation carburant réaliste
- Crash automatique si carburant = 0

## 3. Architecture Multi-Thread

| Thread | Fréquence | Fonction |
|--------|-----------|----------|
| AircraftUpdateWorker | 2s | Mise à jour position/carburant |
| FuelMonitoringWorker | 2s | Surveillance carburant |
| EmergencyHandlerWorker | 2s | Traitement urgences |
| RunwayManagerWorker (×4) | 2s | Attribution pistes |
| WeatherWorker | 30s | Simulation météo |

## 4. Collections MongoDB

### active_aircraft
- callsign, aircraftType, fuel, speed, distance
- origin, destination, status, emergency, priority
- emergencyTimestamp, assignedRunway, timestamp

### runways
- runwayId, status, currentAircraft, thresholdPosition

### emergency_events
- callsign, details, timestamp

### runway_events
- event, timestamp

### weather_events
- event, timestamp

## 5. Critères de Succès

- Système démarre sans erreur
- Aéronefs générés et suivis correctement
- Urgences traitées par ordre de priorité
- Pistes attribuées et libérées automatiquement
- Interface responsive et informative
- Aucun deadlock ou race condition
- Données persistées correctement dans MongoDB
- Support 20+ aéronefs simultanés
