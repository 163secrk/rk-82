package com.freelance.platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDisputeRequest {
    @NotNull(message = "项目ID不能为空")
    private Integer projectId;

    @NotBlank(message = "争议原因不能为空")
    private String reason;

    @NotNull(message = "诉求金额不能为空")
    @DecimalMin(value = "0.01", message = "诉求金额必须大于0")
    private BigDecimal claimedAmount;
}
