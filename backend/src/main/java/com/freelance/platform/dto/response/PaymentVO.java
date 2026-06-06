package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.PaymentStatus;
import com.freelance.platform.common.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Integer id;
    private Integer projectId;
    private String projectTitle;
    private Integer payerId;
    private String payerName;
    private Integer payeeId;
    private String payeeName;
    private BigDecimal amount;
    private PaymentType type;
    private PaymentStatus status;
    private String remark;
    private LocalDateTime createdAt;
}
