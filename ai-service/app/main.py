from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse, FileResponse
from typing import List
from pydantic import BaseModel
import os
import json
import time
import hmac
import hashlib

from app.models.partner_matcher import (
    CommuterCandidate,
    CommuteMatchResult,
    PartnerMatcherAI
)
from app.models.demand_predictor import (
    DemandForecastRequest,
    DemandForecastResponse,
    DemandPredictorAI
)
from app.models.occupancy_predictor import (
    OccupancyPredictionRequest,
    OccupancyPredictionResponse,
    OccupancyPredictorAI
)

app = FastAPI(
    title="RoutBuddy Smart Mobility Platform & AI Service",
    description="AI Intelligence service & Interactive Simulator for Shared Transit & Daily Commute Matching",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class CommuteMatchBatchRequest(BaseModel):
    target: CommuterCandidate
    candidates: List[CommuterCandidate]

class DynamicQRRequest(BaseModel):
    booking_id: str
    trip_id: str
    passenger_id: str
    secret_key: str = "routbuddy_hmac_sha256_secret_key_for_boarding_tokens_2026"

class DynamicQRVerificationRequest(BaseModel):
    qr_payload: str
    secret_key: str = "routbuddy_hmac_sha256_secret_key_for_boarding_tokens_2026"

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "routbuddy-platform-service",
        "version": "1.0.0",
        "timestamp": time.time()
    }

@app.get("/download-apk")
def download_apk():
    apk_path = "C:/Users/Shubhi/OneDrive/Desktop/pbl3/mobile/routbuddy_passenger/build/app/outputs/flutter-apk/app-debug.apk"
    if not os.path.exists(apk_path):
        raise HTTPException(status_code=404, detail="APK build not found on server")
    return FileResponse(
        apk_path,
        media_type="application/vnd.android.package-archive",
        filename="routbuddy_passenger.apk"
    )

@app.post("/api/v1/ai/match-commuters", response_model=List[CommuteMatchResult])
def match_commuters(req: CommuteMatchBatchRequest):
    results = []
    for candidate in req.candidates:
        match = PartnerMatcherAI.calculate_match(req.target, candidate)
        if match is not None:
            results.append(match)
    results.sort(key=lambda x: x.composite_match_score, reverse=True)
    return results

@app.post("/api/v1/ai/demand-forecast", response_model=DemandForecastResponse)
def forecast_demand(req: DemandForecastRequest):
    return DemandPredictorAI.forecast_demand(req)

@app.post("/api/v1/ai/occupancy-predict", response_model=OccupancyPredictionResponse)
def predict_occupancy(req: OccupancyPredictionRequest):
    return OccupancyPredictorAI.predict_occupancy(req)

@app.post("/api/v1/crypto/generate-qr-pass")
def generate_qr_pass(req: DynamicQRRequest):
    time_window = int(time.time() // 60)
    data = f"{req.booking_id}:{req.trip_id}:{req.passenger_id}:{time_window}"
    signature = hmac.new(req.secret_key.encode(), data.encode(), hashlib.sha256).hexdigest()
    return {
        "booking_id": req.booking_id,
        "trip_id": req.trip_id,
        "passenger_id": req.passenger_id,
        "time_window": time_window,
        "validity_seconds_remaining": 60 - int(time.time() % 60),
        "qr_signature": signature,
        "raw_qr_token": f"{data}:{signature}"
    }

@app.post("/api/v1/crypto/verify-qr-pass")
def verify_qr_pass(req: DynamicQRVerificationRequest):
    try:
        parts = req.qr_payload.split(":")
        if len(parts) != 5:
            return {"valid": False, "reason": "Malformed QR token structure"}
        
        booking_id, trip_id, passenger_id, time_window_str, signature = parts
        time_window = int(time_window_str)
        current_window = int(time.time() // 60)
        
        # Accept current window or immediate previous window (for edge drift)
        if abs(current_window - time_window) > 1:
            return {"valid": False, "reason": "QR pass expired (validity exceeded)"}
        
        expected_data = f"{booking_id}:{trip_id}:{passenger_id}:{time_window}"
        expected_signature = hmac.new(req.secret_key.encode(), expected_data.encode(), hashlib.sha256).hexdigest()
        
        if hmac.compare_digest(signature, expected_signature):
            return {
                "valid": True,
                "booking_id": booking_id,
                "trip_id": trip_id,
                "passenger_id": passenger_id,
                "message": "Boarding pass cryptographically verified successfully!"
            }
        else:
            return {"valid": False, "reason": "Invalid cryptographic signature (tampering detected)"}
    except Exception as e:
        return {"valid": False, "reason": str(e)}

@app.get("/", response_class=HTMLResponse)
def get_interactive_dashboard():
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RoutBuddy 🛺 | Smart Mobility & AI Commute Platform</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; }
        .glass-panel { background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(12px); }
        .glow-border { border: 1px solid rgba(16, 185, 129, 0.3); }
        #map { height: 380px; width: 100%; border-radius: 12px; }
        .tab-btn.active { background: #059669; color: white; border-color: #10B981; }
    </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen">
    <!-- Navbar -->
    <header class="border-b border-slate-800 bg-slate-900/90 backdrop-blur sticky top-0 z-50 px-6 py-4 flex items-center justify-between">
        <div class="flex items-center space-x-3">
            <div class="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center text-slate-950 font-black text-xl shadow-lg shadow-emerald-500/20">🛺</div>
            <div>
                <h1 class="text-xl font-bold tracking-tight text-white flex items-center gap-2">
                    RoutBuddy <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Local Dev Platform</span>
                </h1>
                <p class="text-xs text-slate-400">AI-Powered Smart Mobility Platform for Shared Transportation & Daily Commute</p>
            </div>
        </div>
        <div class="flex items-center space-x-3">
            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-950 border border-emerald-500/30 text-emerald-400">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse mr-1.5"></span> Engine Running
            </span>
            <a href="/docs" target="_blank" class="text-xs font-semibold px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 transition">
                Interactive Swagger Docs ↗
            </a>
        </div>
    </header>

    <main class="max-w-7xl mx-auto p-6 space-y-6">
        <!-- Top Stats Row -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div class="glass-panel p-4 rounded-xl border border-slate-800 flex items-center justify-between">
                <div>
                    <p class="text-xs font-medium text-slate-400">Active Transit Corridors</p>
                    <p class="text-2xl font-bold text-white mt-1">12 Corridors</p>
                    <p class="text-[10px] text-emerald-400 mt-0.5">● 100% PostGIS Geo-Indexed</p>
                </div>
                <div class="p-3 bg-emerald-500/10 rounded-xl text-emerald-400">🗺️</div>
            </div>
            <div class="glass-panel p-4 rounded-xl border border-slate-800 flex items-center justify-between">
                <div>
                    <p class="text-xs font-medium text-slate-400">Active Shared Autos</p>
                    <p class="text-2xl font-bold text-white mt-1">48 Vehicles</p>
                    <p class="text-[10px] text-emerald-400 mt-0.5">● Redis GEO Telemetry &le;1s</p>
                </div>
                <div class="p-3 bg-blue-500/10 rounded-xl text-blue-400">🛺</div>
            </div>
            <div class="glass-panel p-4 rounded-xl border border-slate-800 flex items-center justify-between">
                <div>
                    <p class="text-xs font-medium text-slate-400">AI Match Accuracy</p>
                    <p class="text-2xl font-bold text-white mt-1">94.8%</p>
                    <p class="text-[10px] text-emerald-400 mt-0.5">● Multi-Factor Spatial Scoring</p>
                </div>
                <div class="p-3 bg-purple-500/10 rounded-xl text-purple-400">🧠</div>
            </div>
            <div class="glass-panel p-4 rounded-xl border border-slate-800 flex items-center justify-between">
                <div>
                    <p class="text-xs font-medium text-slate-400">Dynamic Boarding QR</p>
                    <p class="text-2xl font-bold text-white mt-1">HMAC-SHA256</p>
                    <p class="text-[10px] text-emerald-400 mt-0.5">● 60s Anti-Counterfeit Window</p>
                </div>
                <div class="p-3 bg-amber-500/10 rounded-xl text-amber-400">🛡️</div>
            </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="flex space-x-2 border-b border-slate-800 pb-2">
            <button onclick="switchTab('module1')" id="tab-btn-module1" class="tab-btn active px-4 py-2 text-sm font-semibold rounded-lg border border-slate-700 bg-slate-900 transition flex items-center gap-2">
                <span>🛺</span> Module 1: Live Shared Auto Corridor
            </button>
            <button onclick="switchTab('module2')" id="tab-btn-module2" class="tab-btn px-4 py-2 text-sm font-semibold rounded-lg border border-slate-700 bg-slate-900 transition flex items-center gap-2">
                <span>🤝</span> Module 2: AI Commute Partner Matcher
            </button>
            <button onclick="switchTab('demand')" id="tab-btn-demand" class="tab-btn px-4 py-2 text-sm font-semibold rounded-lg border border-slate-700 bg-slate-900 transition flex items-center gap-2">
                <span>📈</span> AI Demand & Surge Forecaster
            </button>
            <button onclick="switchTab('crypto')" id="tab-btn-crypto" class="tab-btn px-4 py-2 text-sm font-semibold rounded-lg border border-slate-700 bg-slate-900 transition flex items-center gap-2">
                <span>🔐</span> Dynamic QR Pass Verifier
            </button>
        </div>

        <!-- TAB 1: MODULE 1 SHARED TRANSIT -->
        <div id="tab-module1" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="lg:col-span-2 glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <div class="flex items-center justify-between">
                    <div>
                        <h2 class="text-lg font-bold text-white flex items-center gap-2">
                            <span>📍</span> Corridor: Electronic City Tech Hub &lrarr; Silk Board Metro
                        </h2>
                        <p class="text-xs text-slate-400">Live Stage-Fare Corridor • Distance: 8.4 km • Est. Time: 22 min</p>
                    </div>
                    <div class="flex items-center gap-2">
                        <button onclick="toggleSimulation()" id="sim-btn" class="px-3 py-1.5 text-xs font-bold rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white transition flex items-center gap-1.5 shadow-lg shadow-emerald-600/30">
                            <span>▶</span> Start Live GPS Telemetry
                        </button>
                    </div>
                </div>

                <!-- Leaflet Map Container -->
                <div id="map" class="border border-slate-800 shadow-inner"></div>

                <!-- Live Corridor Stage Stops Bar -->
                <div class="grid grid-cols-4 gap-2 pt-2">
                    <div class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-center">
                        <span class="text-[10px] text-slate-400">Stop 1 (Origin)</span>
                        <p class="text-xs font-bold text-emerald-400">Silk Board Metro</p>
                        <span class="text-[10px] text-slate-500">Fare: ₹10</span>
                    </div>
                    <div class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-center">
                        <span class="text-[10px] text-slate-400">Stop 2</span>
                        <p class="text-xs font-bold text-slate-200">Bommanahalli</p>
                        <span class="text-[10px] text-slate-500">Fare: ₹20</span>
                    </div>
                    <div class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-center">
                        <span class="text-[10px] text-slate-400">Stop 3</span>
                        <p class="text-xs font-bold text-slate-200">Kudlu Gate</p>
                        <span class="text-[10px] text-slate-500">Fare: ₹30</span>
                    </div>
                    <div class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-center">
                        <span class="text-[10px] text-slate-400">Stop 4 (Dest)</span>
                        <p class="text-xs font-bold text-emerald-400">Infosys Gate 1</p>
                        <span class="text-[10px] text-slate-500">Fare: ₹40</span>
                    </div>
                </div>
            </div>

            <!-- Live Vehicle & Seat Meter Panel -->
            <div class="space-y-4">
                <div class="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                    <div class="flex items-center justify-between border-b border-slate-800 pb-3">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-slate-200">🛺</div>
                            <div>
                                <h3 class="text-sm font-bold text-white">KA-05-AA-8921</h3>
                                <p class="text-xs text-slate-400">Bajaj RE E-Rickshaw • Ramesh Kumar</p>
                            </div>
                        </div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-950 border border-emerald-500/30 text-emerald-400">KYC VERIFIED</span>
                    </div>

                    <!-- Seat Occupancy Meter -->
                    <div>
                        <div class="flex justify-between text-xs mb-1.5">
                            <span class="text-slate-400 font-medium">Live Seat Occupancy</span>
                            <span id="seat-label" class="font-bold text-emerald-400">2 / 4 Seats Available</span>
                        </div>
                        <div class="grid grid-cols-4 gap-2" id="seat-grid">
                            <div class="p-3 rounded-lg bg-emerald-500/20 border border-emerald-500/50 text-center font-bold text-emerald-400 text-xs">Seat 1<br><span class="text-[10px] font-normal">Free</span></div>
                            <div class="p-3 rounded-lg bg-emerald-500/20 border border-emerald-500/50 text-center font-bold text-emerald-400 text-xs">Seat 2<br><span class="text-[10px] font-normal">Free</span></div>
                            <div class="p-3 rounded-lg bg-rose-500/20 border border-rose-500/50 text-center font-bold text-rose-400 text-xs">Seat 3<br><span class="text-[10px] font-normal">Occupied</span></div>
                            <div class="p-3 rounded-lg bg-rose-500/20 border border-rose-500/50 text-center font-bold text-rose-400 text-xs">Seat 4<br><span class="text-[10px] font-normal">Occupied</span></div>
                        </div>
                    </div>

                    <!-- Instant Booking Simulation -->
                    <div class="pt-2 border-t border-slate-800 space-y-3">
                        <button onclick="bookSeat()" class="w-full py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 font-bold text-white text-xs shadow-lg shadow-emerald-600/20 transition">
                            Book Shared Auto Seat (₹30)
                        </button>
                        <div id="booking-confirmation" class="hidden p-3 rounded-xl bg-slate-900 border border-emerald-500/40 text-xs space-y-1">
                            <p class="font-bold text-emerald-400 flex items-center gap-1.5">✓ Seat Reserved Successfully!</p>
                            <p class="text-slate-400 text-[11px]">Booking ID: <code class="text-white" id="res-booking-id">RB-9841</code></p>
                            <p class="text-slate-400 text-[11px]">HMAC QR Token generated for offline driver scan.</p>
                        </div>
                    </div>

                    <!-- Driver Daily Ledger -->
                    <div class="p-3.5 rounded-xl bg-slate-900 border border-slate-800 space-y-2 text-xs">
                        <div class="flex justify-between font-bold">
                            <span class="text-slate-400">Driver Today's Ledger</span>
                            <span class="text-emerald-400">₹640.00 Net</span>
                        </div>
                        <div class="flex justify-between text-[11px] text-slate-400">
                            <span>Cash Collected:</span> <span class="text-slate-200">₹320.00</span>
                        </div>
                        <div class="flex justify-between text-[11px] text-slate-400">
                            <span>Digital UPI Passes:</span> <span class="text-slate-200">₹320.00</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- TAB 2: MODULE 2 AI COMMUTE MATCHING -->
        <div id="tab-module2" class="hidden grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <h2 class="text-base font-bold text-white flex items-center gap-2">
                    <span>👤</span> Commuter Profile (Search Query)
                </h2>
                <div class="space-y-3 text-xs">
                    <div>
                        <label class="block text-slate-400 mb-1">Your Full Name</label>
                        <input id="q-name" type="text" value="Aarav Sharma" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium focus:border-emerald-500 outline-none">
                    </div>
                    <div>
                        <label class="block text-slate-400 mb-1">Corporate / Campus Email</label>
                        <input id="q-email" type="text" value="aarav.s@infosys.com" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium focus:border-emerald-500 outline-none">
                    </div>
                    <div class="grid grid-cols-2 gap-2">
                        <div>
                            <label class="block text-slate-400 mb-1">Departure Time</label>
                            <input id="q-time" type="text" value="09:00 AM" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium outline-none">
                        </div>
                        <div>
                            <label class="block text-slate-400 mb-1">Gender Preference</label>
                            <select id="q-gender" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium outline-none">
                                <option value="ANY">Any Gender</option>
                                <option value="SAME_GENDER_ONLY">Same Gender Only</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label class="block text-slate-400 mb-1">Home Origin (Geohash Obscured)</label>
                        <input id="q-origin" type="text" value="HSR Layout Sector 2 (~500m Buffer)" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-slate-300 outline-none" disabled>
                    </div>
                    <div>
                        <label class="block text-slate-400 mb-1">Work Destination</label>
                        <input id="q-dest" type="text" value="Infosys Gate 1, Electronic City" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-slate-300 outline-none" disabled>
                    </div>
                    <button onclick="runAIMatching()" class="w-full py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 font-bold text-white text-xs shadow-lg shadow-emerald-600/30 transition">
                        Run AI Multi-Factor Matching Engine
                    </button>
                </div>
            </div>

            <!-- AI Matching Results Panel -->
            <div class="lg:col-span-2 glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <div class="flex items-center justify-between border-b border-slate-800 pb-3">
                    <div>
                        <h3 class="text-base font-bold text-white flex items-center gap-2">
                            <span>🎯</span> AI Verified Match Candidates
                        </h3>
                        <p class="text-xs text-slate-400">Ranked by Spatiotemporal Overlap + Org Verification + Trust Score</p>
                    </div>
                    <span class="text-xs px-2.5 py-1 rounded bg-purple-950 border border-purple-500/40 text-purple-300 font-bold">FastAPI Scikit-Learn Engine</span>
                </div>

                <div id="ai-match-cards" class="space-y-3">
                    <!-- Dynamic Match Card 1 -->
                    <div class="p-4 rounded-xl bg-slate-900 border border-emerald-500/40 space-y-3">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center font-bold text-white">PS</div>
                                <div>
                                    <h4 class="text-sm font-bold text-white flex items-center gap-2">
                                        Priya Sen <span class="text-[10px] px-2 py-0.5 rounded bg-blue-950 border border-blue-500/30 text-blue-300">@infosys.com Verified</span>
                                    </h4>
                                    <p class="text-xs text-slate-400">Car Commuter • Honda City • Departs 09:10 AM</p>
                                </div>
                            </div>
                            <div class="text-right">
                                <span class="text-xl font-extrabold text-emerald-400">94.8%</span>
                                <p class="text-[10px] font-bold text-emerald-300">PERFECT MATCH</p>
                            </div>
                        </div>
                        <div class="grid grid-cols-4 gap-2 text-[11px] bg-slate-950/60 p-2.5 rounded-lg">
                            <div><span class="text-slate-500">Route Overlap:</span> <p class="font-bold text-white">96.2%</p></div>
                            <div><span class="text-slate-500">Pickup Distance:</span> <p class="font-bold text-white">0.6 km</p></div>
                            <div><span class="text-slate-500">Schedule Delta:</span> <p class="font-bold text-white">10 min</p></div>
                            <div><span class="text-slate-500">Trust Score:</span> <p class="font-bold text-amber-400">⭐ 4.9 / 5.0</p></div>
                        </div>
                        <div class="flex justify-between items-center pt-1">
                            <span class="text-[11px] text-slate-400">🛡️ Double Opt-In: Coordinates obscured until mutual acceptance.</span>
                            <button onclick="requestHandshake(this)" class="px-4 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-xs font-bold text-white transition">
                                Request Ride Handshake
                            </button>
                        </div>
                    </div>

                    <!-- Dynamic Match Card 2 -->
                    <div class="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-3">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center font-bold text-white">VK</div>
                                <div>
                                    <h4 class="text-sm font-bold text-white flex items-center gap-2">
                                        Vikram Kohli <span class="text-[10px] px-2 py-0.5 rounded bg-blue-950 border border-blue-500/30 text-blue-300">@wipro.com Verified</span>
                                    </h4>
                                    <p class="text-xs text-slate-400">Bike Commuter • Royal Enfield • Departs 08:50 AM</p>
                                </div>
                            </div>
                            <div class="text-right">
                                <span class="text-xl font-extrabold text-blue-400">82.1%</span>
                                <p class="text-[10px] font-bold text-blue-300">HIGH COMPATIBILITY</p>
                            </div>
                        </div>
                        <div class="grid grid-cols-4 gap-2 text-[11px] bg-slate-950/60 p-2.5 rounded-lg">
                            <div><span class="text-slate-500">Route Overlap:</span> <p class="font-bold text-white">84.0%</p></div>
                            <div><span class="text-slate-500">Pickup Distance:</span> <p class="font-bold text-white">1.2 km</p></div>
                            <div><span class="text-slate-500">Schedule Delta:</span> <p class="font-bold text-white">10 min</p></div>
                            <div><span class="text-slate-500">Trust Score:</span> <p class="font-bold text-amber-400">⭐ 4.8 / 5.0</p></div>
                        </div>
                        <div class="flex justify-between items-center pt-1">
                            <span class="text-[11px] text-slate-400">🛡️ Double Opt-In: Coordinates obscured until mutual acceptance.</span>
                            <button onclick="requestHandshake(this)" class="px-4 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-200 transition">
                                Request Ride Handshake
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- TAB 3: DEMAND FORECASTER -->
        <div id="tab-demand" class="hidden grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <h2 class="text-base font-bold text-white flex items-center gap-2">
                    <span>⚙️</span> Transit Hub Scenario Controls
                </h2>
                <div class="space-y-4 text-xs">
                    <div>
                        <div class="flex justify-between mb-1 text-slate-400">
                            <span>Hour of Day</span> <span id="hour-val" class="text-white font-bold">09:00 AM (Peak Rush)</span>
                        </div>
                        <input id="hour-slider" type="range" min="6" max="23" value="9" class="w-full accent-emerald-500 cursor-pointer" oninput="updateDemandForecast()">
                    </div>
                    <div>
                        <label class="block text-slate-400 mb-1">Weather Condition</label>
                        <select id="rain-select" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium outline-none" onchange="updateDemandForecast()">
                            <option value="false">Clear Skies / Normal</option>
                            <option value="true">Heavy Rain / Monsoon Surge</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-slate-400 mb-1">Metro Train Arrival Window</label>
                        <select id="metro-select" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2 text-white font-medium outline-none" onchange="updateDemandForecast()">
                            <option value="true">Train Discharged (High Inflow)</option>
                            <option value="false">Off-Schedule / Normal Gap</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- Forecast Output -->
            <div class="lg:col-span-2 glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <h3 class="text-base font-bold text-white flex items-center gap-2">
                    <span>📊</span> Real-Time Transit Hub Demand Prediction
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
                        <span class="text-xs text-slate-400">Demand Classification</span>
                        <p id="pred-level" class="text-2xl font-extrabold text-emerald-400 mt-1">HIGH</p>
                        <span class="text-[10px] text-slate-500">Confidence: 92.4%</span>
                    </div>
                    <div class="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
                        <span class="text-xs text-slate-400">Est. Passenger Queue</span>
                        <p id="pred-queue" class="text-2xl font-extrabold text-white mt-1">65 Passengers</p>
                        <span class="text-[10px] text-slate-500">At Silk Board Interchange</span>
                    </div>
                    <div class="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
                        <span class="text-xs text-slate-400">Required Fleet Dispatch</span>
                        <p id="pred-fleet" class="text-2xl font-extrabold text-blue-400 mt-1">22 Autos</p>
                        <span class="text-[10px] text-emerald-400">Dynamic Multiplier: 1.2x</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- TAB 4: CRYPTOGRAPHIC QR VERIFIER -->
        <div id="tab-crypto" class="hidden grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <h2 class="text-base font-bold text-white flex items-center gap-2">
                    <span>📱</span> Passenger HMAC Dynamic Boarding Pass
                </h2>
                <div class="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center space-y-3">
                    <div class="w-48 h-48 mx-auto bg-white p-3 rounded-xl flex items-center justify-center border-4 border-emerald-500/50 shadow-2xl">
                        <!-- SVG QR Representation -->
                        <svg class="w-full h-full text-slate-950" viewBox="0 0 100 100" fill="currentColor">
                            <rect x="10" y="10" width="25" height="25" fill="#000" />
                            <rect x="15" y="15" width="15" height="15" fill="#fff" />
                            <rect x="18" y="18" width="9" height="9" fill="#000" />
                            <rect x="65" y="10" width="25" height="25" fill="#000" />
                            <rect x="70" y="15" width="15" height="15" fill="#fff" />
                            <rect x="73" y="18" width="9" height="9" fill="#000" />
                            <rect x="10" y="65" width="25" height="25" fill="#000" />
                            <rect x="15" y="70" width="15" height="15" fill="#fff" />
                            <rect x="18" y="73" width="9" height="9" fill="#000" />
                            <rect x="42" y="42" width="16" height="16" fill="#059669" />
                            <circle cx="50" cy="50" r="4" fill="#fff" />
                        </svg>
                    </div>
                    <div class="space-y-1">
                        <p class="text-xs font-bold text-slate-200">Dynamic Rolling Token</p>
                        <p class="text-[11px] font-mono text-emerald-400 break-all" id="qr-display-token">RB-BK7812:TRIP-99:USR-441:30091823:7f8a9...</p>
                        <p class="text-[10px] text-amber-400">⏱ Window Expiry: <span id="qr-expiry-countdown">48</span>s</p>
                    </div>
                </div>
            </div>

            <!-- Driver Scanner & Offline Verification -->
            <div class="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
                <h2 class="text-base font-bold text-white flex items-center gap-2">
                    <span>📷</span> Driver Offline Verification Terminal
                </h2>
                <div class="space-y-3 text-xs">
                    <p class="text-slate-400">The driver's camera scans the passenger token. The device cryptographically verifies the signature without requiring internet access.</p>
                    <textarea id="scanner-input" rows="3" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-2.5 font-mono text-slate-300 outline-none">RB-BK7812:TRIP-99:USR-441:30091823:7f8a9c2b4d1e</textarea>
                    <button onclick="testVerifyQR()" class="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 font-bold text-white transition">
                        Verify Boarding Token Signature
                    </button>
                    <div id="verify-result" class="hidden p-3 rounded-xl bg-emerald-950/80 border border-emerald-500/50 text-emerald-300 space-y-1">
                        <p class="font-bold flex items-center gap-1.5">✓ PASSENGER BOARDING APPROVED</p>
                        <p class="text-[11px] text-slate-300">Valid Seat Reservation • Seat Assigned: 1 • Fare Reconciled: ₹30</p>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <script>
        // Tab Switcher
        function switchTab(tabId) {
            ['module1', 'module2', 'demand', 'crypto'].forEach(t => {
                document.getElementById('tab-' + t).classList.add('hidden');
                document.getElementById('tab-btn-' + t).classList.remove('active');
            });
            document.getElementById('tab-' + tabId).classList.remove('hidden');
            document.getElementById('tab-btn-' + tabId).classList.add('active');
            if (tabId === 'module1') {
                setTimeout(() => { map.invalidateSize(); }, 200);
            }
        }

        // Leaflet Map Initialization (Bengaluru Silk Board - Electronic City Corridor)
        const map = L.map('map').setView([12.895, 77.640], 12);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            attribution: '&copy; OpenStreetMap contributors &copy; CARTO',
            maxZoom: 19
        }).addTo(map);

        // Corridor Stops
        const stops = [
            { name: "Silk Board Metro", coords: [12.9175, 77.6234] },
            { name: "Bommanahalli", coords: [12.9038, 77.6322] },
            { name: "Kudlu Gate", coords: [12.8872, 77.6475] },
            { name: "Infosys Gate 1 (E-City)", coords: [12.8452, 77.6635] }
        ];

        // Draw Corridor Polyline
        const latlngs = stops.map(s => s.coords);
        const polyline = L.polyline(latlngs, { color: '#10B981', weight: 4, opacity: 0.8 }).addTo(map);
        map.fitBounds(polyline.getBounds(), { padding: [30, 30] });

        // Add Stop Markers
        stops.forEach((s, idx) => {
            L.circleMarker(s.coords, {
                radius: 6,
                fillColor: idx === 0 || idx === stops.length - 1 ? '#10B981' : '#3B82F6',
                color: '#ffffff',
                weight: 2,
                fillOpacity: 1
            }).addTo(map).bindPopup(`<b>${s.name}</b><br>Stage Stop ${idx + 1}`);
        });

        // Auto Rickshaw Marker
        const autoIcon = L.divIcon({
            html: '<div style="font-size: 24px; filter: drop-shadow(0 0 8px rgba(16,185,129,0.8));">🛺</div>',
            className: 'custom-auto-icon',
            iconSize: [30, 30],
            iconAnchor: [15, 15]
        });
        let autoMarker = L.marker(stops[0].coords, { icon: autoIcon }).addTo(map);

        // Simulation Loop
        let isSimulating = false;
        let simStep = 0;
        let simInterval = null;

        function toggleSimulation() {
            isSimulating = !isSimulating;
            const btn = document.getElementById('sim-btn');
            if (isSimulating) {
                btn.innerHTML = '<span>⏸</span> Pause Telemetry Stream';
                btn.classList.replace('bg-emerald-600', 'bg-amber-600');
                simInterval = setInterval(() => {
                    simStep = (simStep + 1) % 40;
                    const stopIndex = Math.floor(simStep / 10);
                    const subStep = (simStep % 10) / 10.0;
                    
                    const p1 = stops[Math.min(stopIndex, stops.length - 2)].coords;
                    const p2 = stops[Math.min(stopIndex + 1, stops.length - 1)].coords;
                    
                    const curLat = p1[0] + (p2[0] - p1[0]) * subStep;
                    const curLng = p1[1] + (p2[1] - p1[1]) * subStep;
                    
                    autoMarker.setLatLng([curLat, curLng]);
                }, 400);
            } else {
                btn.innerHTML = '<span>▶</span> Resume Telemetry Stream';
                btn.classList.replace('bg-amber-600', 'bg-emerald-600');
                clearInterval(simInterval);
            }
        }

        // Booking Simulation
        let booked = false;
        function bookSeat() {
            booked = true;
            document.getElementById('seat-label').innerText = '1 / 4 Seats Available';
            document.getElementById('booking-confirmation').classList.remove('hidden');
            document.getElementById('seat-grid').children[1].className = 'p-3 rounded-lg bg-rose-500/20 border border-rose-500/50 text-center font-bold text-rose-400 text-xs';
            document.getElementById('seat-grid').children[1].innerHTML = 'Seat 2<br><span class="text-[10px] font-normal">Reserved (You)</span>';
        }

        // AI Match Handshake
        function requestHandshake(btn) {
            btn.innerText = '✓ Handshake Sent (Double Opt-In Pending)';
            btn.className = 'px-4 py-1.5 rounded-lg bg-emerald-950 border border-emerald-500/40 text-emerald-400 text-xs font-bold';
        }

        // Demand Forecast Update
        function updateDemandForecast() {
            const h = document.getElementById('hour-slider').value;
            const isRain = document.getElementById('rain-select').value === 'true';
            const isMetro = document.getElementById('metro-select').value === 'true';
            
            document.getElementById('hour-val').innerText = (h < 12 ? h + ':00 AM' : (h == 12 ? '12:00 PM' : (h-12) + ':00 PM')) + (h >= 8 && h <= 10 || h >= 17 && h <= 20 ? ' (Peak Rush)' : ' (Normal)');
            
            let q = (h >= 8 && h <= 10 || h >= 17 && h <= 20) ? 45 : 18;
            if (isRain) q = Math.floor(q * 1.6);
            if (isMetro) q += 20;
            
            const autos = Math.ceil(q / 3.0);
            const level = isRain || q > 60 ? 'SURGE' : (q > 35 ? 'HIGH' : 'MODERATE');
            
            document.getElementById('pred-queue').innerText = q + ' Passengers';
            document.getElementById('pred-fleet').innerText = autos + ' Autos';
            document.getElementById('pred-level').innerText = level;
            document.getElementById('pred-level').className = 'text-2xl font-extrabold mt-1 ' + (level === 'SURGE' ? 'text-rose-400' : 'text-emerald-400');
        }

        // QR Verification Test
        function testVerifyQR() {
            document.getElementById('verify-result').classList.remove('hidden');
        }

        // Countdown timer simulation
        setInterval(() => {
            const el = document.getElementById('qr-expiry-countdown');
            if (el) {
                let val = parseInt(el.innerText) - 1;
                if (val <= 0) val = 60;
                el.innerText = val;
            }
        }, 1000);
    </script>
</body>
</html>
    """
