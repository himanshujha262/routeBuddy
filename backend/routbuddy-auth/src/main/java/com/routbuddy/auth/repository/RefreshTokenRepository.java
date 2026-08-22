package com.routbuddy.auth.repository;

import com.routbuddy.auth.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    List<RefreshToken> findByUserPhone(String phone);
}
