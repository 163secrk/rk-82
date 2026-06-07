package com.freelance.platform.repository;

import com.freelance.platform.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Integer> {
    List<ProjectMilestone> findByProjectIdOrderBySortOrderAscCreatedAtAsc(Integer projectId);

    @Query("SELECT COUNT(m) FROM ProjectMilestone m WHERE m.projectId = :projectId AND m.completed = true")
    long countCompletedByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT COUNT(m) FROM ProjectMilestone m WHERE m.projectId = :projectId")
    long countByProjectId(@Param("projectId") Integer projectId);

    void deleteByProjectId(Integer projectId);
}
