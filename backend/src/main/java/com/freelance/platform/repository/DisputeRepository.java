package com.freelance.platform.repository;

import com.freelance.platform.common.enums.DisputeStatus;
import com.freelance.platform.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Integer> {

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE (:status IS NULL OR d.status = :status)")
    Page<Dispute> findByFilters(@Param("status") DisputeStatus status, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE d.initiatorId = :userId OR d.respondentId = :userId")
    List<Dispute> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT d FROM Dispute d WHERE d.projectId = :projectId AND d.status = :status")
    Optional<Dispute> findByProjectIdAndStatus(@Param("projectId") Integer projectId, @Param("status") DisputeStatus status);

    boolean existsByProjectIdAndStatus(Integer projectId, DisputeStatus status);

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = :status")
    long countByStatus(@Param("status") DisputeStatus status);
}
