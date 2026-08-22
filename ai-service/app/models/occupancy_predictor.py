from typing import List
from pydantic import BaseModel

class StopOccupancyInference(BaseModel):
    stop_sequence: int
    stop_name: str
    predicted_boardings: int
    predicted_alightings: int
    predicted_remaining_seats: int

class OccupancyPredictionRequest(BaseModel):
    trip_id: str
    total_capacity: int
    current_occupancy: int
    current_stop_sequence: int
    stops: List[dict]  # list of {sequence: int, name: str, historical_alight_ratio: float}

class OccupancyPredictionResponse(BaseModel):
    trip_id: str
    is_likely_to_fill_up_next_stop: bool
    predicted_end_of_line_occupancy: int
    stop_by_stop_forecast: List[StopOccupancyInference]

class OccupancyPredictorAI:
    @staticmethod
    def predict_occupancy(req: OccupancyPredictionRequest) -> OccupancyPredictionResponse:
        forecast = []
        current_seats = req.total_capacity - req.current_occupancy

        for stop in req.stops:
            seq = stop.get("sequence", 1)
            name = stop.get("name", "Stop")
            if seq <= req.current_stop_sequence:
                continue

            # In typical Indian shared auto corridors, major interchange stops have high alighting
            alight_ratio = stop.get("historical_alight_ratio", 0.3)
            alighting = int(req.current_occupancy * alight_ratio)
            boarding = 1 if current_seats > 0 else 0

            current_seats = min(req.total_capacity, max(0, current_seats + alighting - boarding))

            forecast.append(StopOccupancyInference(
                stop_sequence=seq,
                stop_name=name,
                predicted_boardings=boarding,
                predicted_alightings=alighting,
                predicted_remaining_seats=current_seats
            ))

        next_stop_fill = (forecast[0].predicted_remaining_seats == 0) if forecast else False

        return OccupancyPredictionResponse(
            trip_id=req.trip_id,
            is_likely_to_fill_up_next_stop=next_stop_fill,
            predicted_end_of_line_occupancy=req.total_capacity - (forecast[-1].predicted_remaining_seats if forecast else 0),
            stop_by_stop_forecast=forecast
        )
