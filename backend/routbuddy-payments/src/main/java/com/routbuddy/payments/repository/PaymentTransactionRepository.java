package com.routbuddy.payments.repository;

import com.routbuddy.common.domain.enums.PaymentStatus;
import com.routbuddy.payments.domain.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByBookingId(UUID bookingId);
    Optional<PaymentTransaction> findByGatewayOrderId(String gatewayOrderId);
    Page<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amountInr), 0.0) FROM PaymentTransaction p WHERE p.paymentStatus = 'PAID'")
    Double calculateTotalPlatformRevenue();
}
