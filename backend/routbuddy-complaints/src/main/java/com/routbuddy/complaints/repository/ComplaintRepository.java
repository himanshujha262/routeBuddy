package com.routbuddy.complaints.repository;

import com.routbuddy.common.domain.enums.ComplaintStatus;
import com.routbuddy.complaints.domain.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);
    Page<Complaint> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByStatus(ComplaintStatus status);
}
