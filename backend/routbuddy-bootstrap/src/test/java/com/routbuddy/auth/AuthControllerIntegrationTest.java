package com.routbuddy.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routbuddy.auth.dto.LoginRequest;
import com.routbuddy.auth.dto.RefreshTokenRequest;
import com.routbuddy.auth.dto.RegisterRequest;
import com.routbuddy.auth.dto.SendOtpRequest;
import com.routbuddy.auth.repository.OtpVerificationRepository;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        otpVerificationRepository.deleteAll();
        userRepository.deleteAll();

        // Ensure default roles exist
        for (UserRole role : UserRole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(Role.builder().name(role).description("Role " + role).build());
            }
        }
    }

    @Test
    @DisplayName("Integration Test: Full User Registration Lifecycle")
    void testRegisterUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .phone("9811223344")
                .fullName("Aarav Patel")
                .email("aarav@routbuddy.com")
                .password("password123")
                .role(UserRole.PASSENGER)
                .gender("MALE")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.phone", is("9811223344")))
                .andExpect(jsonPath("$.data.primaryRole", is("PASSENGER")));
    }

    @Test
    @DisplayName("Integration Test: Duplicate Phone Registration returns 409 Conflict")
    void testRegisterDuplicatePhone() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .phone("9811223344")
                .fullName("Aarav Patel")
                .password("password123")
                .role(UserRole.PASSENGER)
                .build();

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Integration Test: Login with valid and invalid credentials")
    void testLoginFlow() throws Exception {
        // 1. Register User
        RegisterRequest registerReq = RegisterRequest.builder()
                .phone("9988776655")
                .fullName("Simran Kaur")
                .password("securePass123")
                .role(UserRole.COMMUTER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // 2. Successful Login
        LoginRequest loginReq = LoginRequest.builder()
                .identifier("9988776655")
                .password("securePass123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));

        // 3. Failed Login (Wrong Password)
        LoginRequest wrongLoginReq = LoginRequest.builder()
                .identifier("9988776655")
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Integration Test: Refresh Token Rotation")
    void testRefreshTokenRotation() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .phone("9876123450")
                .fullName("Vikram Malhotra")
                .password("pass123456")
                .role(UserRole.DRIVER)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        String initialRefreshToken = objectMapper.readTree(json).path("data").path("refreshToken").asText();

        // Rotate token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(initialRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", not(equalTo(initialRefreshToken))));
    }

    @Test
    @DisplayName("Integration Test: Send OTP endpoint")
    void testSendOtp() throws Exception {
        SendOtpRequest otpReq = new SendOtpRequest("9812345678");

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("OTP sent successfully")));
    }

    @Test
    @DisplayName("Integration Test: RBAC Guards for Admin, Driver, and Passenger")
    void testRbacGuards() throws Exception {
        // 1. Register Admin
        RegisterRequest adminReq = RegisterRequest.builder()
                .phone("9800000001")
                .fullName("System Admin")
                .password("adminSecret")
                .role(UserRole.ADMIN)
                .build();

        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 2. Register Passenger
        RegisterRequest passengerReq = RegisterRequest.builder()
                .phone("9800000002")
                .fullName("Regular Passenger")
                .password("passSecret")
                .role(UserRole.PASSENGER)
                .build();

        MvcResult passengerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passengerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String passengerToken = objectMapper.readTree(passengerResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 3. Admin accesses Admin endpoint -> 200 OK
        mockMvc.perform(get("/api/v1/auth/test-rbac/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", containsString("Access Granted")));

        // 4. Passenger attempts Admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/v1/auth/test-rbac/admin")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        // 5. Passenger accesses Passenger endpoint -> 200 OK
        mockMvc.perform(get("/api/v1/auth/test-rbac/passenger")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", containsString("Access Granted")));

        // 6. Unauthenticated request to protected endpoint -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 7. Authenticated request to /api/v1/auth/me -> 200 OK
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone", is("9800000002")))
                .andExpect(jsonPath("$.data.fullName", is("Regular Passenger")));
    }
}
