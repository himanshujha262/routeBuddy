package com.routbuddy.subscriptions.repository;

import com.routbuddy.subscriptions.domain.entity.CommutePass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommutePassRepository extends JpaRepository<CommutePass, UUID> {
    List<CommutePass> findByUserIdAndActiveTrueAndEndDateGreaterThanEqual(UUID userId, LocalDate currentDate);
    Optional<CommutePass> findByPassCode(String passCode);
    long countByActiveTrue();
}
