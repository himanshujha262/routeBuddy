package com.routbuddy.auth;

import com.routbuddy.auth.domain.entity.OtpVerification;
import com.routbuddy.auth.domain.entity.RefreshToken;
import com.routbuddy.auth.dto.*;
import com.routbuddy.auth.repository.OtpVerificationRepository;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.auth.security.JwtTokenProvider;
import com.routbuddy.auth.service.AuthService;
import com.routbuddy.auth.service.RefreshTokenService;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.InvalidOtpException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenService refreshTokenService;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    private User sampleUser;
    private Role passengerRole;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "routbuddy_very_secure_jwt_secret_key_that_is_at_least_256_bits_long_2026",
                3600000L
        );
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, jwtTokenProvider);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
        passwordEncoder = new BCryptPasswordEncoder();

        authService = new AuthService(
                userRepository,
                roleRepository,
                otpVerificationRepository,
                refreshTokenService,
                jwtTokenProvider,
                passwordEncoder
        );

        passengerRole = Role.builder().name(UserRole.PASSENGER).description("Passenger").build();
        passengerRole.setId(UUID.randomUUID());

        sampleUser = User.builder()
                .phone("9876543210")
                .email("user@routbuddy.com")
                .fullName("Priya Verma")
                .passwordHash(passwordEncoder.encode("secret123"))
                .primaryRole(UserRole.PASSENGER)
                .roles(Set.of(passengerRole))
                .active(true)
                .build();
        sampleUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .phone("9876543210")
                .fullName("Priya Verma")
                .email("user@routbuddy.com")
                .password("secret123")
                .role(UserRole.PASSENGER)
                .build();

        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(userRepository.existsByEmail("user@routbuddy.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.PASSENGER)).thenReturn(Optional.of(passengerRole));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("9876543210", response.getPhone());
        assertEquals(UserRole.PASSENGER, response.getPrimaryRole());
    }

    @Test
    @DisplayName("Should reject registration when phone already exists")
    void testRegisterDuplicatePhone() {
        RegisterRequest request = RegisterRequest.builder()
                .phone("9876543210")
                .fullName("Priya Verma")
                .password("secret123")
                .role(UserRole.PASSENGER)
                .build();

        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should login successfully with valid phone and password")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .identifier("9876543210")
                .password("secret123")
                .build();

        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("9876543210", response.getPhone());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid password")
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .identifier("9876543210")
                .password("wrongPassword")
                .build();

        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(sampleUser));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should verify valid OTP and authenticate user")
    void testVerifyOtpSuccess() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phone("9876543210")
                .otpCode("456789")
                .build();

        OtpVerification verification = OtpVerification.builder()
                .phone("9876543210")
                .otpCode("456789")
                .expiryTime(Instant.now().plusSeconds(300))
                .verified(false)
                .build();

        when(otpVerificationRepository.findTopByPhoneAndVerifiedFalseOrderByExpiryTimeDesc("9876543210"))
                .thenReturn(Optional.of(verification));
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.verifyOtp(request);

        assertNotNull(response);
        assertTrue(verification.isVerified());
        assertNotNull(response.getAccessToken());
    }

    @Test
    @DisplayName("Should reject invalid OTP code")
    void testVerifyOtpInvalidCode() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phone("9876543210")
                .otpCode("000000")
                .build();

        OtpVerification verification = OtpVerification.builder()
                .phone("9876543210")
                .otpCode("456789")
                .expiryTime(Instant.now().plusSeconds(300))
                .verified(false)
                .attemptsCount(0)
                .build();

        when(otpVerificationRepository.findTopByPhoneAndVerifiedFalseOrderByExpiryTimeDesc("9876543210"))
                .thenReturn(Optional.of(verification));

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtp(request));
        assertEquals(1, verification.getAttemptsCount());
    }
}
