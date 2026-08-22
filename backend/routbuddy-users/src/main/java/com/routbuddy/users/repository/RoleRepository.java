package com.routbuddy.users.repository;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(UserRole name);
    boolean existsByName(UserRole name);
}
