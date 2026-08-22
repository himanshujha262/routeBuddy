package com.routbuddy.config;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure default roles exist in the database
        for (UserRole roleName : UserRole.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description("Platform role for " + roleName.name())
                        .build());
            }
        }

        if (userRepository.count() > 0) {
            log.info("Database already contains users. Skipping initial user seeding.");
            return;
        }

        log.info("Seeding initial users and roles for RoutBuddy Platform...");

        // 1. Seed Super Admin
        Role superAdminRole = roleRepository.findByName(UserRole.SUPER_ADMIN).orElseThrow();
        Role adminRole = roleRepository.findByName(UserRole.ADMIN).orElseThrow();
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(superAdminRole);
        adminRoles.add(adminRole);

        userRepository.save(User.builder()
                .phone("9876543210")
                .email("admin@routbuddy.com")
                .passwordHash(passwordEncoder.encode("Admin@12345"))
                .fullName("RoutBuddy Super Admin")
                .primaryRole(UserRole.SUPER_ADMIN)
                .roles(adminRoles)
                .verified(true)
                .trustScore(5.0)
                .build());

        // 2. Seed Driver User
        Role driverRole = roleRepository.findByName(UserRole.DRIVER).orElseThrow();
        userRepository.save(User.builder()
                .phone("9811122233")
                .email("ramesh.driver@routbuddy.com")
                .passwordHash(passwordEncoder.encode("Driver@12345"))
                .fullName("Ramesh Kumar")
                .primaryRole(UserRole.DRIVER)
                .roles(Set.of(driverRole))
                .gender("MALE")
                .verified(true)
                .trustScore(4.9)
                .build());

        // 3. Seed Passenger / Commuter User
        Role commuterRole = roleRepository.findByName(UserRole.COMMUTER).orElseThrow();
        userRepository.save(User.builder()
                .phone("9988776655")
                .email("ananya.sharma@techcorp.com")
                .passwordHash(passwordEncoder.encode("Commuter@12345"))
                .fullName("Ananya Sharma")
                .primaryRole(UserRole.COMMUTER)
                .roles(Set.of(commuterRole))
                .gender("FEMALE")
                .verified(true)
                .trustScore(4.9)
                .orgName("TechCorp Solutions")
                .orgEmail("ananya.sharma@techcorp.com")
                .orgVerified(true)
                .languagePreference("English,Hindi")
                .build());

        log.info("RoutBuddy Platform Phase 1 & 2 initial data seeding completed successfully!");
    }
}
