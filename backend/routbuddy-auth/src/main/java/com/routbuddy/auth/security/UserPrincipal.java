package com.routbuddy.auth.security;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String phone;
    private final String email;
    private final String password;
    private final UserRole primaryRole;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public UserPrincipal(UUID id, String phone, String email, String password, UserRole primaryRole, Collection<? extends GrantedAuthority> authorities, boolean active) {
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.primaryRole = primaryRole;
        this.authorities = authorities;
        this.active = active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String phone;
        private String email;
        private String password;
        private UserRole primaryRole;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean active = true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder primaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; return this; }
        public Builder authorities(Collection<? extends GrantedAuthority> authorities) { this.authorities = authorities; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public UserPrincipal build() {
            return new UserPrincipal(id, phone, email, password, primaryRole, authorities, active);
        }
    }

    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                    .collect(Collectors.toSet());
        } else if (user.getPrimaryRole() != null) {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getPrimaryRole().name()));
        } else {
            authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + UserRole.PASSENGER.name()));
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .primaryRole(user.getPrimaryRole() != null ? user.getPrimaryRole() : UserRole.PASSENGER)
                .authorities(authorities)
                .active(user.isActive())
                .build();
    }

    public UUID getId() { return id; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public UserRole getPrimaryRole() { return primaryRole; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phone != null ? phone : (email != null ? email : (id != null ? id.toString() : ""));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
