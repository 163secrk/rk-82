package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.common.enums.DisputeStatus;
import com.freelance.platform.dto.request.CreateDisputeRequest;
import com.freelance.platform.dto.request.DisputeResolutionRequest;
import com.freelance.platform.dto.response.DisputeVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.DisputeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping
    public Result<DisputeVO> createDispute(@Valid @RequestBody CreateDisputeRequest request) {
        Integer userId = getCurrentUserId();
        DisputeVO dispute = disputeService.createDispute(request, userId);
        return Result.success(dispute);
    }

    @GetMapping("/my")
    public Result<List<DisputeVO>> getMyDisputes() {
        Integer userId = getCurrentUserId();
        List<DisputeVO> disputes = disputeService.getUserDisputes(userId);
        return Result.success(disputes);
    }

    @GetMapping("/{id}")
    public Result<DisputeVO> getDisputeDetail(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        DisputeVO dispute = disputeService.getDisputeDetail(id, userId);
        return Result.success(dispute);
    }

    @GetMapping("/admin")
    public Result<Page<DisputeVO>> getAllDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer adminId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DisputeVO> disputes = disputeService.getAllDisputes(status, pageable, adminId);
        return Result.success(disputes);
    }

    @PostMapping("/{id}/resolve/refund")
    public Result<DisputeVO> resolveDisputeRefund(
            @PathVariable Integer id,
            @Valid @RequestBody DisputeResolutionRequest request) {
        Integer adminId = getCurrentUserId();
        DisputeVO dispute = disputeService.resolveDisputeRefund(id, request, adminId);
        return Result.success(dispute);
    }

    @PostMapping("/{id}/resolve/release")
    public Result<DisputeVO> resolveDisputeRelease(
            @PathVariable Integer id,
            @Valid @RequestBody DisputeResolutionRequest request) {
        Integer adminId = getCurrentUserId();
        DisputeVO dispute = disputeService.resolveDisputeRelease(id, request, adminId);
        return Result.success(dispute);
    }

    @PostMapping("/{id}/cancel")
    public Result<DisputeVO> cancelDispute(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        DisputeVO dispute = disputeService.cancelDispute(id, userId);
        return Result.success(dispute);
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
