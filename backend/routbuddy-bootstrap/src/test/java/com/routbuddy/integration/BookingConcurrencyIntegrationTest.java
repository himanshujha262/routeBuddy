package com.routbuddy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routbuddy.auth.dto.AuthResponse;
import com.routbuddy.auth.dto.RegisterRequest;
import com.routbuddy.auth.repository.OtpVerificationRepository;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.bookings.dto.CreateBookingRequest;
import com.routbuddy.bookings.repository.BookingRepository;
import com.routbuddy.common.domain.enums.FuelType;
import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.PaymentMethod;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.domain.enums.VehicleType;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.routes.dto.CreateRouteRequest;
import com.routbuddy.routes.dto.CreateRouteStopRequest;
import com.routbuddy.routes.repository.RouteRepository;
import com.routbuddy.routes.repository.RouteStopRepository;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.dto.CreateTripRequest;
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

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingConcurrencyIntegrationTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private String adminToken;
    private String driverToken;
    private List<String> passengerTokens;
    private UUID tripId;
    private UUID pickupStopId;
    private UUID dropoffStopId;

    private String generateRandomPhone() {
        return "9" + String.format("%09d", Math.abs(RANDOM.nextLong() % 1000000000L));
    }

    @BeforeEach
    void setUp() throws Exception {
        bookingRepository.deleteAll();
        tripRepository.deleteAll();
        routeStopRepository.deleteAll();
        routeRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        otpVerificationRepository.deleteAll();
        userRepository.deleteAll();

        for (UserRole role : UserRole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(Role.builder().name(role).description("Role " + role).build());
            }
        }

        passengerTokens = new ArrayList<>();

        // 1. Register Admin
        RegisterRequest adminReq = RegisterRequest.builder()
                .phone(generateRandomPhone())
                .fullName("System Admin")
                .email("admin_" + UUID.randomUUID() + "@routbuddy.com")
                .password("AdminSecret123!")
                .role(UserRole.ADMIN)
                .build();
        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isCreated())
                .andReturn();
        adminToken = extractToken(adminRes);

        // 2. Register Driver
        RegisterRequest driverReq = RegisterRequest.builder()
                .phone(generateRandomPhone())
                .fullName("Ramesh Auto Driver")
                .email("driver_" + UUID.randomUUID() + "@routbuddy.com")
                .password("DriverSecret123!")
                .role(UserRole.DRIVER)
                .build();
        MvcResult driverRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverReq)))
                .andExpect(status().isCreated())
                .andReturn();
        driverToken = extractToken(driverRes);

        // 3. Driver Profile & Admin KYC Approval
        RegisterDriverRequest driverProfileReq = RegisterDriverRequest.builder()
                .licenseNumber("DL-" + UUID.randomUUID().toString().substring(0, 10))
                .licenseExpiryDate(LocalDate.now().plusYears(4))
                .aadhaarMasked("XXXX-XXXX-8888")
                .build();
        MvcResult driverProfileRes = mockMvc.perform(post("/api/v1/drivers/register")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverProfileReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String driverId = objectMapper.readTree(driverProfileRes.getResponse().getContentAsString())
                .get("data").get("id").asText();

        UpdateKycRequest kycReq = new UpdateKycRequest(KYCStatus.APPROVED, null);
        mockMvc.perform(put("/api/v1/drivers/" + driverId + "/kyc")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kycReq)))
                .andExpect(status().isOk());

        // 4. Register Vehicle (4 seats) & Admin Approval
        RegisterVehicleRequest vehicleReq = RegisterVehicleRequest.builder()
                .plateNumber("DL-01-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .vehicleType(VehicleType.AUTO_3W)
                .modelName("Bajaj Maxima 4-Seater")
                .seatingCapacity(4)
                .fuelType(FuelType.CNG)
                .build();
        MvcResult vehicleRes = mockMvc.perform(post("/api/v1/vehicles/register")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String vehicleId = objectMapper.readTree(vehicleRes.getResponse().getContentAsString())
                .get("data").get("id").asText();

        mockMvc.perform(put("/api/v1/vehicles/" + vehicleId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 5. Create Route with Stops
        CreateRouteRequest routeReq = CreateRouteRequest.builder()
                .name("Botanical Garden to Sector 62 Concurrency Corridor")
                .originName("Botanical Garden")
                .destinationName("Sector 62")
                .originLatitude(28.5645)
                .originLongitude(77.3345)
                .destinationLatitude(28.6280)
                .destinationLongitude(77.3730)
                .baseFareInr(30.0)
                .totalDistanceKm(9.5)
                .estimatedDurationMin(25)
                .stops(List.of(
                        new CreateRouteStopRequest("Botanical Garden", 1, 28.5645, 77.3345, 0.0, 10.0, 50),
                        new CreateRouteStopRequest("Noida Sec 18", 2, 28.5700, 77.3400, 2.0, 20.0, 50),
                        new CreateRouteStopRequest("Noida Sec 62", 3, 28.6280, 77.3730, 9.5, 30.0, 50)
                ))
                .build();
        MvcResult routeRes = mockMvc.perform(post("/api/v1/routes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String routeId = objectMapper.readTree(routeRes.getResponse().getContentAsString())
                .get("data").get("id").asText();
        pickupStopId = UUID.fromString(objectMapper.readTree(routeRes.getResponse().getContentAsString())
                .get("data").get("stops").get(0).get("id").asText());
        dropoffStopId = UUID.fromString(objectMapper.readTree(routeRes.getResponse().getContentAsString())
                .get("data").get("stops").get(2).get("id").asText());

        // 6. Create and Publish Trip with 4 seats
        CreateTripRequest tripReq = CreateTripRequest.builder()
                .vehicleId(UUID.fromString(vehicleId))
                .routeId(UUID.fromString(routeId))
                .scheduledDeparture(Instant.now().plusSeconds(3600))
                .totalSeats(4)
                .farePerSeatInr(30.0)
                .build();
        MvcResult tripRes = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripReq)))
                .andExpect(status().isCreated())
                .andReturn();
        tripId = UUID.fromString(objectMapper.readTree(tripRes.getResponse().getContentAsString())
                .get("data").get("id").asText());

        mockMvc.perform(put("/api/v1/trips/" + tripId + "/publish")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk());

        // 7. Register 10 Passengers
        for (int i = 1; i <= 10; i++) {
            RegisterRequest passReq = RegisterRequest.builder()
                    .phone(generateRandomPhone())
                    .fullName("Passenger " + i)
                    .email("pass_" + i + "_" + UUID.randomUUID() + "@routbuddy.com")
                    .password("PassSecret123!")
                    .role(UserRole.PASSENGER)
                    .build();
            MvcResult passRes = mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passReq)))
                    .andExpect(status().isCreated())
                    .andReturn();
            passengerTokens.add(extractToken(passRes));
        }
    }

    @Test
    @DisplayName("Critical Concurrency Requirement: 10 concurrent booking requests on 4 seats must allow exactly 4 and reject 6")
    void testConcurrentBookingPessimisticLock() throws Exception {
        int numberOfConcurrentUsers = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finishLine = new CountDownLatch(numberOfConcurrentUsers);

        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger rejectedBookings = new AtomicInteger(0);

        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startGun.await(); // Wait for all threads to be ready

                    CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                            .tripId(tripId)
                            .pickupStopId(pickupStopId)
                            .dropoffStopId(dropoffStopId)
                            .seatCount(1)
                            .paymentMethod(PaymentMethod.UPI_INTENT)
                            .idempotencyKey("concurrent-key-" + index + "-" + UUID.randomUUID())
                            .build();

                    MvcResult result = mockMvc.perform(post("/api/v1/bookings")
                                    .header("Authorization", "Bearer " + passengerTokens.get(index))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(bookingRequest)))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        successfulBookings.incrementAndGet();
                    } else if (status == 400) {
                        rejectedBookings.incrementAndGet();
                    }
                } catch (Exception e) {
                    rejectedBookings.incrementAndGet();
                } finally {
                    finishLine.countDown();
                }
            });
        }

        // Fire the starting gun!
        startGun.countDown();
        boolean finishedInTime = finishLine.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(finishedInTime, "Concurrent requests timed out");

        // CRITICAL ASSERTIONS:
        assertEquals(4, successfulBookings.get(), "Exactly 4 seats must be successfully booked");
        assertEquals(6, rejectedBookings.get(), "Exactly 6 requests must be rejected due to capacity limit");

        // Verify remaining available seats on Trip is exactly 0
        Trip updatedTrip = tripRepository.findById(tripId).orElseThrow();
        assertEquals(0, updatedTrip.getAvailableSeats(), "Trip available seats must reach exactly 0");
    }

    @Test
    @DisplayName("Idempotency: Re-submitting the same idempotency key returns the same booking without deducting extra seats")
    void testIdempotencyProtection() throws Exception {
        String token = passengerTokens.get(0);
        String idempotencyKey = "idemp-" + UUID.randomUUID();

        CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                .tripId(tripId)
                .pickupStopId(pickupStopId)
                .dropoffStopId(dropoffStopId)
                .seatCount(2)
                .paymentMethod(PaymentMethod.UPI_INTENT)
                .idempotencyKey(idempotencyKey)
                .build();

        // First Request
        MvcResult firstResult = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.seatCount").value(2))
                .andReturn();

        String firstBookingCode = objectMapper.readTree(firstResult.getResponse().getContentAsString())
                .get("data").get("bookingCode").asText();

        // Second Request with SAME Idempotency Key
        MvcResult secondResult = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookingCode").value(firstBookingCode))
                .andReturn();

        String secondBookingCode = objectMapper.readTree(secondResult.getResponse().getContentAsString())
                .get("data").get("bookingCode").asText();

        assertEquals(firstBookingCode, secondBookingCode);

        // Verify available seats decremented only ONCE (4 - 2 = 2)
        Trip trip = tripRepository.findById(tripId).orElseThrow();
        assertEquals(2, trip.getAvailableSeats());
    }

    private String extractToken(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(json).get("data").toString(),
                AuthResponse.class
        );
        return auth.getAccessToken();
    }
}
