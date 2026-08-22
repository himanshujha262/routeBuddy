package com.routbuddy.matching.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.CommuteType;
import com.routbuddy.common.domain.enums.GenderPreference;
import com.routbuddy.common.domain.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "commute_profiles", indexes = {
        @Index(name = "idx_commute_user_id", columnList = "user_id"),
        @Index(name = "idx_commute_type", columnList = "commute_type"),
        @Index(name = "idx_commute_geohash", columnList = "home_geohash"),
        @Index(name = "idx_commute_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommuteProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "commute_type", nullable = false, length = 30)
    private CommuteType commuteType;

    @Column(name = "home_geohash", nullable = false, length = 8)
    private String homeGeohash;

    @Column(name = "home_geom", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point homeGeom;

    @Column(name = "home_neighborhood_label", length = 100)
    private String homeNeighborhoodLabel;

    @Column(name = "dest_geom", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point destGeom;

    @Column(name = "dest_name", nullable = false, length = 150)
    private String destName;

    @Column(name = "departure_time_start", nullable = false)
    private LocalTime departureTimeStart;

    @Column(name = "departure_time_end", nullable = false)
    private LocalTime departureTimeEnd;

    @Column(name = "return_time_start")
    private LocalTime returnTimeStart;

    @Column(name = "return_time_end")
    private LocalTime returnTimeEnd;

    @Column(name = "travel_days", nullable = false, length = 50)
    @Builder.Default
    private String travelDays = "MON,TUE,WED,THU,FRI";

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_preference", nullable = false, length = 30)
    @Builder.Default
    private GenderPreference genderPreference = GenderPreference.ANY;

    @Column(name = "language_preference", length = 50)
    @Builder.Default
    private String languagePreference = "English,Hindi";

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 30)
    @Builder.Default
    private PlanType planType = PlanType.OFFICE_COMMUTE;

    @Column(name = "vehicle_model", length = 100)
    private String vehicleModel;

    @Column(name = "seats_offered")
    @Builder.Default
    private int seatsOffered = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
