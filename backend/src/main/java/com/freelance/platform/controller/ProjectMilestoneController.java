package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.request.CreateMilestoneRequest;
import com.freelance.platform.dto.request.UpdateMilestoneRequest;
import com.freelance.platform.dto.response.MilestoneVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.ProjectMilestoneService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/milestones")
public class ProjectMilestoneController {

    @Autowired
    private ProjectMilestoneService milestoneService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping
    public Result<MilestoneVO> createMilestone(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateMilestoneRequest request) {
        Integer userId = getCurrentUserId();
        MilestoneVO milestone = milestoneService.createMilestone(projectId, request, userId);
        return Result.success(milestone);
    }

    @GetMapping
    public Result<List<MilestoneVO>> getMilestones(@PathVariable Integer projectId) {
        List<MilestoneVO> milestones = milestoneService.getMilestonesByProjectId(projectId);
        return Result.success(milestones);
    }

    @GetMapping("/progress")
    public Result<BigDecimal> getMilestoneProgress(@PathVariable Integer projectId) {
        BigDecimal progress = milestoneService.getMilestoneProgress(projectId);
        return Result.success(progress);
    }

    @PutMapping("/{milestoneId}")
    public Result<MilestoneVO> updateMilestone(
            @PathVariable Integer projectId,
            @PathVariable Integer milestoneId,
            @Valid @RequestBody UpdateMilestoneRequest request) {
        Integer userId = getCurrentUserId();
        MilestoneVO milestone = milestoneService.updateMilestone(milestoneId, request, userId);
        return Result.success(milestone);
    }

    @PutMapping("/{milestoneId}/toggle")
    public Result<MilestoneVO> toggleMilestoneCompletion(
            @PathVariable Integer projectId,
            @PathVariable Integer milestoneId) {
        Integer userId = getCurrentUserId();
        MilestoneVO milestone = milestoneService.toggleMilestoneCompletion(milestoneId, userId);
        return Result.success(milestone);
    }

    @DeleteMapping("/{milestoneId}")
    public Result<Void> deleteMilestone(
            @PathVariable Integer projectId,
            @PathVariable Integer milestoneId) {
        Integer userId = getCurrentUserId();
        milestoneService.deleteMilestone(milestoneId, userId);
        return Result.success();
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
