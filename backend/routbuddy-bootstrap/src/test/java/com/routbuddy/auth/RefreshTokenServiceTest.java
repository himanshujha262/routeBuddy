package com.routbuddy.auth;

import com.routbuddy.auth.domain.entity.RefreshToken;
import com.routbuddy.auth.dto.TokenRefreshResponse;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.auth.security.JwtTokenProvider;
import com.routbuddy.auth.service.RefreshTokenService;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.TokenRefreshException;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenService refreshTokenService;

    private User sampleUser;
    private RefreshToken sampleToken;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "routbuddy_very_secure_jwt_secret_key_that_is_at_least_256_bits_long_2026",
                3600000L
        );
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, jwtTokenProvider);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);

        sampleUser = User.builder()
                .phone("9876543210")
                .fullName("Rahul Sharma")
                .primaryRole(UserRole.PASSENGER)
                .roles(Set.of(Role.builder().name(UserRole.PASSENGER).build()))
                .active(true)
                .build();
        sampleUser.setId(UUID.randomUUID());

        sampleToken = RefreshToken.builder()
                .user(sampleUser)
                .token("sample-valid-refresh-token-1234567890")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        sampleToken.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create and save a new refresh token for user")
    void testCreateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(sampleUser);

        assertNotNull(created);
        assertNotNull(created.getToken());
        assertFalse(created.isRevoked());
        assertTrue(created.getExpiryDate().isAfter(Instant.now()));
        assertEquals(sampleUser, created.getUser());
    }

    @Test
    @DisplayName("Should rotate refresh token and issue new access token")
    void testRotateRefreshToken() {
        when(refreshTokenRepository.findByToken("sample-valid-refresh-token-1234567890")).thenReturn(Optional.of(sampleToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenRefreshResponse response = refreshTokenService.rotateRefreshToken("sample-valid-refresh-token-1234567890");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals("sample-valid-refresh-token-1234567890", response.getRefreshToken());
        assertTrue(sampleToken.isRevoked());
    }

    @Test
    @DisplayName("Should throw TokenRefreshException when rotating revoked token")
    void testRotateRevokedTokenThrowsException() {
        sampleToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(sampleToken));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.rotateRefreshToken("revoked-token"));
    }

    @Test
    @DisplayName("Should throw TokenRefreshException when rotating expired token")
    void testRotateExpiredTokenThrowsException() {
        sampleToken.setExpiryDate(Instant.now().minusSeconds(3600));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(sampleToken));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.rotateRefreshToken("expired-token"));
    }
}
