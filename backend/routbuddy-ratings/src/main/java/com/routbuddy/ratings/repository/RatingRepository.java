package com.routbuddy.ratings.repository;

import com.routbuddy.ratings.domain.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    Page<Rating> findByTargetUserIdOrderByCreatedAtDesc(UUID targetUserId, Pageable pageable);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.targetUserId = :targetUserId")
    Double calculateAverageRatingForUser(@Param("targetUserId") UUID targetUserId);

    long countByTargetUserId(UUID targetUserId);
}
