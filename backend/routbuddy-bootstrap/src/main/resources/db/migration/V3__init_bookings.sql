-- ========================================================
-- Flyway Migration V3: Bookings Schema
-- ========================================================

CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    idempotency_key VARCHAR(64) UNIQUE,
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE RESTRICT,
    passenger_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pickup_stop_id UUID NOT NULL REFERENCES route_stops(id) ON DELETE RESTRICT,
    pickup_stop_name VARCHAR(100) NOT NULL,
    dropoff_stop_id UUID NOT NULL REFERENCES route_stops(id) ON DELETE RESTRICT,
    dropoff_stop_name VARCHAR(100) NOT NULL,
    seat_count INT NOT NULL DEFAULT 1,
    fare_amount_inr DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'UPI_INTENT',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    booking_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    qr_token VARCHAR(512),
    otp_code VARCHAR(6),
    boarded_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_bookings_code ON bookings(booking_code);
CREATE INDEX IF NOT EXISTS idx_bookings_idempotency ON bookings(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX IF NOT EXISTS idx_bookings_passenger_id ON bookings(passenger_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(booking_status);
CREATE INDEX IF NOT EXISTS idx_bookings_passenger_created ON bookings(passenger_id, created_at DESC);
