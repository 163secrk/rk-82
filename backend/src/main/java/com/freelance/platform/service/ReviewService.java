package com.freelance.platform.service;

import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.dto.request.ReviewRequest;
import com.freelance.platform.dto.response.ReviewVO;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.Review;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.ReviewRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReviewVO createReview(ReviewRequest request, Integer reviewerId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new BusinessException("项目未完成，无法评价");
        }

        Integer revieweeId;
        if (reviewerId.equals(project.getClientId())) {
            revieweeId = project.getFreelancerId();
            if (revieweeId == null) {
                throw new BusinessException("项目暂无接包方，无法评价");
            }
        } else if (reviewerId.equals(project.getFreelancerId())) {
            revieweeId = project.getClientId();
        } else {
            throw new BusinessException(403, "您不是该项目的参与方，无法评价");
        }

        if (reviewRepository.existsByProjectIdAndReviewerId(request.getProjectId(), reviewerId)) {
            throw new BusinessException("您已经评价过该项目");
        }

        Review review = new Review();
        review.setProjectId(request.getProjectId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        updateUserRating(revieweeId);

        return convertToVO(review);
    }

    public List<ReviewVO> getReviewsByRevieweeId(Integer revieweeId) {
        List<Review> reviews = reviewRepository.findByRevieweeId(revieweeId);
        return reviews.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Integer userId) {
        Double avg = reviewRepository.calculateAverageRating(userId);
        return avg != null ? avg : 0.0;
    }

    public boolean hasReviewed(Integer projectId, Integer userId) {
        return reviewRepository.existsByProjectIdAndReviewerId(projectId, userId);
    }

    public long getReviewCount(Integer userId) {
        return reviewRepository.findByRevieweeId(userId).size();
    }

    private void updateUserRating(Integer userId) {
        Double avgRating = reviewRepository.calculateAverageRating(userId);
        if (avgRating != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
                userRepository.save(user);
            }
        }
    }

    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        Project project = projectRepository.findById(review.getProjectId()).orElse(null);
        if (project != null) {
            vo.setProjectTitle(project.getTitle());
        }

        User reviewer = userRepository.findById(review.getReviewerId()).orElse(null);
        if (reviewer != null) {
            vo.setReviewerName(reviewer.getNickname());
            vo.setReviewerAvatar(reviewer.getAvatar());
        }

        User reviewee = userRepository.findById(review.getRevieweeId()).orElse(null);
        if (reviewee != null) {
            vo.setRevieweeName(reviewee.getNickname());
            vo.setRevieweeAvatar(reviewee.getAvatar());
        }

        return vo;
    }
}
