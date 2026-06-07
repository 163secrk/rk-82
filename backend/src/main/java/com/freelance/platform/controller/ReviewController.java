package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.request.ReviewRequest;
import com.freelance.platform.dto.response.ReviewVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping
    public Result<ReviewVO> createReview(@Valid @RequestBody ReviewRequest request) {
        Integer reviewerId = getCurrentUserId();
        ReviewVO review = reviewService.createReview(request, reviewerId);
        return Result.success(review);
    }

    @GetMapping("/user/{userId}")
    public Result<List<ReviewVO>> getReviewsByUserId(@PathVariable Integer userId) {
        List<ReviewVO> reviews = reviewService.getReviewsByRevieweeId(userId);
        return Result.success(reviews);
    }

    @GetMapping("/user/{userId}/summary")
    public Result<Map<String, Object>> getUserRatingSummary(@PathVariable Integer userId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", reviewService.getAverageRating(userId));
        summary.put("reviewCount", reviewService.getReviewCount(userId));
        return Result.success(summary);
    }

    @GetMapping("/project/{projectId}/has-reviewed")
    public Result<Boolean> hasReviewed(@PathVariable Integer projectId) {
        Integer userId = getCurrentUserId();
        boolean hasReviewed = reviewService.hasReviewed(projectId, userId);
        return Result.success(hasReviewed);
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = userDetailsService.loadUserEntityByUsername(email);
            return user.getId();
        }
        throw new RuntimeException("未登录");
    }
}
