package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.request.LoginRequest;
import com.freelance.platform.dto.request.RegisterRequest;
import com.freelance.platform.dto.response.LoginResponse;
import com.freelance.platform.dto.response.UserInfo;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        UserInfo userInfo = authService.register(request);
        return Result.success(userInfo);
    }

    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            UserInfo userInfo = authService.getCurrentUser(email);
            return Result.success(userInfo);
        }
        return Result.error(401, "未登录");
    }
}
