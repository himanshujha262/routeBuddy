package com.routbuddy.auth.service;

import com.routbuddy.auth.domain.entity.RefreshToken;
import com.routbuddy.auth.dto.TokenRefreshResponse;
import com.routbuddy.auth.repository.RefreshTokenRepository;
import com.routbuddy.auth.security.JwtTokenProvider;
import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.exception.TokenRefreshException;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${routbuddy.jwt.refresh-expiration-ms:604800000}") // 7 days default
    private long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenRefreshException("User not found for id: " + userId));
        return createRefreshToken(user);
    }

    @Transactional
    public TokenRefreshResponse rotateRefreshToken(String requestRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database"));

        verifyExpiration(token);

        User user = token.getUser();

        // Token rotation: revoke current token and issue replacement
        String newTokenString = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        token.setReplacedByToken(newTokenString);
        refreshTokenRepository.save(token);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newTokenString)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);

        log.info("Successfully rotated refresh token for user id: {}", user.getId());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .build();
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            log.warn("Attempted use of revoked refresh token: {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh token has been revoked. Please log in again.");
        }

        if (token.isExpired()) {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            log.warn("Refresh token expired: {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please log in again.");
        }

        return token;
    }

    @Transactional
    public void revokeRefreshToken(String tokenString) {
        refreshTokenRepository.findByToken(tokenString).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            log.info("Revoked refresh token: {}", tokenString);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        if (!tokens.isEmpty()) {
            Instant now = Instant.now();
            for (RefreshToken token : tokens) {
                token.setRevoked(true);
                token.setRevokedAt(now);
            }
            refreshTokenRepository.saveAll(tokens);
            log.info("Revoked {} active refresh tokens for user id: {}", tokens.size(), userId);
        }
    }
}
