from typing import List
from pydantic import BaseModel
import math

class DemandForecastRequest(BaseModel):
    hub_id: str
    hub_name: str
    latitude: float
    longitude: float
    hour_of_day: int
    day_of_week: int  # 0 = Monday, 6 = Sunday
    is_raining: bool = False
    metro_train_arrival_window: bool = True

class DemandForecastResponse(BaseModel):
    hub_id: str
    hub_name: str
    predicted_demand_level: str  # "HIGH", "MODERATE", "SURGE", "LOW"
    estimated_passenger_queue: int
    recommended_active_autos: int
    surge_fare_multiplier: float
    confidence_score: float

class DemandPredictorAI:
    @staticmethod
    def forecast_demand(req: DemandForecastRequest) -> DemandForecastResponse:
        # Base demand curve based on commute rush hours (8-11 AM and 5-9 PM)
        hour = req.hour_of_day
        if 8 <= hour <= 10 or 17 <= hour <= 20:
            base_queue = 45
            level = "HIGH"
            multiplier = 1.2
        elif 11 <= hour <= 16:
            base_queue = 18
            level = "MODERATE"
            multiplier = 1.0
        elif 21 <= hour <= 23:
            base_queue = 25
            level = "MODERATE"
            multiplier = 1.1
        else:
            base_queue = 5
            level = "LOW"
            multiplier = 1.0

        # Adjust for rain / weather
        if req.is_raining:
            base_queue = int(base_queue * 1.6)
            level = "SURGE"
            multiplier = min(1.5, multiplier + 0.3)

        # Adjust for metro train discharge
        if req.metro_train_arrival_window:
            base_queue += 20
            if level == "MODERATE":
                level = "HIGH"

        # Weekday vs weekend
        if req.day_of_week in [5, 6]:  # Weekend
            base_queue = int(base_queue * 0.7)

        recommended_autos = math.ceil(base_queue / 3.0)

        return DemandForecastResponse(
            hub_id=req.hub_id,
            hub_name=req.hub_name,
            predicted_demand_level=level,
            estimated_passenger_queue=base_queue,
            recommended_active_autos=recommended_autos,
            surge_fare_multiplier=round(multiplier, 2),
            confidence_score=0.92
        )
