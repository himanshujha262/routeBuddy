package com.routbuddy.matching.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commute_matches", indexes = {
        @Index(name = "idx_matches_requester", columnList = "requester_profile_id"),
        @Index(name = "idx_matches_partner", columnList = "partner_profile_id"),
        @Index(name = "idx_matches_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommuteMatch extends BaseEntity {

    @Column(name = "requester_profile_id", nullable = false)
    private UUID requesterProfileId;

    @Column(name = "partner_profile_id", nullable = false)
    private UUID partnerProfileId;

    @Column(name = "match_score", nullable = false)
    private double matchScore;

    @Column(name = "route_overlap_ratio", nullable = false)
    private double routeOverlapRatio;

    @Column(name = "schedule_delta_minutes", nullable = false)
    private int scheduleDeltaMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private MatchStatus status = MatchStatus.SUGGESTED;

    @Column(name = "chat_channel_id", length = 100)
    private String chatChannelId;

    @Column(name = "unlocked_at")
    private Instant unlockedAt;

    @Column(name = "request_message", length = 255)
    private String requestMessage;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;
}
