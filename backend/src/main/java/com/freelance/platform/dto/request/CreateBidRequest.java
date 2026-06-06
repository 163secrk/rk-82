package com.freelance.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBidRequest {
    @NotNull(message = "报价不能为空")
    private BigDecimal price;

    @NotNull(message = "交付周期不能为空")
    private Integer deliveryDays;

    private String proposal;
}
