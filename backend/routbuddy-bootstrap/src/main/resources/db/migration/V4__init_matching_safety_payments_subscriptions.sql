-- ========================================================
-- Flyway Migration V4: Commute Matching, Safety SOS, Payments, Subscriptions, Ratings, and Complaints Schema
-- ========================================================

-- 1. Commute Profiles Table (with PostGIS Geometry & Geohash)
CREATE TABLE IF NOT EXISTS commute_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    commute_type VARCHAR(30) NOT NULL,
    home_geohash VARCHAR(8) NOT NULL,
    home_geom geometry(Point, 4326) NOT NULL,
    home_neighborhood_label VARCHAR(100),
    dest_geom geometry(Point, 4326) NOT NULL,
    dest_name VARCHAR(150) NOT NULL,
    departure_time_start TIME NOT NULL,
    departure_time_end TIME NOT NULL,
    return_time_start TIME,
    return_time_end TIME,
    travel_days VARCHAR(50) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI',
    gender_preference VARCHAR(30) NOT NULL DEFAULT 'ANY',
    language_preference VARCHAR(50) DEFAULT 'English,Hindi',
    plan_type VARCHAR(30) NOT NULL DEFAULT 'OFFICE_COMMUTE',
    vehicle_model VARCHAR(100),
    seats_offered INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_commute_user_id ON commute_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_commute_type ON commute_profiles(commute_type);
CREATE INDEX IF NOT EXISTS idx_commute_geohash ON commute_profiles(home_geohash);
CREATE INDEX IF NOT EXISTS idx_commute_is_active ON commute_profiles(is_active);
CREATE INDEX IF NOT EXISTS idx_commute_home_geom ON commute_profiles USING GIST (home_geom);
CREATE INDEX IF NOT EXISTS idx_commute_dest_geom ON commute_profiles USING GIST (dest_geom);

-- 2. Commute Matches Table
CREATE TABLE IF NOT EXISTS commute_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    requester_profile_id UUID NOT NULL REFERENCES commute_profiles(id) ON DELETE CASCADE,
    partner_profile_id UUID NOT NULL REFERENCES commute_profiles(id) ON DELETE CASCADE,
    match_score DOUBLE PRECISION NOT NULL,
    route_overlap_ratio DOUBLE PRECISION NOT NULL,
    schedule_delta_minutes INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUGGESTED',
    chat_channel_id VARCHAR(100),
    unlocked_at TIMESTAMP WITH TIME ZONE,
    request_message VARCHAR(255),
    rejection_reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_matches_requester ON commute_matches(requester_profile_id);
CREATE INDEX IF NOT EXISTS idx_matches_partner ON commute_matches(partner_profile_id);
CREATE INDEX IF NOT EXISTS idx_matches_status ON commute_matches(status);

-- 3. Safety Incidents Table (SOS Pipeline)
CREATE TABLE IF NOT EXISTS safety_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trip_id UUID REFERENCES trips(id) ON DELETE SET NULL,
    incident_type VARCHAR(30) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location_description VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'TRIGGERED',
    emergency_contacts_notified BOOLEAN NOT NULL DEFAULT FALSE,
    police_notified BOOLEAN NOT NULL DEFAULT FALSE,
    admin_notes VARCHAR(1000),
    resolved_by_admin_id UUID REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_incidents_user_id ON safety_incidents(user_id);
CREATE INDEX IF NOT EXISTS idx_incidents_trip_id ON safety_incidents(trip_id);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON safety_incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_type ON safety_incidents(incident_type);

-- 4. Payment Transactions Table
CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    subscription_id UUID,
    amount_inr DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    gateway_signature VARCHAR(255),
    upi_vpa VARCHAR(100),
    failure_reason VARCHAR(500),
    refund_amount_inr DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payment_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payment_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payment_transactions(payment_status);

-- 5. Commute Passes Table (Subscriptions)
CREATE TABLE IF NOT EXISTS commute_passes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type VARCHAR(30) NOT NULL,
    pass_code VARCHAR(30) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_rides_allowed INT NOT NULL,
    rides_used INT NOT NULL DEFAULT 0,
    price_inr DOUBLE PRECISION NOT NULL,
    discount_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    qr_pass_token VARCHAR(512)
);

CREATE INDEX IF NOT EXISTS idx_passes_user_id ON commute_passes(user_id);
CREATE INDEX IF NOT EXISTS idx_passes_is_active ON commute_passes(is_active);
CREATE INDEX IF NOT EXISTS idx_passes_pass_code ON commute_passes(pass_code);

-- 6. Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    reviewer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trip_id UUID REFERENCES trips(id) ON DELETE SET NULL,
    commute_match_id UUID REFERENCES commute_matches(id) ON DELETE SET NULL,
    score INT NOT NULL,
    feedback_text VARCHAR(500),
    punctuality_score INT,
    safety_score INT,
    cleanliness_score INT
);

CREATE INDEX IF NOT EXISTS idx_ratings_target ON ratings(target_user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_trip ON ratings(trip_id);
CREATE INDEX IF NOT EXISTS idx_ratings_reviewer ON ratings(reviewer_id);

-- 7. Complaints Table
CREATE TABLE IF NOT EXISTS complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    trip_id UUID REFERENCES trips(id) ON DELETE SET NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    admin_action_taken VARCHAR(500),
    resolved_by_admin_id UUID REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_complaints_user_id ON complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_complaints_reported_user ON complaints(reported_user_id);
