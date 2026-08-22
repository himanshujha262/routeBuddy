package com.routbuddy.users.service;

import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.users.domain.entity.User;
import com.routbuddy.users.dto.UpdateProfileRequest;
import com.routbuddy.users.dto.UserProfileDto;

import java.util.UUID;

public interface UserService {
    UserProfileDto getUserProfile(UUID userId);
    User getUserEntity(UUID userId);
    UserProfileDto updateProfile(UUID userId, UpdateProfileRequest request);
    void assignRoleToUser(UUID userId, UserRole roleName);
}
