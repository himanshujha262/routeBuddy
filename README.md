# RoutBuddy 🛺

> **“Smarter Routes. Better Commutes.”**  
> *AI-Powered Smart Mobility Platform for Shared Transportation and Daily Commute*

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL PostGIS](https://img.shields.io/badge/PostgreSQL-16%20%2B%20PostGIS%203.4-blue.svg)](https://postgis.net/)
[![Redis](https://img.shields.io/badge/Redis-7%20Geo%20%2B%20PubSub-red.svg)](https://redis.io/)
[![Python](https://img.shields.io/badge/Python-3.11-yellow.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.111-teal.svg)](https://fastapi.tiangolo.com/)
[![Next.js](https://img.shields.io/badge/Next.js-14%20(App%20Router)-black.svg)](https://nextjs.org/)
[![Flutter](https://img.shields.io/badge/Flutter-3.x%20Dart-02569B.svg)](https://flutter.dev/)

---

## 🌟 Product Vision & Capabilities

**RoutBuddy** is a production-grade, enterprise-scale smart mobility ecosystem designed to solve India's complex first-mile/last-mile transit and daily urban commute challenges.

### 1. Module 1 — Digital Local Shared Transit
* **Fixed Corridors & Designated Stage Stops:** Digitizes semi-organized auto-rickshaws, e-rickshaws, and mini-shuttles along high-density transit corridors.
* **Live GPS Telemetry & Seat Availability Meter:** Real-time location streaming ($\le 1\text{s}$ pings) and dynamic seat inventory counters (e.g., 3/4 seats occupied).
* **Dynamic Cryptographic Boarding Passes:** Time-expiring (60-second sliding window) HMAC-SHA256 digital QR tickets scanned online/offline by drivers to prevent ticket tampering or duplicate boarding.
* **Automated Dual Ledger:** Reconciles cash passenger boardings alongside digital UPI payments into an automated daily driver earnings ledger.

### 2. Module 2 — Daily Commute Partner (Peer-to-Peer)
* **Verified Corporate & Campus Commuters:** Mandatory institutional authentication via work email (`@company.com`) or university credentials (`.edu`).
* **AI Multi-Factor Match Scorer:** Spatial trajectory overlap, schedule tolerance delta, detour distance penalty, and historical trust scores calculated by our FastAPI ML microservice.
* **Double Opt-In Privacy Shield:** Residential addresses are obscured behind a ~500m geohash fuzzy buffer until both commuters mutually accept a ride match request.
* **Mutual Trust & Safety Scoring:** Bayesian-weighted user ratings, integrated 24/7 SOS alert pipeline, and real-time route deviation alarms.

---

## 🏛 High-Level System Architecture

```
                    ┌─────────────────────────┐
                    │  Passenger Mobile App   │ (Flutter / Dart)
                    │  Driver Mobile App      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  Admin & Ops Dashboard  │ (Next.js 14 / TypeScript)
                    └────────────┬────────────┘
                                 │ HTTPS / WSS
                    ┌────────────▼────────────┐
                    │   AWS CloudFront + WAF  │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ Application Load Balancer│
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  Spring Boot 3.3 API    │ (Java 21 Modular Monolith)
                    │  Modular Monolith Core  │
                    └────────────┬────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
 ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
 │ PostgreSQL 16│         │   Redis 7    │         │  AWS S3 /    │
 │   + PostGIS  │         │ (Geo/PubSub) │         │  MinIO Media │
 └──────────────┘         └──────┬───────┘         └──────────────┘
                                 │
                                 ▼ WebSocket STOMP
                                 │
        ┌────────────────────────┴────────────────────────┐
        │                                                 │
        ▼                                                 ▼
 ┌──────────────────────────────────────────────────────────────┐
 │               FastAPI AI / ML Microservice                   │
 │                                                              │
 │   ┌─────────────────┐ ┌─────────────────┐ ┌──────────────┐   │
 │   │ Demand Forecast │ │Occupancy Predict│ │AI Partner Mch│   │
 │   └─────────────────┘ └─────────────────┘ └──────────────┘   │
 └──────────────────────────────────────────────────────────────┘
```

---

## 📦 Project Structure

```
pbl3/
├── backend/                             # Spring Boot 3.3 Modular Monolith (Java 21)
│   ├── pom.xml                          # Master Multi-Module POM
│   ├── checkstyle.xml                   # Java Code Quality & Linting Rules
│   ├── .env.example                     # Backend Environment Variables
│   ├── routbuddy-common/                # Domain kernel, enums, exceptions, PostGIS crypto
│   ├── routbuddy-auth/                  # JWT rotation, Phone OTP, Spring Security RBAC
│   ├── routbuddy-users/                 # User profiles, corporate KYC, trust scoring
│   ├── routbuddy-drivers/               # Driver onboarding, KYC, daily earnings ledger
│   ├── routbuddy-vehicles/              # Vehicle registry, seating layouts, fuel types
│   ├── routbuddy-routes/                # Corridors, designated stops, stage fares, PostGIS geometries
│   ├── routbuddy-trips/                 # Trip scheduling, live state machine, telemetry
│   ├── routbuddy-bookings/              # Concurrency-safe seat reservations, dynamic QR
│   ├── routbuddy-payments/              # UPI/Razorpay integration, driver payout settlements
│   ├── routbuddy-subscriptions/         # Weekly, monthly, student/corporate transit passes
│   ├── routbuddy-matching/              # Daily commute partner matching, mutual handshake
│   ├── routbuddy-locations/             # Real-time GPS ingestion, Redis GEO, WebSocket STOMP
│   ├── routbuddy-notifications/         # Push notifications (FCM), transactional SMS
│   ├── routbuddy-ratings/               # Reviews, ratings, trust score recalculation
│   ├── routbuddy-safety/                # Emergency SOS alerts, route deviation detection
│   ├── routbuddy-complaints/            # Grievance ticketing & dispute resolution
│   ├── routbuddy-analytics/             # Route demand heatmaps, fleet occupancy trends
│   ├── routbuddy-admin/                 # Backoffice APIs, KYC approval queue
│   └── routbuddy-bootstrap/             # Unified Spring Boot runner & Flyway Migrations (V1-V4)
│
├── ai-service/                          # AI/ML Microservice (Python 3.11 / FastAPI)
│   ├── app/
│   │   ├── main.py                      # FastAPI application entry point
│   │   ├── core/                        # Service settings & configuration
│   │   └── models/
│   │       ├── partner_matcher.py       # Multi-factor commute similarity scoring
│   │       ├── demand_predictor.py      # Spatial-temporal transit demand forecaster
│   │       └── occupancy_predictor.py   # Stop-by-stop occupancy inference
│   ├── pyproject.toml                   # Python linting & formatting (Black, isort)
│   ├── requirements.txt                 # Dependencies (FastAPI, Uvicorn, Scikit-Learn)
│   ├── .env.example                     # AI Service Environment Variables
│   └── Dockerfile
│
├── frontend-admin/                      # Admin & Operations Dashboard (Next.js 14 / TypeScript)
│   ├── src/app/                         # App Router (Dashboard, Drivers KYC, Corridors, SOS)
│   ├── src/components/                  # LiveFleetMap, StatCard, Sidebar, Navbar
│   ├── .eslintrc.json                   # ESLint code quality configuration
│   ├── .prettierrc                      # Prettier code formatting rules
│   ├── package.json
│   ├── .env.example                     # Next.js Environment Variables
│   └── Dockerfile
│
├── mobile/                              # Mobile Client Applications (Flutter / Dart)
│   ├── routbuddy_passenger/             # Passenger & Commuter App (Search, QR Pass, Partner Matching, SOS)
│   └── routbuddy_driver/                # Driver App (Trip dispatch, Telemetry, Camera QR Scanner, Earnings)
│
├── infrastructure/                      # DevOps & Cloud Infrastructure
│   ├── docker/                          # docker-compose.yml (PostgreSQL+PostGIS, Redis, Backend, AI, Admin)
│   │   ├── .env.example
│   │   └── init-db.sql                  # Automated PostGIS extension initializer
│   ├── terraform/                       # AWS IaC (VPC, RDS PostGIS, ElastiCache, S3, WAF, CloudFront)
│   └── .github/workflows/               # GitHub Actions CI/CD pipeline
│
├── .editorconfig                        # Universal IDE indentation & whitespace rules
├── .gitattributes                       # Git line ending normalization (LF/CRLF)
├── .gitignore                           # Comprehensive root gitignore
├── run_dev.bat                          # One-Click Windows Development Launcher
└── run_dev.ps1                          # PowerShell Development Launcher
```

---

## 🚀 Quickstart & Development Setup

### 1. Prerequisites
* **Java 21 LTS** & **Apache Maven 3.9+**
* **Python 3.11+**
* **Node.js 18+** & **npm 9+**
* **Docker & Docker Compose**

### 2. Step-by-Step Local Execution

#### Step 2.1: Start Infrastructure Containers (PostgreSQL PostGIS + Redis)
```bash
cd infrastructure/docker
docker-compose up -d postgres redis
```
* **PostgreSQL (PostGIS)**: `localhost:5432` (`routbuddy_db`, user: `postgres`, password: `postgrespassword`)
* **Redis 7**: `localhost:6379`

#### Step 2.2: Launch Python AI / ML Microservice
```bash
cd ai-service
py -3.11 -m pip install -r requirements.txt
py -3.11 -m uvicorn app.main:app --reload --port 8000
```
* **Interactive OpenAPI Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)
* **Health Check**: [http://localhost:8000/health](http://localhost:8000/health)

#### Step 2.3: Launch Spring Boot Modular Backend
```bash
cd backend
mvn clean spring-boot:run -pl routbuddy-bootstrap
```
* **Swagger UI Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

#### Step 2.4: Launch Next.js Admin Operations Dashboard
```bash
cd frontend-admin
npm install
npm run dev
```
* **Admin Portal**: [http://localhost:3000](http://localhost:3000)

#### Step 2.5: Launch Flutter Mobile Clients
```bash
# Passenger & Daily Commuter App
cd mobile/routbuddy_passenger
flutter pub get
flutter run

# Driver & Telemetry App
cd mobile/routbuddy_driver
flutter pub get
flutter run
```

---

## 🛡 Security & Privacy Shield

- **Double Opt-In Commuter Handshake**: Exact residential coordinates and personal phone numbers are protected behind ~500m geohash privacy buffers. Data is only decrypted and shared once both commuters mutually accept a ride request.
- **Dynamic Boarding Passes**: QR codes are cryptographically signed using `HMAC-SHA256(booking_id, trip_id, passenger_id, timestampFloor(epoch, 60))` to prevent counterfeit tickets and support offline driver verification.
- **24/7 Safety Command Center**: Integrated SOS triggers broadcast instant SMS alerts to registered emergency contacts and push live coordinates to the Operations Dashboard.
