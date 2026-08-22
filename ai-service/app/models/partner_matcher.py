import math
from typing import List, Dict, Any
from pydantic import BaseModel

class CommuterCandidate(BaseModel):
    user_id: str
    full_name: str
    gender: str
    org_name: str | None = None
    org_email: str | None = None
    trust_score: float = 4.5
    origin_lat: float
    origin_lon: float
    dest_lat: float
    dest_lon: float
    departure_time_minutes: int  # e.g., 540 = 09:00 AM
    return_time_minutes: int | None = None
    travel_days: List[str]
    gender_preference: str = "ANY"
    language_preference: str = "English"
    plan_type: str = "OFFICE_COMMUTE"
    vehicle_model: str | None = None
    seats_offered: int = 1

class CommuteMatchResult(BaseModel):
    partner_user_id: str
    partner_name: str
    composite_match_score: float  # 0.0 to 100.0%
    route_similarity_score: float
    schedule_overlap_score: float
    organization_match_score: float
    trust_score_weight: float
    estimated_pickup_distance_km: float
    estimated_destination_distance_km: float
    schedule_delta_minutes: int
    shared_travel_days: List[str]
    match_tier: str  # "PERFECT_MATCH", "HIGH_COMPATIBILITY", "MODERATE"

def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6371.0
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (math.sin(dlat / 2) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
         math.sin(dlon / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

class PartnerMatcherAI:
    @staticmethod
    def calculate_match(target: CommuterCandidate, candidate: CommuterCandidate) -> CommuteMatchResult | None:
        # 1. Hard Gender Filter Check
        if target.gender_preference == "SAME_GENDER_ONLY" and target.gender.upper() != candidate.gender.upper():
            return None
        if candidate.gender_preference == "SAME_GENDER_ONLY" and candidate.gender.upper() != target.gender.upper():
            return None

        # 2. Origin & Destination Proximity
        origin_dist = haversine_km(target.origin_lat, target.origin_lon, candidate.origin_lat, candidate.origin_lon)
        dest_dist = haversine_km(target.dest_lat, target.dest_lon, candidate.dest_lat, candidate.dest_lon)

        # Discard if origin > 5km or destination > 3km
        if origin_dist > 5.0 or dest_dist > 3.0:
            return None

        # Route similarity score (exponential decay over distance)
        s_origin = math.exp(-origin_dist / 2.0)
        s_dest = math.exp(-dest_dist / 1.5)
        s_route = (0.5 * s_origin + 0.5 * s_dest)

        # 3. Schedule Compatibility
        time_delta = abs(target.departure_time_minutes - candidate.departure_time_minutes)
        if time_delta > 60:  # Max 1 hour deviation
            return None
        s_time = max(0.0, 1.0 - (time_delta / 60.0))

        # 4. Travel Days Overlap
        shared_days = list(set(target.travel_days).intersection(set(candidate.travel_days)))
        if not shared_days:
            return None
        day_overlap_ratio = len(shared_days) / max(len(target.travel_days), 1)

        # 5. Organization Compatibility
        s_org = 0.0
        if target.org_name and candidate.org_name:
            if target.org_name.lower().strip() == candidate.org_name.lower().strip():
                s_org = 1.0
            elif target.org_email and candidate.org_email:
                target_domain = target.org_email.split("@")[-1]
                cand_domain = candidate.org_email.split("@")[-1]
                if target_domain == cand_domain:
                    s_org = 1.0

        # 6. Trust & Rating
        s_trust = min(1.0, candidate.trust_score / 5.0)

        # 7. Composite Weighted Score
        # Weights: Route: 35%, Schedule: 25%, Days: 15%, Org: 15%, Trust: 10%
        composite = (
            0.35 * s_route +
            0.25 * s_time +
            0.15 * day_overlap_ratio +
            0.15 * s_org +
            0.10 * s_trust
        )

        match_score_pct = round(composite * 100.0, 1)

        if match_score_pct >= 85.0:
            tier = "PERFECT_MATCH"
        elif match_score_pct >= 70.0:
            tier = "HIGH_COMPATIBILITY"
        else:
            tier = "MODERATE"

        return CommuteMatchResult(
            partner_user_id=candidate.user_id,
            partner_name=candidate.full_name,
            composite_match_score=match_score_pct,
            route_similarity_score=round(s_route * 100, 1),
            schedule_overlap_score=round(s_time * 100, 1),
            organization_match_score=round(s_org * 100, 1),
            trust_score_weight=round(s_trust * 100, 1),
            estimated_pickup_distance_km=round(origin_dist, 2),
            estimated_destination_distance_km=round(dest_dist, 2),
            schedule_delta_minutes=time_delta,
            shared_travel_days=shared_days,
            match_tier=tier
        )
