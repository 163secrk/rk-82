package com.freelance.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EscrowRequest {
    @NotNull(message = "项目ID不能为空")
    private Integer projectId;

    @NotNull(message = "托管金额不能为空")
    private BigDecimal amount;
}
