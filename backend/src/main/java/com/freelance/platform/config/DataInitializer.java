package com.freelance.platform.config;

import com.freelance.platform.common.enums.UserRole;
import com.freelance.platform.entity.User;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initTestUser("client@test.com", "测试发包方", UserRole.CLIENT, new BigDecimal("10000.00"));
        initTestUser("freelancer@test.com", "测试接包方", UserRole.FREELANCER, BigDecimal.ZERO);
    }

    private void initTestUser(String email, String nickname, UserRole role, BigDecimal balance) {
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                String encodedPassword = passwordEncoder.encode("123456");
                if (!passwordEncoder.matches("123456", user.getPassword())) {
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                }
            },
            () -> {
                User user = new User();
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setNickname(nickname);
                user.setRole(role);
                user.setBalance(balance);
                user.setRating(new BigDecimal("5.00"));
                userRepository.save(user);
            }
        );
    }
}
