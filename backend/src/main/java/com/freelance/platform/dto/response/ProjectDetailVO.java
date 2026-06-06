package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectDetailVO {
    private Integer id;
    private String title;
    private String description;
    private String category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime deadline;
    private ProjectStatus status;
    private Integer clientId;
    private String clientName;
    private String clientAvatar;
    private Integer freelancerId;
    private String freelancerName;
    private String freelancerAvatar;
    private BigDecimal agreedPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer bidCount;
}
