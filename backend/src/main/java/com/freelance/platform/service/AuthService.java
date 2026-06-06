package com.freelance.platform.service;

import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.dto.request.LoginRequest;
import com.freelance.platform.dto.request.RegisterRequest;
import com.freelance.platform.dto.response.LoginResponse;
import com.freelance.platform.dto.response.UserInfo;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.UserRepository;
import com.freelance.platform.security.JwtTokenProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        UserInfo userInfo = convertToUserInfo(user);
        return new LoginResponse(token, userInfo);
    }

    @Transactional
    public UserInfo register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("该邮箱已被注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(request.getRole());
        user.setBalance(BigDecimal.ZERO);
        user.setRating(new BigDecimal("5.00"));

        if (request.getRole() == UserRole.CLIENT) {
            user.setBalance(new BigDecimal("10000.00"));
        }

        user = userRepository.save(user);
        return convertToUserInfo(user);
    }

    public UserInfo getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return convertToUserInfo(user);
    }

    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }
}
