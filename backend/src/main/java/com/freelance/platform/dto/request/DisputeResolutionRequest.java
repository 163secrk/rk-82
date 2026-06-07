package com.freelance.platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisputeResolutionRequest {
    @NotBlank(message = "裁决结果不能为空")
    private String resolution;

    @NotNull(message = "裁决金额不能为空")
    @DecimalMin(value = "0", message = "裁决金额不能为负数")
    private BigDecimal resolvedAmount;
}
