package com.freelance.platform.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private String requirements;

    private ProjectStatus status;
    private Integer clientId;
    private String clientName;
    private String clientAvatar;
    private Integer freelancerId;
    private String freelancerName;
    private String freelancerAvatar;
    private BigDecimal agreedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer bidCount;
}
