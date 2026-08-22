package com.routbuddy.subscriptions.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "commute_passes", indexes = {
        @Index(name = "idx_passes_user_id", columnList = "user_id"),
        @Index(name = "idx_passes_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommutePass extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 30)
    private PlanType planType;

    @Column(name = "pass_code", nullable = false, unique = true, length = 30)
    private String passCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_rides_allowed", nullable = false)
    private int totalRidesAllowed;

    @Column(name = "rides_used", nullable = false)
    @Builder.Default
    private int ridesUsed = 0;

    @Column(name = "price_inr", nullable = false)
    private double priceInr;

    @Column(name = "discount_percent", nullable = false)
    @Builder.Default
    private double discountPercent = 0.0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "qr_pass_token", length = 512)
    private String qrPassToken;
}
