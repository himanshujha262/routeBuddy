package com.routbuddy.drivers.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "driver_earnings_ledger", indexes = {
        @Index(name = "idx_ledger_driver_id", columnList = "driver_id"),
        @Index(name = "idx_ledger_trip_id", columnList = "trip_id")
})
public class DriverEarningsLedger extends BaseEntity {

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "amount_inr", nullable = false)
    private double amountInr;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType; // TRIP_EARNING, PLATFORM_FEE, WITHDRAWAL, BONUS

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "running_balance_inr", nullable = false)
    private double runningBalanceInr;

    public DriverEarningsLedger() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID driverId;
        private UUID tripId;
        private double amountInr;
        private String transactionType;
        private String description;
        private double runningBalanceInr;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder driverId(UUID driverId) { this.driverId = driverId; return this; }
        public Builder tripId(UUID tripId) { this.tripId = tripId; return this; }
        public Builder amountInr(double amountInr) { this.amountInr = amountInr; return this; }
        public Builder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder runningBalanceInr(double runningBalanceInr) { this.runningBalanceInr = runningBalanceInr; return this; }

        public DriverEarningsLedger build() {
            DriverEarningsLedger ledger = new DriverEarningsLedger();
            if (id != null) ledger.setId(id);
            ledger.setDriverId(driverId);
            ledger.setTripId(tripId);
            ledger.setAmountInr(amountInr);
            ledger.setTransactionType(transactionType);
            ledger.setDescription(description);
            ledger.setRunningBalanceInr(runningBalanceInr);
            return ledger;
        }
    }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }

    public double getAmountInr() { return amountInr; }
    public void setAmountInr(double amountInr) { this.amountInr = amountInr; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRunningBalanceInr() { return runningBalanceInr; }
    public void setRunningBalanceInr(double runningBalanceInr) { this.runningBalanceInr = runningBalanceInr; }
}
