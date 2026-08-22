package com.routbuddy.auth.service;

import com.routbuddy.auth.domain.entity.OtpVerification;
import com.routbuddy.auth.domain.entity.RefreshToken;
import com.routbuddy.auth.dto.*;
import com.routbuddy.auth.repository.OtpVerificationRepository;
import com.routbuddy.auth.security.JwtTokenProvider;
import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.InvalidOtpException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.dto.UserProfileDto;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OtpVerificationRepository otpVerificationRepository,
            RefreshTokenService refreshTokenService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String phone = request.getPhone().trim();
        if (userRepository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("A user with phone number " + phone + " already exists");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("A user with email " + email + " already exists");
            }
        }

        UserRole primaryRole = request.getRole() != null ? request.getRole() : UserRole.PASSENGER;
        Role role = roleRepository.findByName(primaryRole)
                .orElseGet(() -> roleRepository.save(Role.builder().name(primaryRole).description("Role " + primaryRole).build()));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .phone(phone)
                .email(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .primaryRole(primaryRole)
                .roles(roles)
                .gender(request.getGender())
                .orgName(request.getOrgName())
                .orgEmail(request.getOrgEmail() != null ? request.getOrgEmail().trim().toLowerCase() : null)
                .verified(false)
                .trustScore(4.5)
                .active(true)
                .lastLoginAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {}, phone: {}, role: {}", savedUser.getId(), savedUser.getPhone(), primaryRole);

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        User user = userRepository.findByPhone(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials provided"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials provided");
        }

        if (!user.isActive()) {
            throw new BadRequestException("User account is deactivated. Please contact support.");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in successfully: {}", user.getPhone());
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public String sendOtp(SendOtpRequest request) {
        String phone = request.getPhone().trim();
        int otp = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(otp);

        OtpVerification verification = OtpVerification.builder()
                .phone(phone)
                .otpCode(otpCode)
                .expiryTime(Instant.now().plus(5, ChronoUnit.MINUTES))
                .verified(false)
                .attemptsCount(0)
                .build();

        otpVerificationRepository.save(verification);
        log.info("Generated OTP for phone: {} (OTP: {})", phone, otpCode);

        return "OTP sent successfully to " + phone;
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone().trim();
        String inputOtp = request.getOtpCode().trim();

        OtpVerification verification = otpVerificationRepository
                .findTopByPhoneAndVerifiedFalseOrderByExpiryTimeDesc(phone)
                .orElseThrow(() -> new InvalidOtpException("No active OTP request found for this phone number"));

        if (verification.isExpired()) {
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }

        if (!verification.getOtpCode().equals(inputOtp)) {
            verification.setAttemptsCount(verification.getAttemptsCount() + 1);
            otpVerificationRepository.save(verification);
            throw new InvalidOtpException("Invalid OTP entered. Please try again.");
        }

        verification.setVerified(true);
        otpVerificationRepository.save(verification);

        // Find or create user on OTP verification
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            Role passengerRole = roleRepository.findByName(UserRole.PASSENGER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.PASSENGER).build()));

            User newUser = User.builder()
                    .phone(phone)
                    .fullName("RoutBuddy Commuter")
                    .primaryRole(UserRole.PASSENGER)
                    .roles(Set.of(passengerRole))
                    .verified(true)
                    .trustScore(4.5)
                    .active(true)
                    .lastLoginAt(Instant.now())
                    .build();
            return userRepository.save(newUser);
        });

        user.setVerified(true);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("OTP verified successfully for phone: {}", phone);
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserProfileDto.fromEntity(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
                : Set.of();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .userId(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .primaryRole(user.getPrimaryRole())
                .roles(roles)
                .isVerified(user.isVerified())
                .trustScore(user.getTrustScore())
                .build();
    }
}
