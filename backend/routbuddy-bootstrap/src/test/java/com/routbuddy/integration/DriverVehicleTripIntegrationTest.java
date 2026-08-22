package com.routbuddy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routbuddy.auth.dto.RegisterRequest;
import com.routbuddy.auth.repository.OtpVerificationRepository;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.domain.enums.VehicleType;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.routes.dto.CreateRouteRequest;
import com.routbuddy.routes.dto.CreateRouteStopRequest;
import com.routbuddy.routes.repository.RouteRepository;
import com.routbuddy.routes.repository.RouteStopRepository;
import com.routbuddy.trips.dto.CreateTripRequest;
import com.routbuddy.trips.dto.UpdateTelemetryRequest;
import com.routbuddy.trips.repository.TripRepository;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
import com.routbuddy.vehicles.dto.RegisterVehicleRequest;
import com.routbuddy.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverVehicleTripIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private TripRepository tripRepository;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeStopRepository.deleteAll();
        routeRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        otpVerificationRepository.deleteAll();
        userRepository.deleteAll();

        // Ensure roles exist
        for (UserRole role : UserRole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(Role.builder().name(role).description("Role " + role).build());
            }
        }
    }

    @Test
    @DisplayName("End-to-End Integration: Driver Onboarding -> Vehicle Registration -> Route & Stop Creation -> Trip Lifecycle & State Machine")
    void testCompleteDriverVehicleTripLifecycle() throws Exception {
        // ========================================================
        // 1. Register Admin and Driver Users
        // ========================================================
        RegisterRequest adminReq = RegisterRequest.builder()
                .phone("9800011111")
                .fullName("Platform Admin")
                .password("adminSecret123")
                .role(UserRole.ADMIN)
                .build();

        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        RegisterRequest driverReq = RegisterRequest.builder()
                .phone("9800022222")
                .fullName("Ramesh Kumar")
                .password("driverSecret123")
                .role(UserRole.DRIVER)
                .build();

        MvcResult driverResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String driverToken = objectMapper.readTree(driverResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // ========================================================
        // 2. Driver Onboarding / License Submission
        // ========================================================
        RegisterDriverRequest driverProfileReq = RegisterDriverRequest.builder()
                .licenseNumber("DL-0420110098765")
                .licenseExpiryDate(LocalDate.now().plusYears(5))
                .licenseFrontImageUrl("https://cdn.routbuddy.com/licenses/front.jpg")
                .licenseBackImageUrl("https://cdn.routbuddy.com/licenses/back.jpg")
                .aadhaarMasked("XXXX-XXXX-9999")
                .build();

        MvcResult driverProfileResult = mockMvc.perform(post("/api/v1/drivers/register")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverProfileReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.kycStatus", is("PENDING")))
                .andExpect(jsonPath("$.data.licenseNumber", is("DL-0420110098765")))
                .andReturn();

        String driverId = objectMapper.readTree(driverProfileResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Verify going online fails while KYC is PENDING
        mockMvc.perform(put("/api/v1/drivers/me/status?online=true")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("KYC must be APPROVED")));

        // ========================================================
        // 3. Admin Approves Driver KYC
        // ========================================================
        UpdateKycRequest approveKycReq = new UpdateKycRequest(KYCStatus.APPROVED, null);

        mockMvc.perform(put("/api/v1/drivers/" + driverId + "/kyc")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveKycReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kycStatus", is("APPROVED")));

        // Driver can now go online
        mockMvc.perform(put("/api/v1/drivers/me/status?online=true")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.online", is(true)));

        // ========================================================
        // 4. Driver Registers Vehicle & Admin Approves
        // ========================================================
        RegisterVehicleRequest vehicleReq = RegisterVehicleRequest.builder()
                .plateNumber("UP-16-CD-5678")
                .vehicleType(VehicleType.AUTO_3W)
                .modelName("Bajaj Compact Auto")
                .seatingCapacity(3)
                .fuelType(FuelType.CNG)
                .build();

        MvcResult vehicleResult = mockMvc.perform(post("/api/v1/vehicles/register")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.approved", is(false)))
                .andReturn();

        String vehicleId = objectMapper.readTree(vehicleResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Admin approves vehicle
        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved", is(true)));

        // ========================================================
        // 5. Admin Creates Route with Sequential Stops
        // ========================================================
        CreateRouteRequest routeReq = CreateRouteRequest.builder()
                .name("Noida Sec 62 to Botanical Garden")
                .originName("Noida Electronic City")
                .destinationName("Botanical Garden Metro")
                .originLatitude(28.6280)
                .originLongitude(77.3730)
                .destinationLatitude(28.5645)
                .destinationLongitude(77.3345)
                .baseFareInr(40.0)
                .totalDistanceKm(9.5)
                .estimatedDurationMin(25)
                .stops(List.of(
                        new CreateRouteStopRequest("Noida Sec 62", 1, 28.6280, 77.3730, 0.0, 10.0, 50),
                        new CreateRouteStopRequest("Noida Sec 59", 2, 28.6120, 77.3620, 2.5, 20.0, 50),
                        new CreateRouteStopRequest("Noida City Centre", 3, 28.5740, 77.3560, 6.0, 30.0, 50),
                        new CreateRouteStopRequest("Botanical Garden", 4, 28.5645, 77.3345, 9.5, 40.0, 50)
                ))
                .build();

        MvcResult routeResult = mockMvc.perform(post("/api/v1/routes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Noida Sec 62 to Botanical Garden")))
                .andExpect(jsonPath("$.data.stops", hasSize(4)))
                .andReturn();

        String routeId = objectMapper.readTree(routeResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // ========================================================
        // 6. Trip Creation & Capacity Validation
        // ========================================================
        // Attempting to create trip with capacity (5) > vehicle capacity (3) -> 400 Bad Request
        CreateTripRequest invalidCapacityTrip = CreateTripRequest.builder()
                .vehicleId(UUID.fromString(vehicleId))
                .routeId(UUID.fromString(routeId))
                .scheduledDeparture(Instant.now().plusSeconds(1800))
                .totalSeats(5)
                .farePerSeatInr(40.0)
                .build();

        mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCapacityTrip)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exceeds vehicle seating capacity")));

        // Valid trip creation
        CreateTripRequest validTripReq = CreateTripRequest.builder()
                .vehicleId(UUID.fromString(vehicleId))
                .routeId(UUID.fromString(routeId))
                .scheduledDeparture(Instant.now().plusSeconds(1800))
                .totalSeats(3)
                .farePerSeatInr(40.0)
                .build();

        MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTripReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("SCHEDULED")))
                .andExpect(jsonPath("$.data.availableSeats", is(3)))
                .andReturn();

        String tripId = objectMapper.readTree(tripResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // ========================================================
        // 7. Trip State Machine Progression:
        //    SCHEDULED -> PUBLISHED -> BOARDING -> IN_TRANSIT -> COMPLETED
        // ========================================================

        // 7.1 Publish Trip
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/publish")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PUBLISHED")));

        // 7.2 Start Boarding
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/start-boarding")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("BOARDING")));

        // 7.3 Start Trip (Depart)
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/start-trip")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_TRANSIT")))
                .andExpect(jsonPath("$.data.actualDeparture", notNullValue()));

        // 7.4 Live Telemetry Update
        UpdateTelemetryRequest telemetryReq = UpdateTelemetryRequest.builder()
                .latitude(28.6120)
                .longitude(77.3620)
                .speedKmph(32.5)
                .heading(180.0)
                .currentStopSequence(2)
                .build();

        mockMvc.perform(put("/api/v1/trips/" + tripId + "/telemetry")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(telemetryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStopSequence", is(2)))
                .andExpect(jsonPath("$.data.liveSpeedKmph", is(32.5)));

        // 7.5 Complete Trip
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/complete")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.actualArrival", notNullValue()));

        // ========================================================
        // 8. Reject Illegal State Transition after Completion
        // ========================================================
        mockMvc.perform(put("/api/v1/trips/" + tripId + "/start-boarding")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Illegal trip state transition")));
    }
}
