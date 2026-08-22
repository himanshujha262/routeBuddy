package com.routbuddy.auth;

import com.routbuddy.auth.security.JwtTokenProvider;
import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "routbuddy_very_secure_jwt_secret_key_that_is_at_least_256_bits_long_2026";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT access token from UserPrincipal")
    void testGenerateAccessTokenFromUserPrincipal() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .phone("9876543210")
                .email("test@routbuddy.com")
                .primaryRole(UserRole.PASSENGER)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .active(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(principal);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("9876543210", jwtTokenProvider.getPhoneFromToken(token));
        assertEquals("test@routbuddy.com", jwtTokenProvider.getEmailFromToken(token));
        assertTrue(jwtTokenProvider.getRolesFromToken(token).contains("ROLE_PASSENGER"));
    }

    @Test
    @DisplayName("Should generate valid JWT access token with multiple roles")
    void testGenerateAccessTokenWithMultipleRoles() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(
                userId,
                "9811122233",
                "driver@routbuddy.com",
                UserRole.DRIVER,
                Set.of("ROLE_DRIVER", "ROLE_COMMUTER")
        );

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("9811122233", jwtTokenProvider.getPhoneFromToken(token));
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_DRIVER"));
        assertTrue(roles.contains("ROLE_COMMUTER"));
    }

    @Test
    @DisplayName("Should reject invalid or tampered JWT token")
    void testValidateInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidsignature";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void testExpiredToken() {
        // Provider with negative expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider(secret, -1000);
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .phone("9876543210")
                .primaryRole(UserRole.PASSENGER)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .active(true)
                .build();

        String expiredToken = expiredProvider.generateAccessToken(principal);
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }
}
