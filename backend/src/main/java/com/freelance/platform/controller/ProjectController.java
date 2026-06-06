package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.common.enums.ProjectStatus;
import com.freelance.platform.dto.request.CreateProjectRequest;
import com.freelance.platform.dto.response.ProjectDetailVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping
    public Result<ProjectDetailVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Integer clientId = getCurrentUserId();
        ProjectDetailVO project = projectService.createProject(request, clientId);
        return Result.success(project);
    }

    @GetMapping
    public Result<Page<ProjectDetailVO>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<ProjectDetailVO> projects = projectService.getProjectList(page, size, status, category, keyword);
        return Result.success(projects);
    }

    @GetMapping("/{id}")
    public Result<ProjectDetailVO> getProjectDetail(@PathVariable Integer id) {
        ProjectDetailVO project = projectService.getProjectDetail(id);
        return Result.success(project);
    }

    @GetMapping("/my")
    public Result<List<ProjectDetailVO>> getMyProjects() {
        Integer userId = getCurrentUserId();
        List<ProjectDetailVO> projects = projectService.getMyProjects(userId);
        return Result.success(projects);
    }

    @PutMapping("/{id}/status")
    public Result<ProjectDetailVO> updateProjectStatus(
            @PathVariable Integer id,
            @RequestParam ProjectStatus status) {
        ProjectDetailVO project = projectService.updateProjectStatus(id, status);
        return Result.success(project);
    }

    @PutMapping("/{id}/deliver")
    public Result<ProjectDetailVO> deliverProject(@PathVariable Integer id) {
        ProjectDetailVO project = projectService.deliverProject(id);
        return Result.success(project);
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
