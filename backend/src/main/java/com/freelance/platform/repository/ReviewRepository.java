package com.freelance.platform.repository;

import com.freelance.platform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByRevieweeId(Integer revieweeId);

    boolean existsByProjectIdAndReviewerId(Integer projectId, Integer reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :revieweeId")
    Double calculateAverageRating(@Param("revieweeId") Integer revieweeId);
}
