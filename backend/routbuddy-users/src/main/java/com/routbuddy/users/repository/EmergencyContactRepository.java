package com.routbuddy.users.repository;

import com.routbuddy.users.domain.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
    List<EmergencyContact> findByUserId(UUID userId);
}
