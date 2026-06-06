package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.request.EscrowRequest;
import com.freelance.platform.dto.request.ReleaseRequest;
import com.freelance.platform.dto.response.PaymentVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/escrow")
    public Result<PaymentVO> escrow(@Valid @RequestBody EscrowRequest request) {
        Integer clientId = getCurrentUserId();
        PaymentVO payment = paymentService.escrow(request, clientId);
        return Result.success(payment);
    }

    @PostMapping("/release")
    public Result<PaymentVO> release(@Valid @RequestBody ReleaseRequest request) {
        Integer clientId = getCurrentUserId();
        PaymentVO payment = paymentService.release(request, clientId);
        return Result.success(payment);
    }

    @PostMapping("/deposit")
    public Result<PaymentVO> deposit(@RequestParam BigDecimal amount) {
        Integer userId = getCurrentUserId();
        PaymentVO payment = paymentService.deposit(amount, userId);
        return Result.success(payment);
    }

    @PostMapping("/withdraw")
    public Result<PaymentVO> withdraw(@RequestParam BigDecimal amount) {
        Integer userId = getCurrentUserId();
        PaymentVO payment = paymentService.withdraw(amount, userId);
        return Result.success(payment);
    }

    @GetMapping("/history")
    public Result<List<PaymentVO>> getPaymentHistory() {
        Integer userId = getCurrentUserId();
        List<PaymentVO> payments = paymentService.getPaymentHistory(userId);
        return Result.success(payments);
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
