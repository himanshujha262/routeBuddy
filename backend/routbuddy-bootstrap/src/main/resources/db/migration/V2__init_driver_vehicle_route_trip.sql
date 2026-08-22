-- ========================================================
-- Flyway Migration V2: Driver, Vehicle, Route, RouteStop & Trip Schema
-- ========================================================

-- 1. Driver Profiles Table
CREATE TABLE IF NOT EXISTS driver_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    license_number VARCHAR(30) NOT NULL UNIQUE,
    license_expiry_date DATE,
    license_front_image_url VARCHAR(500),
    license_back_image_url VARCHAR(500),
    aadhaar_masked VARCHAR(20),
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    kyc_rejection_reason VARCHAR(500),
    is_police_verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating_avg DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    total_ratings_count INT NOT NULL DEFAULT 0,
    total_trips_completed INT NOT NULL DEFAULT 0,
    total_earnings_inr DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    wallet_balance_inr DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    current_vehicle_id UUID,
    active_route_id UUID
);

CREATE INDEX IF NOT EXISTS idx_drivers_user_id ON driver_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_drivers_kyc_status ON driver_profiles(kyc_status);
CREATE INDEX IF NOT EXISTS idx_drivers_is_online ON driver_profiles(is_online);

-- 2. Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    driver_id UUID NOT NULL REFERENCES driver_profiles(id) ON DELETE CASCADE,
    plate_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL,
    model_name VARCHAR(50),
    seating_capacity INT NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,
    rc_number VARCHAR(30),
    permit_number VARCHAR(30),
    permit_expiry_date DATE,
    insurance_expiry_date DATE,
    is_electric BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    vehicle_photo_url VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_vehicles_driver_id ON vehicles(driver_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_plate_number ON vehicles(plate_number);

-- 3. Routes Table (with PostGIS Geometry)
CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(150) NOT NULL,
    origin_name VARCHAR(100) NOT NULL,
    destination_name VARCHAR(100) NOT NULL,
    origin_geom geometry(Point, 4326) NOT NULL,
    destination_geom geometry(Point, 4326) NOT NULL,
    corridor_path geometry(LineString, 4326),
    base_fare_inr DOUBLE PRECISION NOT NULL,
    total_distance_km DOUBLE PRECISION NOT NULL,
    estimated_duration_min INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_routes_name ON routes(name);
CREATE INDEX IF NOT EXISTS idx_routes_is_active ON routes(is_active);
CREATE INDEX IF NOT EXISTS idx_routes_origin_geom ON routes USING GIST (origin_geom);
CREATE INDEX IF NOT EXISTS idx_routes_destination_geom ON routes USING GIST (destination_geom);

-- 4. Route Stops Table
CREATE TABLE IF NOT EXISTS route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    stop_name VARCHAR(100) NOT NULL,
    sequence_order INT NOT NULL,
    stop_geom geometry(Point, 4326) NOT NULL,
    distance_from_origin_km DOUBLE PRECISION NOT NULL,
    stage_fare_inr DOUBLE PRECISION NOT NULL,
    geofence_radius_meters INT NOT NULL DEFAULT 50
);

CREATE INDEX IF NOT EXISTS idx_route_stops_route_id ON route_stops(route_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_sequence ON route_stops(route_id, sequence_order);
CREATE INDEX IF NOT EXISTS idx_route_stops_geom ON route_stops USING GIST (stop_geom);

-- 5. Trips Table
CREATE TABLE IF NOT EXISTS trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    driver_id UUID NOT NULL REFERENCES driver_profiles(id) ON DELETE RESTRICT,
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE RESTRICT,
    scheduled_departure TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_departure TIMESTAMP WITH TIME ZONE,
    actual_arrival TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    fare_per_seat_inr DOUBLE PRECISION NOT NULL,
    live_latitude DOUBLE PRECISION,
    live_longitude DOUBLE PRECISION,
    live_heading DOUBLE PRECISION,
    live_speed_kmph DOUBLE PRECISION,
    last_telemetry_ping_at TIMESTAMP WITH TIME ZONE,
    current_stop_sequence INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_trips_driver_id ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_route_id ON trips(route_id);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trips_scheduled_departure ON trips(scheduled_departure);

-- 6. Driver Earnings Ledger Table
CREATE TABLE IF NOT EXISTS driver_earnings_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    driver_id UUID NOT NULL REFERENCES driver_profiles(id) ON DELETE CASCADE,
    trip_id UUID REFERENCES trips(id) ON DELETE SET NULL,
    amount_inr DOUBLE PRECISION NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    running_balance_inr DOUBLE PRECISION NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ledger_driver_id ON driver_earnings_ledger(driver_id);
CREATE INDEX IF NOT EXISTS idx_ledger_trip_id ON driver_earnings_ledger(trip_id);
