package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.BidStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidVO {
    private Integer id;
    private Integer projectId;
    private Integer freelancerId;
    private String freelancerName;
    private String freelancerAvatar;
    private BigDecimal price;
    private Integer deliveryDays;
    private String proposal;
    private BidStatus status;
    private LocalDateTime createdAt;
}
