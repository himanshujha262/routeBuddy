package com.routbuddy.ratings.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ratings", indexes = {
        @Index(name = "idx_ratings_target", columnList = "target_user_id"),
        @Index(name = "idx_ratings_trip", columnList = "trip_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating extends BaseEntity {

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "commute_match_id")
    private UUID commuteMatchId;

    @Column(name = "score", nullable = false)
    private int score; // 1 to 5

    @Column(name = "feedback_text", length = 500)
    private String feedbackText;

    @Column(name = "punctuality_score")
    private Integer punctualityScore;

    @Column(name = "safety_score")
    private Integer safetyScore;

    @Column(name = "cleanliness_score")
    private Integer cleanlinessScore;
}
