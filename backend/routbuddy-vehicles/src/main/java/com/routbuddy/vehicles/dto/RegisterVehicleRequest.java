package com.routbuddy.vehicles.dto;

import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RegisterVehicleRequest {

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Model name is required")
    private String modelName;

    @NotNull(message = "Seating capacity is required")
    @Min(value = 1, message = "Seating capacity must be at least 1")
    private Integer seatingCapacity;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    private String rcNumber;
    private String permitNumber;
    private LocalDate permitExpiryDate;
    private LocalDate insuranceExpiryDate;
    private boolean electric = false;
    private String vehiclePhotoUrl;

    public RegisterVehicleRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String plateNumber;
        private VehicleType vehicleType;
        private String modelName;
        private Integer seatingCapacity;
        private FuelType fuelType;
        private String rcNumber;
        private String permitNumber;
        private LocalDate permitExpiryDate;
        private LocalDate insuranceExpiryDate;
        private boolean electric = false;
        private String vehiclePhotoUrl;

        public Builder plateNumber(String plateNumber) { this.plateNumber = plateNumber; return this; }
        public Builder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder seatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; return this; }
        public Builder fuelType(FuelType fuelType) { this.fuelType = fuelType; return this; }
        public Builder rcNumber(String rcNumber) { this.rcNumber = rcNumber; return this; }
        public Builder permitNumber(String permitNumber) { this.permitNumber = permitNumber; return this; }
        public Builder permitExpiryDate(LocalDate permitExpiryDate) { this.permitExpiryDate = permitExpiryDate; return this; }
        public Builder insuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; return this; }
        public Builder electric(boolean electric) { this.electric = electric; return this; }
        public Builder vehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; return this; }

        public RegisterVehicleRequest build() {
            RegisterVehicleRequest req = new RegisterVehicleRequest();
            req.plateNumber = plateNumber;
            req.vehicleType = vehicleType;
            req.modelName = modelName;
            req.seatingCapacity = seatingCapacity;
            req.fuelType = fuelType;
            req.rcNumber = rcNumber;
            req.permitNumber = permitNumber;
            req.permitExpiryDate = permitExpiryDate;
            req.insuranceExpiryDate = insuranceExpiryDate;
            req.electric = electric;
            req.vehiclePhotoUrl = vehiclePhotoUrl;
            return req;
        }
    }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }

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

    public String getVehiclePhotoUrl() { return vehiclePhotoUrl; }
    public void setVehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; }
}
