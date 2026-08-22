package com.routbuddy.matching.repository;

import com.routbuddy.common.domain.enums.MatchStatus;
import com.routbuddy.matching.domain.entity.CommuteMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommuteMatchRepository extends JpaRepository<CommuteMatch, UUID> {

    @Query("SELECT m FROM CommuteMatch m WHERE m.requesterProfileId = :profileId OR m.partnerProfileId = :profileId")
    List<CommuteMatch> findAllByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT m FROM CommuteMatch m WHERE (m.requesterProfileId = :profile1 AND m.partnerProfileId = :profile2) OR (m.requesterProfileId = :profile2 AND m.partnerProfileId = :profile1)")
    Optional<CommuteMatch> findBetweenProfiles(@Param("profile1") UUID profile1, @Param("profile2") UUID profile2);

    List<CommuteMatch> findByPartnerProfileIdAndStatus(UUID partnerProfileId, MatchStatus status);

    List<CommuteMatch> findByRequesterProfileIdAndStatus(UUID requesterProfileId, MatchStatus status);

    long countByStatus(MatchStatus status);
}
