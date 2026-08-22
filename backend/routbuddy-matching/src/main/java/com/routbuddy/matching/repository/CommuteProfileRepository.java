package com.routbuddy.matching.repository;

import com.routbuddy.common.domain.enums.CommuteType;
import com.routbuddy.matching.domain.entity.CommuteProfile;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommuteProfileRepository extends JpaRepository<CommuteProfile, UUID> {

    Optional<CommuteProfile> findByUserIdAndActiveTrue(UUID userId);

    List<CommuteProfile> findByUserId(UUID userId);

    @Query(value = """
        SELECT cp.* FROM commute_profiles cp
        WHERE cp.is_active = true
        AND cp.user_id <> :excludeUserId
        AND cp.commute_type <> :myCommuteType
        AND ST_DWithin(cp.home_geom, :originPoint, :originRadiusMeters, false)
        AND ST_DWithin(cp.dest_geom, :destPoint, :destRadiusMeters, false)
    """, nativeQuery = true)
    List<CommuteProfile> findCandidatesWithinRadius(
            @Param("excludeUserId") UUID excludeUserId,
            @Param("myCommuteType") String myCommuteType,
            @Param("originPoint") Point originPoint,
            @Param("destPoint") Point destPoint,
            @Param("originRadiusMeters") double originRadiusMeters,
            @Param("destRadiusMeters") double destRadiusMeters);
}
