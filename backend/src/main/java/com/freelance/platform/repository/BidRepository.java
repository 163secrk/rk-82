package com.freelance.platform.repository;

import com.freelance.platform.common.enums.BidStatus;
import com.freelance.platform.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    List<Bid> findByProjectId(Integer projectId);

    List<Bid> findByProjectIdAndStatus(Integer projectId, BidStatus status);

    boolean existsByProjectIdAndFreelancerId(Integer projectId, Integer freelancerId);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.freelancerId = :freelancerId AND b.status = 'PENDING'")
    long countPendingBidsByFreelancerId(@Param("freelancerId") Integer freelancerId);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.projectId = :projectId")
    long countByProjectId(@Param("projectId") Integer projectId);
}
