package com.routbuddy.bookings.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.domain.enums.PaymentMethod;
import com.routbuddy.common.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_code", columnList = "booking_code", unique = true),
        @Index(name = "idx_bookings_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_bookings_trip_id", columnList = "trip_id"),
        @Index(name = "idx_bookings_passenger_id", columnList = "passenger_id"),
        @Index(name = "idx_bookings_status", columnList = "booking_status")
})
public class Booking extends BaseEntity {

    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @Column(name = "idempotency_key", unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "pickup_stop_id", nullable = false)
    private UUID pickupStopId;

    @Column(name = "pickup_stop_name", nullable = false, length = 100)
    private String pickupStopName;

    @Column(name = "dropoff_stop_id", nullable = false)
    private UUID dropoffStopId;

    @Column(name = "dropoff_stop_name", nullable = false, length = 100)
    private String dropoffStopName;

    @Column(name = "seat_count", nullable = false)
    private int seatCount = 1;

    @Column(name = "fare_amount_inr", nullable = false)
    private double fareAmountInr;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.UPI_INTENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 30)
    private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

    @Column(name = "qr_token", length = 512)
    private String qrToken;

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "boarded_at")
    private Instant boardedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    public Booking() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String bookingCode;
        private String idempotencyKey;
        private UUID tripId;
        private UUID passengerId;
        private UUID pickupStopId;
        private String pickupStopName;
        private UUID dropoffStopId;
        private String dropoffStopName;
        private int seatCount = 1;
        private double fareAmountInr;
        private PaymentMethod paymentMethod = PaymentMethod.UPI_INTENT;
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;
        private BookingStatus bookingStatus = BookingStatus.CONFIRMED;
        private String qrToken;
        private String otpCode;
        private Instant boardedAt;
        private Instant completedAt;
        private Instant cancelledAt;
        private String cancellationReason;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder bookingCode(String bookingCode) { this.bookingCode = bookingCode; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder tripId(UUID tripId) { this.tripId = tripId; return this; }
        public Builder passengerId(UUID passengerId) { this.passengerId = passengerId; return this; }
        public Builder pickupStopId(UUID pickupStopId) { this.pickupStopId = pickupStopId; return this; }
        public Builder pickupStopName(String pickupStopName) { this.pickupStopName = pickupStopName; return this; }
        public Builder dropoffStopId(UUID dropoffStopId) { this.dropoffStopId = dropoffStopId; return this; }
        public Builder dropoffStopName(String dropoffStopName) { this.dropoffStopName = dropoffStopName; return this; }
        public Builder seatCount(int seatCount) { this.seatCount = seatCount; return this; }
        public Builder fareAmountInr(double fareAmountInr) { this.fareAmountInr = fareAmountInr; return this; }
        public Builder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public Builder bookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; return this; }
        public Builder qrToken(String qrToken) { this.qrToken = qrToken; return this; }
        public Builder otpCode(String otpCode) { this.otpCode = otpCode; return this; }
        public Builder boardedAt(Instant boardedAt) { this.boardedAt = boardedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder cancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; return this; }
        public Builder cancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; return this; }

        public Booking build() {
            Booking b = new Booking();
            if (id != null) b.setId(id);
            b.setBookingCode(bookingCode);
            b.setIdempotencyKey(idempotencyKey);
            b.setTripId(tripId);
            b.setPassengerId(passengerId);
            b.setPickupStopId(pickupStopId);
            b.setPickupStopName(pickupStopName);
            b.setDropoffStopId(dropoffStopId);
            b.setDropoffStopName(dropoffStopName);
            b.setSeatCount(seatCount);
            b.setFareAmountInr(fareAmountInr);
            b.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.UPI_INTENT);
            b.setPaymentStatus(paymentStatus != null ? paymentStatus : PaymentStatus.PENDING);
            b.setBookingStatus(bookingStatus != null ? bookingStatus : BookingStatus.CONFIRMED);
            b.setQrToken(qrToken);
            b.setOtpCode(otpCode);
            b.setBoardedAt(boardedAt);
            b.setCompletedAt(completedAt);
            b.setCancelledAt(cancelledAt);
            b.setCancellationReason(cancellationReason);
            return b;
        }
    }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }

    public UUID getPassengerId() { return passengerId; }
    public void setPassengerId(UUID passengerId) { this.passengerId = passengerId; }

    public UUID getPickupStopId() { return pickupStopId; }
    public void setPickupStopId(UUID pickupStopId) { this.pickupStopId = pickupStopId; }

    public String getPickupStopName() { return pickupStopName; }
    public void setPickupStopName(String pickupStopName) { this.pickupStopName = pickupStopName; }

    public UUID getDropoffStopId() { return dropoffStopId; }
    public void setDropoffStopId(UUID dropoffStopId) { this.dropoffStopId = dropoffStopId; }

    public String getDropoffStopName() { return dropoffStopName; }
    public void setDropoffStopName(String dropoffStopName) { this.dropoffStopName = dropoffStopName; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public double getFareAmountInr() { return fareAmountInr; }
    public void setFareAmountInr(double fareAmountInr) { this.fareAmountInr = fareAmountInr; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Instant getBoardedAt() { return boardedAt; }
    public void setBoardedAt(Instant boardedAt) { this.boardedAt = boardedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
}
