package com.freelance.platform.repository;

import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.status = :status " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Project> findByFilters(@Param("status") ProjectStatus status,
                                 @Param("category") String category,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    List<Project> findByClientId(Integer clientId);

    List<Project> findByFreelancerId(Integer freelancerId);

    @Query("SELECT p FROM Project p WHERE p.clientId = :userId OR p.freelancerId = :userId")
    List<Project> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.clientId = :clientId AND p.status = :status")
    long countByClientIdAndStatus(@Param("clientId") Integer clientId, @Param("status") ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.freelancerId = :freelancerId AND p.status = :status")
    long countByFreelancerIdAndStatus(@Param("freelancerId") Integer freelancerId, @Param("status") ProjectStatus status);
}
