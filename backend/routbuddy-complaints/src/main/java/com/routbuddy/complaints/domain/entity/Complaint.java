package com.routbuddy.complaints.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaints_user_id", columnList = "user_id"),
        @Index(name = "idx_complaints_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reported_user_id")
    private UUID reportedUserId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "admin_action_taken", length = 500)
    private String adminActionTaken;

    @Column(name = "resolved_by_admin_id")
    private UUID resolvedByAdminId;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
