package com.routbuddy.bookings.dto;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.domain.enums.PaymentMethod;
import com.routbuddy.common.domain.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public class BookingDto {

    private UUID id;
    private String bookingCode;
    private String idempotencyKey;
    private UUID tripId;
    private UUID passengerId;
    private UUID pickupStopId;
    private String pickupStopName;
    private UUID dropoffStopId;
    private String dropoffStopName;
    private int seatCount;
    private double fareAmountInr;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BookingStatus bookingStatus;
    private String qrToken;
    private String otpCode;
    private Instant boardedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;
    private Instant createdAt;

    public BookingDto() {}

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
        private int seatCount;
        private double fareAmountInr;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;
        private BookingStatus bookingStatus;
        private String qrToken;
        private String otpCode;
        private Instant boardedAt;
        private Instant completedAt;
        private Instant cancelledAt;
        private String cancellationReason;
        private Instant createdAt;

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
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public BookingDto build() {
            BookingDto dto = new BookingDto();
            dto.id = id;
            dto.bookingCode = bookingCode;
            dto.idempotencyKey = idempotencyKey;
            dto.tripId = tripId;
            dto.passengerId = passengerId;
            dto.pickupStopId = pickupStopId;
            dto.pickupStopName = pickupStopName;
            dto.dropoffStopId = dropoffStopId;
            dto.dropoffStopName = dropoffStopName;
            dto.seatCount = seatCount;
            dto.fareAmountInr = fareAmountInr;
            dto.paymentMethod = paymentMethod;
            dto.paymentStatus = paymentStatus;
            dto.bookingStatus = bookingStatus;
            dto.qrToken = qrToken;
            dto.otpCode = otpCode;
            dto.boardedAt = boardedAt;
            dto.completedAt = completedAt;
            dto.cancelledAt = cancelledAt;
            dto.cancellationReason = cancellationReason;
            dto.createdAt = createdAt;
            return dto;
        }
    }

    public static BookingDto fromEntity(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .idempotencyKey(booking.getIdempotencyKey())
                .tripId(booking.getTripId())
                .passengerId(booking.getPassengerId())
                .pickupStopId(booking.getPickupStopId())
                .pickupStopName(booking.getPickupStopName())
                .dropoffStopId(booking.getDropoffStopId())
                .dropoffStopName(booking.getDropoffStopName())
                .seatCount(booking.getSeatCount())
                .fareAmountInr(booking.getFareAmountInr())
                .paymentMethod(booking.getPaymentMethod())
                .paymentStatus(booking.getPaymentStatus())
                .bookingStatus(booking.getBookingStatus())
                .qrToken(booking.getQrToken())
                .otpCode(booking.getOtpCode())
                .boardedAt(booking.getBoardedAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
