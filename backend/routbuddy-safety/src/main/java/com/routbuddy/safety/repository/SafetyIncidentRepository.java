package com.routbuddy.safety.repository;

import com.routbuddy.common.domain.enums.IncidentStatus;
import com.routbuddy.safety.domain.entity.SafetyIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, UUID> {
    List<SafetyIncident> findByStatusIn(List<IncidentStatus> statuses);
    Page<SafetyIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status, Pageable pageable);
    long countByStatus(IncidentStatus status);
}
