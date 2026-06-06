package com.freelance.platform.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {
    private Integer publishedProjects;
    private Integer inProgressProjects;
    private Integer completedProjects;
    private Integer pendingBids;
    private Integer unreadMessages;
    private BigDecimal totalEarnings;
    private BigDecimal totalSpent;
    private BigDecimal balance;
}
