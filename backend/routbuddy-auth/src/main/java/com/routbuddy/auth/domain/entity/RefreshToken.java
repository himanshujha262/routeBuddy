package com.routbuddy.auth.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.users.domain.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
})
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token", length = 255)
    private String replacedByToken;

    public RefreshToken() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private String token;
        private Instant expiryDate;
        private boolean revoked = false;
        private Instant revokedAt;
        private String replacedByToken;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder expiryDate(Instant expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public Builder revokedAt(Instant revokedAt) { this.revokedAt = revokedAt; return this; }
        public Builder replacedByToken(String replacedByToken) { this.replacedByToken = replacedByToken; return this; }

        public RefreshToken build() {
            RefreshToken rt = new RefreshToken();
            if (id != null) rt.setId(id);
            rt.setUser(user);
            rt.setToken(token);
            rt.setExpiryDate(expiryDate);
            rt.setRevoked(revoked);
            rt.setRevokedAt(revokedAt);
            rt.setReplacedByToken(replacedByToken);
            return rt;
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public String getReplacedByToken() { return replacedByToken; }
    public void setReplacedByToken(String replacedByToken) { this.replacedByToken = replacedByToken; }
}
