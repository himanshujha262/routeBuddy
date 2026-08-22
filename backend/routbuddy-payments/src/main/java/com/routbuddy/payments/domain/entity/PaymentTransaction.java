package com.routbuddy.payments.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.PaymentMethod;
import com.routbuddy.common.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payments_user_id", columnList = "user_id"),
        @Index(name = "idx_payments_booking_id", columnList = "booking_id"),
        @Index(name = "idx_payments_status", columnList = "payment_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "amount_inr", nullable = false)
    private double amountInr;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "gateway_signature", length = 255)
    private String gatewaySignature;

    @Column(name = "upi_vpa", length = 100)
    private String upiVpa;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount_inr")
    private Double refundAmountInr;
}
