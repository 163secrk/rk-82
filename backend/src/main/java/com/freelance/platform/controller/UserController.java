package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.response.DashboardVO;
import com.freelance.platform.dto.response.MonthlyTrendVO;
import com.freelance.platform.dto.response.UserInfo;
import com.freelance.platform.entity.User;

import java.util.List;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/{id}")
    public Result<UserInfo> getUserById(@PathVariable Integer id) {
        UserInfo userInfo = userService.getUserById(id);
        return Result.success(userInfo);
    }

    @PutMapping("/{id}")
    public Result<UserInfo> updateUser(@PathVariable Integer id, @RequestBody UserInfo userInfo) {
        Integer currentUserId = getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return Result.error(403, "无权限修改其他用户信息");
        }
        UserInfo updated = userService.updateUser(id, userInfo);
        return Result.success(updated);
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        Integer userId = getCurrentUserId();
        DashboardVO dashboard = userService.getDashboard(userId);
        return Result.success(dashboard);
    }

    @GetMapping("/monthly-trend")
    public Result<List<MonthlyTrendVO>> getMonthlyTrend() {
        Integer userId = getCurrentUserId();
        List<MonthlyTrendVO> trend = userService.getMonthlyTrend(userId);
        return Result.success(trend);
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
