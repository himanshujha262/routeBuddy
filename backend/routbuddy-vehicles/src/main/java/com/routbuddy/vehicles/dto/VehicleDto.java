package com.routbuddy.vehicles.dto;

import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.VehicleType;
import com.routbuddy.vehicles.domain.entity.Vehicle;

import java.time.LocalDate;
import java.util.UUID;

public class VehicleDto {
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
    private boolean electric;
    private boolean approved;
    private String vehiclePhotoUrl;

    public VehicleDto() {}

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
        private boolean electric;
        private boolean approved;
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

        public VehicleDto build() {
            VehicleDto dto = new VehicleDto();
            dto.id = id;
            dto.driverId = driverId;
            dto.plateNumber = plateNumber;
            dto.vehicleType = vehicleType;
            dto.modelName = modelName;
            dto.seatingCapacity = seatingCapacity;
            dto.fuelType = fuelType;
            dto.rcNumber = rcNumber;
            dto.permitNumber = permitNumber;
            dto.permitExpiryDate = permitExpiryDate;
            dto.insuranceExpiryDate = insuranceExpiryDate;
            dto.electric = electric;
            dto.approved = approved;
            dto.vehiclePhotoUrl = vehiclePhotoUrl;
            return dto;
        }
    }

    public static VehicleDto fromEntity(Vehicle vehicle) {
        return VehicleDto.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriverId())
                .plateNumber(vehicle.getPlateNumber())
                .vehicleType(vehicle.getVehicleType())
                .modelName(vehicle.getModelName())
                .seatingCapacity(vehicle.getSeatingCapacity())
                .fuelType(vehicle.getFuelType())
                .rcNumber(vehicle.getRcNumber())
                .permitNumber(vehicle.getPermitNumber())
                .permitExpiryDate(vehicle.getPermitExpiryDate())
                .insuranceExpiryDate(vehicle.getInsuranceExpiryDate())
                .electric(vehicle.isElectric())
                .approved(vehicle.isApproved())
                .vehiclePhotoUrl(vehicle.getVehiclePhotoUrl())
                .build();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
