package com.routbuddy.safety.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.IncidentStatus;
import com.routbuddy.common.domain.enums.IncidentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "safety_incidents", indexes = {
        @Index(name = "idx_incidents_user_id", columnList = "user_id"),
        @Index(name = "idx_incidents_trip_id", columnList = "trip_id"),
        @Index(name = "idx_incidents_status", columnList = "status"),
        @Index(name = "idx_incidents_type", columnList = "incident_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyIncident extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 30)
    private IncidentType incidentType;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "location_description")
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.TRIGGERED;

    @Column(name = "emergency_contacts_notified", nullable = false)
    @Builder.Default
    private boolean emergencyContactsNotified = false;

    @Column(name = "police_notified", nullable = false)
    @Builder.Default
    private boolean policeNotified = false;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "resolved_by_admin_id")
    private UUID resolvedByAdminId;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
