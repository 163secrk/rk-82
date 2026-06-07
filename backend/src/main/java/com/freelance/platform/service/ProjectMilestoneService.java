package com.freelance.platform.service;

import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.request.CreateMilestoneRequest;
import com.freelance.platform.dto.request.UpdateMilestoneRequest;
import com.freelance.platform.dto.response.MilestoneVO;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.ProjectMilestone;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.ProjectMilestoneRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectMilestoneService {

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public MilestoneVO createMilestone(Integer projectId, CreateMilestoneRequest request, Integer userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        checkClientPermission(project, userId);

        ProjectMilestone milestone = new ProjectMilestone();
        BeanUtils.copyProperties(request, milestone);
        milestone.setProjectId(projectId);
        if (request.getSortOrder() == null) {
            milestone.setSortOrder(0);
        }

        milestone = milestoneRepository.save(milestone);
        return convertToVO(milestone);
    }

    public List<MilestoneVO> getMilestonesByProjectId(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        List<ProjectMilestone> milestones = milestoneRepository.findByProjectIdOrderBySortOrderAscCreatedAtAsc(projectId);
        return milestones.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public BigDecimal getMilestoneProgress(Integer projectId) {
        long total = milestoneRepository.countByProjectId(projectId);
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        long completed = milestoneRepository.countCompletedByProjectId(projectId);
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public MilestoneVO updateMilestone(Integer milestoneId, UpdateMilestoneRequest request, Integer userId) {
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BusinessException(404, "里程碑不存在"));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        checkClientPermission(project, userId);

        milestone.setName(request.getName());
        milestone.setDescription(request.getDescription());
        milestone.setExpectedDate(request.getExpectedDate());
        if (request.getSortOrder() != null) {
            milestone.setSortOrder(request.getSortOrder());
        }

        milestone = milestoneRepository.save(milestone);
        return convertToVO(milestone);
    }

    @Transactional
    public MilestoneVO toggleMilestoneCompletion(Integer milestoneId, Integer userId) {
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BusinessException(404, "里程碑不存在"));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        checkClientPermission(project, userId);

        if (milestone.getCompleted()) {
            milestone.setCompleted(false);
            milestone.setActualDate(null);
        } else {
            milestone.setCompleted(true);
            milestone.setActualDate(LocalDateTime.now());
        }

        milestone = milestoneRepository.save(milestone);
        return convertToVO(milestone);
    }

    @Transactional
    public void deleteMilestone(Integer milestoneId, Integer userId) {
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BusinessException(404, "里程碑不存在"));

        Project project = projectRepository.findById(milestone.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        checkClientPermission(project, userId);

        milestoneRepository.delete(milestone);
    }

    private void checkClientPermission(Project project, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (user.getRole() != UserRole.CLIENT || !project.getClientId().equals(userId)) {
            throw new BusinessException(403, "只有项目发包方可以操作里程碑");
        }

        if (project.getStatus() == ProjectStatus.DISPUTED) {
            throw new BusinessException("项目存在争议，暂无法进行操作");
        }
    }

    private MilestoneVO convertToVO(ProjectMilestone milestone) {
        MilestoneVO vo = new MilestoneVO();
        BeanUtils.copyProperties(milestone, vo);
        return vo;
    }
}
