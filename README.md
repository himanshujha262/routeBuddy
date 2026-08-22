# RoutBuddy 🛺

> **“Smarter Routes. Better Commutes.”**  
> *AI-Powered Smart Mobility Platform for Shared Transportation and Daily Commute*

---

## 🌟 Product Vision

**RoutBuddy** is a production-grade smart mobility platform designed to digitize local shared transportation in India (auto-rickshaws, e-rickshaws, shared shuttles) and enable verified, recurring daily commute partnerships (carpooling, bikepooling, and corridor transit).

The platform addresses two massive transit use cases:
1. **Module 1 — Digital Local Shared Auto**: Digitizes informal and semi-organized shared 3-wheelers and e-rickshaws along fixed corridors, providing live GPS tracking, seat occupancy meters, dynamic HMAC-SHA256 boarding QR passes, and automated fare ledgers.
2. **Module 2 — Daily Commute Partner**: Connects verified corporate and university commuters with compatible schedules and routes using an AI multi-factor matching engine (spatiotemporal overlap, org email verification, gender preferences, and double opt-in privacy shielding).

---

## 🏛 High-Level Architecture

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
│   ├── pom.xml                          # Parent Maven POM
│   ├── routbuddy-common/                # Common kernel, enums, exceptions, crypto, PostGIS utils
│   ├── routbuddy-auth/                  # JWT, Phone OTP, OAuth2, RBAC
│   ├── routbuddy-users/                 # User profiles, corporate/college KYC, trust scores
│   ├── routbuddy-drivers/               # Driver onboarding, licensing, daily/weekly earnings ledger
│   ├── routbuddy-vehicles/              # Vehicle registry (Auto-3W, E-Rickshaw, Van), seating layouts
│   ├── routbuddy-routes/                # Corridors, designated stops, stage fares, PostGIS geometries
│   ├── routbuddy-trips/                 # Trip scheduling, live status state machine, telemetry
│   ├── routbuddy-bookings/              # Seat reservations, dynamic QR codes, ticketing
│   ├── routbuddy-payments/              # UPI/Razorpay integration, driver payout ledger
│   ├── routbuddy-subscriptions/         # Weekly, monthly, semester commute passes
│   ├── routbuddy-matching/              # Daily commute partner matching, mutual double opt-in
│   ├── routbuddy-locations/             # Real-time GPS ingestion, Redis GEO, WebSocket STOMP
│   ├── routbuddy-notifications/         # Push notifications (FCM), SMS alerts
│   ├── routbuddy-ratings/               # Reviews, ratings, trust score recalculation
│   ├── routbuddy-safety/                # Emergency SOS alerts, route deviation detection
│   ├── routbuddy-complaints/            # Grievances, dispute resolution
│   ├── routbuddy-analytics/             # Route demand heatmaps, fleet occupancy trends
│   ├── routbuddy-admin/                 # Backoffice APIs, KYC approval queue
│   └── routbuddy-bootstrap/             # Unified Spring Boot runner & OpenAPI Swagger docs
│
├── ai-service/                          # AI/ML Microservice (Python 3.11 / FastAPI)
│   ├── app/
│   │   ├── main.py                      # FastAPI application
│   │   └── models/
│   │       ├── partner_matcher.py       # Multi-factor commute similarity scoring
│   │       ├── demand_predictor.py      # Spatial-temporal transit demand forecaster
│   │       └── occupancy_predictor.py   # Stop-by-stop occupancy inference
│   ├── requirements.txt
│   └── Dockerfile
│
├── frontend-admin/                      # Admin & Operations Dashboard (Next.js 14 / TypeScript)
│   ├── src/app/                         # Dashboard, Drivers KYC, Corridors, Safety SOS, Analytics
│   ├── src/components/                  # LiveFleetMap, StatCard, Sidebar, Navbar
│   ├── package.json
│   └── Dockerfile
│
├── mobile/                              # Mobile Client Applications (Flutter / Dart)
│   ├── routbuddy_passenger/             # Passenger & Commuter App (Search, QR Pass, Partner Matching, SOS)
│   └── routbuddy_driver/                # Driver App (Trip dispatch, Telemetry, Camera QR Scanner, Earnings)
│
└── infrastructure/                      # DevOps & Cloud Infrastructure
    ├── docker/                          # docker-compose.yml (PostgreSQL+PostGIS, Redis, Backend, AI, Admin)
    ├── terraform/                       # AWS IaC (VPC, RDS PostGIS, ElastiCache, S3, WAF, CloudFront)
    └── .github/workflows/               # GitHub Actions CI/CD pipeline
```

---

## 🚀 Quickstart (Running with Docker Compose)

To spin up the entire RoutBuddy stack locally:

```bash
cd infrastructure/docker
docker-compose up --build -d
```

### Access Platform Portals:
- **Backend API & Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **AI Microservice Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **Admin & Operations Dashboard**: [http://localhost:3000](http://localhost:3000)
- **PostgreSQL PostGIS**: `localhost:5432` (`user: postgres`, `pass: postgrespassword`)
- **Redis Broker**: `localhost:6379`

---

## 🛡 Security & Privacy Shield

- **Double Opt-In Commuter Handshake**: Exact residential addresses and contact numbers are protected behind ~500m geohash privacy buffers and virtual relay IDs. Coordinates and in-app chat are only unlocked once both commuters mutually accept a match.
- **Dynamic Boarding Passes**: QR codes are cryptographically signed using `HMAC-SHA256(booking_id, trip_id, passenger_id, expiry)` to prevent counterfeit tickets and support offline verification.
- **24/7 Safety Command Center**: Integrated SOS triggers broadcast instant SMS alerts to registered emergency contacts and push live coordinates to the Operations Dashboard.
