package com.routbuddy.drivers.repository;

import com.routbuddy.drivers.domain.entity.DriverEarningsLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverEarningsLedgerRepository extends JpaRepository<DriverEarningsLedger, UUID> {
    Page<DriverEarningsLedger> findByDriverId(UUID driverId, Pageable pageable);
    Optional<DriverEarningsLedger> findByTripId(UUID tripId);

    @Query("SELECT COALESCE(SUM(d.amountInr), 0.0) FROM DriverEarningsLedger d WHERE d.driverId = :driverId")
    Double calculateTotalNetEarningsByDriverId(@Param("driverId") UUID driverId);
}
