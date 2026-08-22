package com.routbuddy.trips.repository;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.trips.domain.entity.Trip;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findByIdWithLock(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Trip t SET t.availableSeats = t.availableSeats - :seats WHERE t.id = :tripId AND t.availableSeats >= :seats")
    int decrementAvailableSeats(@Param("tripId") UUID tripId, @Param("seats") int seats);

    @Modifying
    @Query("UPDATE Trip t SET t.availableSeats = t.availableSeats + :seats WHERE t.id = :tripId AND (t.availableSeats + :seats) <= t.totalSeats")
    int incrementAvailableSeats(@Param("tripId") UUID tripId, @Param("seats") int seats);

    List<Trip> findByRouteIdAndStatusIn(UUID routeId, List<TripStatus> statuses);

    Page<Trip> findByDriverIdOrderByScheduledDepartureDesc(UUID driverId, Pageable pageable);

    Optional<Trip> findFirstByDriverIdAndStatusInOrderByScheduledDepartureDesc(UUID driverId, List<TripStatus> statuses);

    long countByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.routeId IN :routeIds AND t.status IN :statuses AND t.availableSeats > 0 AND t.scheduledDeparture >= :fromTime ORDER BY t.scheduledDeparture ASC")
    List<Trip> findAvailableTripsForRoutes(
            @Param("routeIds") List<UUID> routeIds,
            @Param("statuses") List<TripStatus> statuses,
            @Param("fromTime") Instant fromTime);
}
