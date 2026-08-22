package com.routbuddy.users.service;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.users.domain.entity.Role;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.dto.UpdateProfileRequest;
import com.routbuddy.users.dto.UserProfileDto;
import com.routbuddy.users.repository.RoleRepository;
import com.routbuddy.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserProfileDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUserEntity(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getOrgName() != null) {
            user.setOrgName(request.getOrgName());
        }
        if (request.getOrgEmail() != null) {
            user.setOrgEmail(request.getOrgEmail().trim().toLowerCase());
        }
        if (request.getLanguagePreference() != null) {
            user.setLanguagePreference(request.getLanguagePreference());
        }

        User updated = userRepository.save(user);
        log.info("User profile updated for user id: {}", userId);
        return UserProfileDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, UserRole roleName) {
        User user = getUserEntity(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        user.addRole(role);
        userRepository.save(user);
        log.info("Role {} assigned to user {}", roleName, userId);
    }
}
