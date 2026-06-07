package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.DisputeStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeVO {
    private Integer id;
    private Integer projectId;
    private String projectTitle;
    private Integer initiatorId;
    private String initiatorName;
    private Integer respondentId;
    private String respondentName;
    private String reason;
    private BigDecimal claimedAmount;
    private DisputeStatus status;
    private String resolution;
    private BigDecimal resolvedAmount;
    private LocalDateTime resolvedAt;
    private Integer adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
