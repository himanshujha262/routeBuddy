package com.routbuddy.vehicles.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.VehicleType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicles_driver_id", columnList = "driver_id"),
        @Index(name = "idx_vehicles_plate_number", columnList = "plate_number", unique = true)
})
public class Vehicle extends BaseEntity {

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Column(name = "seating_capacity", nullable = false)
    private int seatingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    @Column(name = "rc_number", length = 30)
    private String rcNumber;

    @Column(name = "permit_number", length = 30)
    private String permitNumber;

    @Column(name = "permit_expiry_date")
    private LocalDate permitExpiryDate;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "is_electric", nullable = false)
    private boolean electric = false;

    @Column(name = "is_approved", nullable = false)
    private boolean approved = false;

    @Column(name = "vehicle_photo_url")
    private String vehiclePhotoUrl;

    public Vehicle() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID driverId;
        private String plateNumber;
        private VehicleType vehicleType;
        private String modelName;
        private int seatingCapacity;
        private FuelType fuelType;
        private String rcNumber;
        private String permitNumber;
        private LocalDate permitExpiryDate;
        private LocalDate insuranceExpiryDate;
        private boolean electric = false;
        private boolean approved = false;
        private String vehiclePhotoUrl;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder driverId(UUID driverId) { this.driverId = driverId; return this; }
        public Builder plateNumber(String plateNumber) { this.plateNumber = plateNumber; return this; }
        public Builder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder seatingCapacity(int seatingCapacity) { this.seatingCapacity = seatingCapacity; return this; }
        public Builder fuelType(FuelType fuelType) { this.fuelType = fuelType; return this; }
        public Builder rcNumber(String rcNumber) { this.rcNumber = rcNumber; return this; }
        public Builder permitNumber(String permitNumber) { this.permitNumber = permitNumber; return this; }
        public Builder permitExpiryDate(LocalDate permitExpiryDate) { this.permitExpiryDate = permitExpiryDate; return this; }
        public Builder insuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; return this; }
        public Builder electric(boolean electric) { this.electric = electric; return this; }
        public Builder approved(boolean approved) { this.approved = approved; return this; }
        public Builder vehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; return this; }

        public Vehicle build() {
            Vehicle v = new Vehicle();
            if (id != null) v.setId(id);
            v.setDriverId(driverId);
            v.setPlateNumber(plateNumber);
            v.setVehicleType(vehicleType);
            v.setModelName(modelName);
            v.setSeatingCapacity(seatingCapacity);
            v.setFuelType(fuelType);
            v.setRcNumber(rcNumber);
            v.setPermitNumber(permitNumber);
            v.setPermitExpiryDate(permitExpiryDate);
            v.setInsuranceExpiryDate(insuranceExpiryDate);
            v.setElectric(electric);
            v.setApproved(approved);
            v.setVehiclePhotoUrl(vehiclePhotoUrl);
            return v;
        }
    }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public int getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(int seatingCapacity) { this.seatingCapacity = seatingCapacity; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public String getRcNumber() { return rcNumber; }
    public void setRcNumber(String rcNumber) { this.rcNumber = rcNumber; }

    public String getPermitNumber() { return permitNumber; }
    public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }

    public LocalDate getPermitExpiryDate() { return permitExpiryDate; }
    public void setPermitExpiryDate(LocalDate permitExpiryDate) { this.permitExpiryDate = permitExpiryDate; }

    public LocalDate getInsuranceExpiryDate() { return insuranceExpiryDate; }
    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; }

    public boolean isElectric() { return electric; }
    public void setElectric(boolean electric) { this.electric = electric; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getVehiclePhotoUrl() { return vehiclePhotoUrl; }
    public void setVehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; }
}
