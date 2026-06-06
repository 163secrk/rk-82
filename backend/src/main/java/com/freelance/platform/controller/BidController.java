package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.request.CreateBidRequest;
import com.freelance.platform.dto.response.BidVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/projects/{projectId}/bids")
    public Result<BidVO> createBid(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateBidRequest request) {
        Integer freelancerId = getCurrentUserId();
        BidVO bid = bidService.createBid(projectId, request, freelancerId);
        return Result.success(bid);
    }

    @GetMapping("/projects/{projectId}/bids")
    public Result<List<BidVO>> getBidsByProject(@PathVariable Integer projectId) {
        List<BidVO> bids = bidService.getBidsByProject(projectId);
        return Result.success(bids);
    }

    @GetMapping("/bids/my")
    public Result<List<BidVO>> getMyBids() {
        Integer freelancerId = getCurrentUserId();
        List<BidVO> bids = bidService.getBidsByFreelancer(freelancerId);
        return Result.success(bids);
    }

    @PostMapping("/bids/{bidId}/accept")
    public Result<BidVO> acceptBid(@PathVariable Integer bidId) {
        Integer clientId = getCurrentUserId();
        BidVO bid = bidService.acceptBid(bidId, clientId);
        return Result.success(bid);
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
