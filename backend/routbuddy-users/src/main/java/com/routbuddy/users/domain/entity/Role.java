package com.routbuddy.users.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.UserRole;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_roles_name", columnList = "name", unique = true)
})
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private UserRole name;

    @Column(name = "description", length = 200)
    private String description;

    public Role() {}

    public Role(UserRole name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UserRole name;
        private String description;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(UserRole name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Role build() {
            Role role = new Role(name, description);
            if (id != null) {
                role.setId(id);
            }
            return role;
        }
    }

    public UserRole getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
