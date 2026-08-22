package com.routbuddy.bookings.dto;

import com.routbuddy.common.domain.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateBookingRequest {

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotNull(message = "Pickup stop ID is required")
    private UUID pickupStopId;

    @NotNull(message = "Dropoff stop ID is required")
    private UUID dropoffStopId;

    @NotNull(message = "Seat count is required")
    @Min(value = 1, message = "Seat count must be at least 1")
    private Integer seatCount = 1;

    private PaymentMethod paymentMethod = PaymentMethod.UPI_INTENT;

    private String idempotencyKey;

    public CreateBookingRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID tripId;
        private UUID pickupStopId;
        private UUID dropoffStopId;
        private Integer seatCount = 1;
        private PaymentMethod paymentMethod = PaymentMethod.UPI_INTENT;
        private String idempotencyKey;

        public Builder tripId(UUID tripId) { this.tripId = tripId; return this; }
        public Builder pickupStopId(UUID pickupStopId) { this.pickupStopId = pickupStopId; return this; }
        public Builder dropoffStopId(UUID dropoffStopId) { this.dropoffStopId = dropoffStopId; return this; }
        public Builder seatCount(Integer seatCount) { this.seatCount = seatCount; return this; }
        public Builder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }

        public CreateBookingRequest build() {
            CreateBookingRequest req = new CreateBookingRequest();
            req.tripId = tripId;
            req.pickupStopId = pickupStopId;
            req.dropoffStopId = dropoffStopId;
            req.seatCount = seatCount != null ? seatCount : 1;
            req.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.UPI_INTENT;
            req.idempotencyKey = idempotencyKey;
            return req;
        }
    }

    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }

    public UUID getPickupStopId() { return pickupStopId; }
    public void setPickupStopId(UUID pickupStopId) { this.pickupStopId = pickupStopId; }

    public UUID getDropoffStopId() { return dropoffStopId; }
    public void setDropoffStopId(UUID dropoffStopId) { this.dropoffStopId = dropoffStopId; }

    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
