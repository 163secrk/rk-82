package com.freelance.platform.service;

import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.request.CreateProjectRequest;
import com.freelance.platform.dto.response.MilestoneVO;
import com.freelance.platform.dto.response.ProjectDetailVO;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.BidRepository;
import com.freelance.platform.repository.ProjectMilestoneRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.ReviewRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Transactional
    public ProjectDetailVO createProject(CreateProjectRequest request, Integer clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (client.getRole() != UserRole.CLIENT) {
            throw new BusinessException(403, "只有发包方可以发布项目");
        }

        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        project.setClientId(clientId);
        project.setStatus(ProjectStatus.PUBLISHED);

        project = projectRepository.save(project);
        return convertToDetailVO(project);
    }

    public Page<ProjectDetailVO> getProjectList(int page, int size, String status, String category, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        ProjectStatus projectStatus = status != null && !status.isEmpty() ? ProjectStatus.valueOf(status) : ProjectStatus.PUBLISHED;
        String filterCategory = (category != null && !category.isEmpty()) ? category : null;
        String filterKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : null;

        Page<Project> projectPage = projectRepository.findByFilters(projectStatus, filterCategory, filterKeyword, pageable);
        return projectPage.map(this::convertToDetailVO);
    }

    public ProjectDetailVO getProjectDetail(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));
        return convertToDetailVO(project);
    }

    public List<ProjectDetailVO> getMyProjects(Integer userId) {
        List<Project> projects = projectRepository.findByUserId(userId);
        return projects.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDetailVO updateProjectStatus(Integer id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));
        project.setStatus(status);
        if (status == ProjectStatus.IN_PROGRESS) {
            project.setStartDate(LocalDateTime.now());
        } else if (status == ProjectStatus.COMPLETED) {
            project.setEndDate(LocalDateTime.now());
        }
        project = projectRepository.save(project);
        return convertToDetailVO(project);
    }

    @Transactional
    public ProjectDetailVO deliverProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new BusinessException("项目状态不允许交付");
        }

        if (project.getStatus() == ProjectStatus.DISPUTED) {
            throw new BusinessException("项目存在争议，暂无法进行操作");
        }

        project.setStatus(ProjectStatus.DELIVERED);
        project = projectRepository.save(project);
        return convertToDetailVO(project);
    }

    private ProjectDetailVO convertToDetailVO(Project project) {
        ProjectDetailVO vo = new ProjectDetailVO();
        BeanUtils.copyProperties(project, vo);

        User client = userRepository.findById(project.getClientId()).orElse(null);
        if (client != null) {
            vo.setClientName(client.getNickname());
            vo.setClientAvatar(client.getAvatar());
        }

        if (project.getFreelancerId() != null) {
            User freelancer = userRepository.findById(project.getFreelancerId()).orElse(null);
            if (freelancer != null) {
                vo.setFreelancerName(freelancer.getNickname());
                vo.setFreelancerAvatar(freelancer.getAvatar());
            }
        }

        vo.setBidCount((int) bidRepository.countByProjectId(project.getId()));

        vo.setClientReviewed(reviewRepository.existsByProjectIdAndReviewerId(project.getId(), project.getClientId()));
        if (project.getFreelancerId() != null) {
            vo.setFreelancerReviewed(reviewRepository.existsByProjectIdAndReviewerId(project.getId(), project.getFreelancerId()));
        } else {
            vo.setFreelancerReviewed(false);
        }

        long totalMilestones = milestoneRepository.countByProjectId(project.getId());
        if (totalMilestones > 0) {
            long completedMilestones = milestoneRepository.countCompletedByProjectId(project.getId());
            BigDecimal progress = BigDecimal.valueOf(completedMilestones)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalMilestones), 2, RoundingMode.HALF_UP);
            vo.setMilestoneProgress(progress);
        } else {
            vo.setMilestoneProgress(BigDecimal.ZERO);
        }

        List<MilestoneVO> milestones = milestoneRepository.findByProjectIdOrderBySortOrderAscCreatedAtAsc(project.getId())
                .stream()
                .map(m -> {
                    MilestoneVO mvo = new MilestoneVO();
                    BeanUtils.copyProperties(m, mvo);
                    return mvo;
                })
                .collect(Collectors.toList());
        vo.setMilestones(milestones);

        return vo;
    }
}
