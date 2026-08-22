from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import List
from pydantic import BaseModel

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
    title="RoutBuddy AI & ML Microservice",
    description="AI Intelligence service for Commute Partner Matching, Transit Demand Forecasting, and Occupancy Inference",
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

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "routbuddy-ai-service",
        "version": "1.0.0"
    }

@app.post("/api/v1/ai/match-commuters", response_model=List[CommuteMatchResult])
def match_commuters(req: CommuteMatchBatchRequest):
    results = []
    for candidate in req.candidates:
        match = PartnerMatcherAI.calculate_match(req.target, candidate)
        if match is not None:
            results.append(match)

    # Sort descending by composite match score
    results.sort(key=lambda x: x.composite_match_score, reverse=True)
    return results

@app.post("/api/v1/ai/demand-forecast", response_model=DemandForecastResponse)
def forecast_demand(req: DemandForecastRequest):
    return DemandPredictorAI.forecast_demand(req)

@app.post("/api/v1/ai/occupancy-predict", response_model=OccupancyPredictionResponse)
def predict_occupancy(req: OccupancyPredictionRequest):
    return OccupancyPredictorAI.predict_occupancy(req)
